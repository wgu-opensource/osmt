# Hackathon Project: Making OSMT more deployable to cloud environment

Notes and planning:

1. User persona: An organization with an IT staff that manage their cloud resources. This organization values reliability over cheapness most of the time, so they would prefer services such as Elasticache and RDS over managing their own Redis or Database inside docker containers.
2. Goal: deliver something robust on AWS, use their services for DB, Cache & Elasticsearch
3. Passing configuration to docker in an adequate way will need some attention.
   - [ ] Assume AWS account is secure. Using policies to set up account securely. Will need to document how to keep it secure
   - Going to have container definitions for the application that pass in a big list of environment variable pairs through terraform
   - Don't want to keep secrets in repository etc (can't because repo is shared across open source users)
   - We might think about using AWS Secrets Manager for this.
4. Network: deploying high-availability is not too expensive. Worth doing 2 AZs
   - 4-subnet situation: 2 public subnets and 2 private subnets. Everything other than the ALB will be in the private subnet.
5. How do we actually identify/name and deploy a version of OSMT that a user wants to deploy (for example, at the git commit they currently have checked out, or a specific git commit they desire.)

TODOs:

- [ ] Deliver an example config object with sensible defaults and clarity about what needs individual configuration to get started up
- [x] Write instructions on how to set up an AWS account:
- [x] note "private_subnets?" in `ecs.tf`
- [x] Check `cloudwatch.tf` for correctness
- [x] Include solid `.gitignore` for excluding files that might hold user secrets.
- [ ] Document how user will choose their AWS region and choose two AZs and populate that into terraform config
- [ ] Get to the point where migrations are running. Migrations will probably fail against RDS due to `utf8mb3_unicode_ci` encoded into migrations. Discuss and figure out plan.
- [x] Add `.terraform.lock.hcl` to `.gitignore`?
- [x] Add `iam.tf` to manage policies
  - [ ] create security groups

Nice to Have Enhancements (probably leave for later):

- [ ] Add S3 bucket and store ALB access logs into it.

# Recipe: Set up and manage OSMT infrastructure on AWS

This recipe is intended to be a starting point for organizations that want to deploy OSMT in an AWS cloud environment. It is not the only way to deploy OSMT, but it shows a workable approach that aims to be simple, secure, and production-ready. This approach can be copied and modified for specific organizational needs.

This directory includes code that enables an implementer to quickly provision the OSMT application into a new production environment on the AWS cloud. The approach uses Terraform to provision infrastructure on a specified AWS account including: Route53, ECS/Fargate, RDS, Elasticache, and Cloudwatch. Configuration is synced with a S3 Bucket and DynamoDB table.

This is not the only way to deploy OSMT, but it shows a workable approach that aims to be simple, secure, and production-ready. This approach can be copied and modified for specific organizational needs. Some runtime secrets are accessible within privileged access on the AWS Console, so maintaining the security of the AWS account is important.

The workflows documented here have been developed and tested on MacOS but use tools supported more broadly. As an admin user of an AWS account, these instructions will enable you to set up infrastructure from a local computer.

Advanced installations will likely extend and customize the infrastructure beyond what is mentioned here. For example, teams can populate settings templates with secrets in secure automated environments. This approach is not covered here, but discussion and further development is welcome within the OSMT community.

**Known Issues and TODO improvements**:

- WARNING! Migrations are not yet working against RDS due to `utf8mb3_unicode_ci` encoded into migrations. To discuss with maintainer team.
- App containers are set to run migrations and reindex elasticsearch. There should be a container usually not running that is used to run migrations and reindexing. This is a TODO.

## Install dependencies on your local computer

- Terraform CLI ([See instructions](https://developer.hashicorp.com/terraform/tutorials/aws-get-started/install-cli))
- Optional: AWS CLI ([See instructions](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html))

## Create an AWS Account and relevant access roles

- Go to the [AWS Console](https://console.aws.amazon.com/console/home) and select "Create a new AWS account". Or use an existing account, but make sure that the account is secure, because this approach encodes some application secrets into environment variables. Only system admins authorized to access this secure configuration should have relevant permissions in the account.

- Sign up and confirm email addresses, real addresses, and payment method
- Create IAM Role for `osmt-admin`
  - Go to the Identity & Access Management (IAM) service and select Users
  - Add IAM user: select "Add user" -> IAM -> attach policies manually -> add policy `AdministratorAccess`
- Obtain Access key and save in your local workspace

  - Go to IAM console
  - Go to user you created
  - Go to security credentials
  - Create access key
  - Select CLI option
    Create a description tag value for the access key (OSMT infra)
  - Download CSV of the access key, store securely
  - Make `~/.aws/credentials` file and fill values from CSV:

  ```
  [osmt]
  aws_access_key_id = YOURKEY
  aws_secret_access_key = SECRET
  ```

  - Test credentials: `aws sts get-caller-identity --profile osmt`

- Choose AWS region and 2 availability zones you want to deploy your app to. For example, select the `us-west-2` region with AZs `us-west-2a` and `us-west-2b`. This will be used in the Terraform configuration.

### Set up an S3 bucket and Dynamo DB to store terraform state for the deployment.

This recipe uses S3 to back up the terraform state, to enable the infrastructure to be managed by multiple users or the same user across devices over time. The Terraform state is stored in a S3 bucket with a key name identified in a DynamoDB table. These resources are created manually in the AWS console.

- Go to S3 within the AWS console: Create new bucket with settings:
  name: `osmt-test` (It is recommended to use this format to indicate application and environment, e.g. `osmt-prod`. This value will be the name of the environment you'll deploy and will be used later in the Terraform configuration).
  - ACLs Disabled (object ownership: bucket owner enforced)
  - Block all public access
  - Bucket versioning: enable
  - Default encryption (use defaults: AWS S3 managed keys, leave bucket key enabled)
- Go to DynamoDB within the AWS Console
  - create table `osmt-test`
  - Partition key: `LockID`
  - No sort key is needed
  - Table settings: default

A future upgrade to this guide could replace these manual steps with AWS CLI commands.

## Generate configuration files

To get the app ready to deploy into your AWS account, generate settings files from templates and populate them with your custom environment settings.

- [ ] In the `terraform/invocation` directory, run a script `TODO` to generate `main.tf` from the template `main.tf.tpl`.
- Generate `config.auto.tfvars.json` and `secrets.auto.tfvars.json` in the same directory.
- Begin populating templates with data.

## Configure your domain name and SSL certificate

- Buy a domain at your registrar of choice (you can use AWS Route53 for easiest setup), or identify a subdomain that you want to direct to the OSMT app
- Note: to serve the app at the root of a domain, your registrar needs to support the `ALIAS` record type. Route53 supports this, but some registrars do not.
- Create a hosted zone matching your domain name. Add a description, and choose type public. If you used Route53 to register, this will be created automatically
- Configure certificate: go to AWS Certificate manager in the console. Request certificate -> Public. Follow any instructions to verify your domain name.
  - Add your domain name (and `*.yourdomainname.net`) to the domain names. DNS validation, default key algorithm.
  - Go into certificate (probably pending). Click Create Records in Route53
  - Copy the ARN from the certificate detail page. Paste it into `config.auto.tfvars.json` as the value for `config.alb.certificate_arn`

## Run terraform to create cloud resources

- The goal of the user is to generate terraform code starting point (`main.tf`) that is ready to run against their AWS account.
- Terraform needs to look at your config before it's able to do fancy terraform stuff, like generating "var.config" from stored state.
- [ ] Copy `main.tf.tpl` `main.tf`, customize values (TODO check)
- Navigate to `/infra/terraform/invocation`. Run command `terraform init`. Troubleshoot any errors that appear (typically means missing or incorrect configuration)
- Observe resources to be created: `terraform plan`
- Apply plan: `terraform apply`

Observe the resources being created in your AWS Account. This will result in a version of the application

## Cleanup: Take down the infrastructure (don't pay for something you're not using)

For test environments, it's important to not pay for resources you're not using. Here is how to turn off your AWS resources:

- Remove `enable_deletion_protection` (e.g. in `config.auto.tfvars.json`)
- Invoke terraform to tear down resources: `terraform destroy`
