terraform {
    backend "s3" {
        bucket         = "<your_s3_bucket_name>"
        dynamodb_table = "<your_dynamodb_table_name>"
        encrypt        = true
        key            = "<state_file_s3_object_key>"
        profile        = "<your_aws_profile_name>"
        region         = "<aws_region>"
    }
}

provider "aws" {
    profile = "<your_aws_profile_name>"
    region  = "<aws_region>"

    default_tags {
        foo = "bar"
    }
}

module "osmt" {
    source = "../module/"

    config = ?
}