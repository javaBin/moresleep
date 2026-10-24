# moresleep on the javaBin platform

This guide explains how moresleep runs on the javaBin platform. It is written for whoever maintains
moresleep and wants to know what happens to their code after a merge.

**What does not change:** the database (`dbmoresleep`, same data, same credentials), the public
hostname `https://sleepingpill.javazone.no`, the Basic-auth model, and the env-var configuration
read by `Setup.kt`. No consumer (submit, cake-redux, cupcake, the JavaZone web sites, ...) needs to
change anything.

**What changed:** the code is built and deployed by a shared pipeline instead of `cdk deploy`. The
container runs in the platform's ECS cluster behind the platform's load balancer, and secrets come
from SSM Parameter Store instead of a SOPS file baked into the image.

---

## 1. The big picture

```
 app.yaml ──(expand)──► terraform/ ──(plan + review)──► AWS resources
    │                                                    ECR repo, target group, ALB rule,
    │                                                    DNS record, ECS service, IAM role,
    │                                                    SSM secrets, alarms, + adopted RDS
 Dockerfile ──(docker build)──► ECR image ──(ecs deploy)──► 2 Fargate tasks on port 5000
 pom.xml ──(mvn verify against postgres:17)──► tests must pass before anything deploys
```

You describe *what* the app needs in **`app.yaml`**. The platform turns that into Terraform, plans
it, has the plan reviewed, applies it, builds the Docker image and rolls it out. There is no
`cdk deploy` and no AWS credentials in the repo. GitHub Actions gets short-lived credentials
through OIDC, scoped to the `moresleep` team.

The platform's own docs live in [javaBin/platform/docs](https://github.com/javaBin/platform/tree/main/docs).
The most useful ones are [app-yaml-reference.md](https://github.com/javaBin/platform/blob/main/docs/app-yaml-reference.md)
and [reusable-workflows.md](https://github.com/javaBin/platform/blob/main/docs/reusable-workflows.md).

## 2. app.yaml, field by field

| Section | What it creates | moresleep value |
|---|---|---|
| `name`, `team` | Resource names are `<team>-<name>`, here `moresleep-moresleep`. `team` must be a team in [javaBin/registry](https://github.com/javaBin/registry), and the repo must belong to that GitHub team. | `moresleep` / `moresleep` |
| `compute` | ECS Fargate task definition + service (x86, private subnets, read-only root filesystem with a writable `/tmp`, runs as uid 1000) | 256 CPU, 512 MB, port 5000, health check `/` |
| `routing` | ALB target group, a listener rule on the shared HTTPS load balancer (`priority` must be unique), and a Route53 A record. The wildcard certificate already covers `*.javazone.no`. | `moresleep.javazone.no` during the parallel run, `sleepingpill.javazone.no` after the cutover |
| `resources.databases` | Normally a *new* RDS instance. `adopt:` instead **imports the existing one**: Terraform takes it into its state, adds tags and turns on deletion protection, and changes nothing else (class, storage, network, security group, password all stay). | `adopt: dbmoresleep`, with `DBHOST`/`DBPORT` injected from the instance |
| `resources.secrets` | One SSM SecureString per entry, injected as an env var when a task starts. Created with the value `PLACEHOLDER`. | DB name/user/password, ALLACCESS_USER, READ_USER |
| `environment` | Plain env vars | the non-secret settings that used to be in the SOPS file |

The generated Terraform lands in `terraform/`. Do not edit it by hand: it is regenerated from
`app.yaml` on every run, and a pull request shows the regenerated files in its diff. You may add your
own extra `.tf` files next to the generated ones; the generator only overwrites its own files.

## 3. The pipeline (`.github/workflows/deploy.yml`)

`deploy.yml` is a few lines that call the shared workflow `javaBin/platform/.github/workflows/javabin.yml@main`.
It detects what is in the repo and runs:

| Job | On a pull request | On `master` (merge or manual run) |
|---|---|---|
| **build-jvm** | `mvn verify` with a throwaway `postgres:17` container on `localhost:5432` (db `moresleepunit`, user `localdevuser`), the same setup as `docker/postgres` | same |
| **expand** | runs the generator; if `terraform/` changed, it commits the result to the PR branch | same |
| **tf-plan** | `terraform plan`. An AI reviewer rates the risk, and the plan and review are posted as a PR comment | same; the plan is stored for apply |
| **tf-apply** | — | applies exactly the reviewed plan (a HIGH risk rating blocks it until a platform owner approves an override) |
| **docker-build** | — | builds `Dockerfile`, pushes to ECR `moresleep-moresleep` with tags `sha-<commit>` and `latest` |
| **ecs-deploy** | — | rolls the ECS service onto the new image and waits until it is stable |

So: **open a PR to see what would change, merge to deploy.** Nothing touches AWS from a PR.

## 4. Secrets

Secrets live in SSM Parameter Store under `/javabin/apps/moresleep/moresleep/<name>`. To set or change
one:

```bash
brew install javaBin/tap/javabin          # once
aws sso login --profile javabin           # your SSO user, permission set javabin-team-moresleep
cd moresleep
javabin secret list                       # shows which ones still hold PLACEHOLDER
javabin secret set db-password            # prompts without echo
```

The platform restarts the service within about a minute of a secret changing, so the tasks pick up
the new value. Terraform never overwrites a value you have set.

For the migration, the values came from the old SOPS file (`conf/.enc.env`, removed after the cutover; still in git history):

| SOPS variable | platform secret |
|---|---|
| `DATASOURCENAME` | `db-name` |
| `DBUSER` | `db-user` |
| `DBPASSWORD` | `db-password` |
| `ALLACCESS_USER` | `allaccess-user` |
| `READ_USER` | `read-user` |

`DBHOST` is injected from the adopted instance. The other SOPS values (`SERVER_PORT`, `RUN_FROM_JAR`,
`ALL_OPEN_MODE`, `LOAD_FROM_SLEEPINGPILL`) are plain `environment:` entries in `app.yaml`. Check them
against the decrypted file before the cutover. `SLEEPINGPILL_AUTH` is only used to import from the
retired sleepingPillCore, so it is dropped.

## 5. Day-to-day

| I want to... | Do this |
|---|---|
| deploy a code change | merge to `master` |
| see logs | CloudWatch log group `/ecs/moresleep/moresleep` (AWS console via [aws.javazone.no](https://aws.javazone.no), or `aws logs tail /ecs/moresleep/moresleep --follow`) |
| see status and cost | `javabin status` |
| scale | change `compute.desired_count` / `cpu` / `memory` in `app.yaml`, open a PR, merge |
| add an env var | `environment:` in `app.yaml` (plain) or `resources.secrets` (secret) |
| roll back code | revert the commit on `master`; the pipeline deploys the previous code |
| run a schema migration | as today: `DO_FLYWAY_MIGRATION` stays `false` in production. Run migrations deliberately, e.g. by setting it to `"true"` for one deploy |

Alarms (CPU, memory, 5xx, unhealthy targets) go to the platform alert channel.
The team's monthly budget (500 NOK) is set in [javaBin/registry](https://github.com/javaBin/registry) `teams/moresleep.yaml`.

## 6. Network and database

Platform tasks run in private subnets of the platform VPC and reach the internet through its NAT
gateway. `dbmoresleep` stays where it is (default VPC, public endpoint, TLS forced by the
`default.postgres17` parameter group), so the tasks connect to it the same way other clients do.
Nothing about the database moves. Because both the old stack and the platform use the same database,
the parallel run and the cutover need no data copy and no write freeze.

## 7. Migration steps

Each step is one pull request in this stack. Merge them in order, and only when the step's
precondition holds:

1. `chore: make master match the deployed awsdocker line`. Production runs `awsdocker`, so master must contain it first.
2. `docs(platform): add app.yaml and a platform guide`. This file. It changes nothing on its own.
3. `ci: deploy with the javaBin platform pipeline`. The first apply imports the DB and creates the service with 0 tasks.
4. `feat(platform): run two tasks on moresleep.javazone.no`. Merge after the secrets are set.
5. `feat(platform): serve sleepingpill.javazone.no from the platform`. The cutover, after the parallel run checks out.
6. `chore: remove CDK, Elastic Beanstalk and SOPS deploy bits`. Merge 14 days after the cutover, once the old stack is deleted.
