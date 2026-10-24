# GENERATED FROM app.yaml — do not edit, changes will be overwritten
terraform {
  required_version = ">= 1.5"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = "eu-central-1"

  default_tags {
    tags = {
      team        = "moresleep"
      service     = "moresleep"
      repo        = "javaBin/moresleep"
      environment = "production"
      managed-by  = "terraform"
    }
  }
}
