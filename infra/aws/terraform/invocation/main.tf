terraform {
    backend "s3" {
        bucket         = "osmt-test"
        dynamodb_table = "osmt-test"
        encrypt        = true
        key            = "terraform/state"
        profile        = "osmt"
        region         = "us-west-2"
    }
}

provider "aws" {
    profile = "osmt"
    region  = "us-west-2"

    default_tags {
        tags = {
            TerraformModule = "OSMT"
        }
    }
}

module "osmt" {
    source = "../module/"

    config  = var.config
    secrets = var.secrets
}