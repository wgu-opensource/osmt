# Creating AWS resources for OSMT with Terraform #
***

This guild will help you deploy the necessary resources in AWS to stand up an OSMT app on the cloud.

## Prerequisites ##

Before you begin, you must complete the following prerequisites:

- An AWS account with permissions to create the necessary resources.
- AWS CLI installed

## Setting up AWS in Terraform ##

To use Terraform to manage and deploy resources and infrastructure to AWS, you will need to use the AWS
provider. You must configure the provider with the proper credentials before you can use it. This provider
is maintained internally by the HashiCorp AWS Provider team.

```hcl
terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
      version = "~> 4.53.0"
    }
  }
}

provider "aws" {
  profile = var.aws_profile
}
```

#### Note: ####

*As you can see we have the version set to "4.53.0" (latest at the time of writing). This is recommended best
practice to avoid any unexpected changes in behavior between provider versions. Setting the version allows
you to manually update it from the terraform CLI in the future.*

In the provider block is where you will add your authentication method of choice. The most common methods of
authenticating is either passing in an access and secret key or passing in a token if you are using federated
login. Both examples make use of environment variables. If using the access and secret key method you will
need to set up the environment variables of `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` and `AWS_REGION` and
leave the provider block empty as below. Note, you will need to create these keys in the AWS console and add
them using the AWS CLI via the `aws configure`.

```shell
$ export AWS_ACCESS_KEY_ID="my-access-key"
$ export AWS_SECRET_ACCESS_KEY="my-secret-key"
$ export AWS_REGION="us-west-2" 
```
Alternatively, a token can be used instead of Key ID and Access Key:
```shell
$ export AWS_SESSION_TOKEN="my-token"
```
Leave the provider block empty
```hcl
provider "aws" {}
```

The method we use here just passes in a profile which was configured when setting up the AWS CLI. We found this
to be the simplest way to do it if using federated login. In the provider block just add
`profile = "my-aws-profile"` and set the `AWS_PROFILE` environment variable to your AWS profile name

```shell
$ export AWS_PROFILE="my-aws-profile"
```

## The Resources ##

The resources required to stand up an OSMT instance are listed below:

- AWS VPC
- AWS Subnet (a public subnet which is done with a security group)
- AWS Internet Gateway
- AWS Route table
- AWS security group ( Allow internet traffic to your EC2 instance )
- AWS ECS
- AWS RDS
- AWS Elasticache
- AWS EC2

The AWS VPC, Subnet, Internet Gateway, Route table and security group are the main Networking setup required
for the resources to be able to communicate with one another. The actual resources to stand up are the AWS
ECS, RDS, Elasticache and EC2.

## ENV Variables ##

There a few places in the main.tf file that we are passing variables in. These are also in the variables file.
You will need to pass the following variables via environment variables

- db_user
- db_password
- db_name
- aws_profile
- default_sg
- subnet_id

The `db_*` variables are all passed to the RDS resource for you managed mysql. The `aws_profile` is to pass
the profile you'd like to authenticate with. The remaining are used for the EC2 instance as you will need to
pass a public subnet and security group that allows internet traffic. When creating the environment variables
you will need to prefix it with `TF_VAR_` This way terraform knows to use these variables. Example below.

```shell
$ export TF_VAR_db_user="user"
```
