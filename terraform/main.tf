# GENERATED FROM app.yaml — do not edit, changes will be overwritten
module "platform" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/platform-data?ref=main"

  project = "javabin"
  domain  = "javazone.no"
  team    = "moresleep"
}

module "ecr" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/ecr-repo?ref=main"

  name = "moresleep"
  team = "moresleep"
}

module "routing" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/service-routing?ref=main"

  name                   = "moresleep"
  team                   = "moresleep"
  vpc_id                 = module.platform.vpc_id
  port                   = 5000
  health_check_path      = "/"
  health_check_matcher   = "200"
  https_listener_arn     = module.platform.https_listener_arn
  listener_rule_priority = 120
  host_header            = "moresleep.javazone.no"
  route53_zone_id        = module.platform.route53_zone_ids["javazone.no"]
  alb_dns_name           = module.platform.alb_dns_name
  alb_zone_id            = module.platform.alb_zone_id
  deregistration_delay   = 30
}

module "rds_main" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/service-rds-adopted?ref=main"

  identifier          = "dbmoresleep"
  deletion_protection = true
}

module "secret_db-name" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/service-secret?ref=main"

  name        = "db-name"
  team        = "moresleep"
  project     = "javabin"
  service     = "moresleep"
  description = ""
}

module "secret_db-user" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/service-secret?ref=main"

  name        = "db-user"
  team        = "moresleep"
  project     = "javabin"
  service     = "moresleep"
  description = ""
}

module "secret_db-password" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/service-secret?ref=main"

  name        = "db-password"
  team        = "moresleep"
  project     = "javabin"
  service     = "moresleep"
  description = ""
}

module "secret_allaccess-user" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/service-secret?ref=main"

  name        = "allaccess-user"
  team        = "moresleep"
  project     = "javabin"
  service     = "moresleep"
  description = ""
}

module "secret_read-user" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/service-secret?ref=main"

  name        = "read-user"
  team        = "moresleep"
  project     = "javabin"
  service     = "moresleep"
  description = ""
}

module "task_role" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/service-role?ref=main"

  name                     = "moresleep"
  team                     = "moresleep"
  region                   = "eu-central-1"
  aws_account_id           = "553637109631"
  permissions_boundary_arn = module.platform.developer_boundary_arn
  additional_policy_jsons = {
    secret_db-name        = module.secret_db-name.access_policy_json
    secret_db-user        = module.secret_db-user.access_policy_json
    secret_db-password    = module.secret_db-password.access_policy_json
    secret_allaccess-user = module.secret_allaccess-user.access_policy_json
    secret_read-user      = module.secret_read-user.access_policy_json
  }
}

module "service" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/ecs-service?ref=main"

  name                     = "moresleep"
  team                     = "moresleep"
  cluster_id               = module.platform.ecs_cluster_id
  image                    = module.ecr.repository_url
  cpu                      = 256
  memory                   = 512
  port                     = 5000
  desired_count            = 0
  execution_role_arn       = module.platform.execution_role_arn
  task_role_arn            = module.task_role.role_arn
  subnet_ids               = module.platform.private_subnet_ids
  security_group_ids       = [module.platform.ecs_tasks_security_group_id]
  region                   = "eu-central-1"
  container_user           = "1000"
  readonly_root_filesystem = true
  target_group_arn         = module.routing.target_group_arn
  environment = {
    SERVER_PORT            = "5000"
    RUN_FROM_JAR           = "true"
    DATABASE_TYPE          = "POSTGRES"
    DO_FLYWAY_MIGRATION    = "false"
    LOAD_FROM_SLEEPINGPILL = "false"
    ALL_OPEN_MODE          = "false"
    DBHOST                 = module.rds_main.address
    DBPORT                 = module.rds_main.port
  }
  secrets = {
    DATASOURCENAME = module.secret_db-name.parameter_arn
    DBUSER         = module.secret_db-user.parameter_arn
    DBPASSWORD     = module.secret_db-password.parameter_arn
    ALLACCESS_USER = module.secret_allaccess-user.parameter_arn
    READ_USER      = module.secret_read-user.parameter_arn
  }
}

module "alarms" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/service-alarm?ref=main"

  name                    = "moresleep"
  team                    = "moresleep"
  service                 = "moresleep"
  cluster_name            = module.platform.ecs_cluster_name
  sns_topic_arns          = [data.aws_sns_topic.alerts.arn]
  cpu_threshold           = 80
  memory_threshold        = 80
  error_5xx_threshold     = 10
  target_group_arn_suffix = module.routing.target_group_arn_suffix
  alb_arn_suffix          = module.platform.alb_arn_suffix
}

data "aws_sns_topic" "alerts" {
  name = "javabin-alerts"
}

import {
  to = module.rds_main.aws_db_instance.this
  id = "dbmoresleep"
}
