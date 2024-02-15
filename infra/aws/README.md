# Recipe: Set up and manage OSMT infrastructure on AWS

This recipe is intended to be a starting point for organizations that want to deploy OSMT in an AWS cloud environment. It is not the only way to deploy OSMT, but it shows a workable approach that aims to be simple, secure, and production-ready. This approach can be copied and modified for specific organizational needs.

This directory includes code that enables an implementer to quickly provision the OSMT application into a new production environment on the AWS cloud. The approach uses Terraform to provision infrastructure on a specified AWS account including: Route53, ECS/Fargate, RDS, Elasticache, and Cloudwatch. Configuration is synced with a S3 Bucket and DynamoDB table.

This is not the only way to deploy OSMT, but it shows a workable approach that aims to be simple, secure, and production-ready. This approach can be copied and modified for specific organizational needs. Some runtime secrets are accessible within privileged access on the AWS Console, so maintaining the security of the AWS account is important.

The workflows documented here have been developed and tested on MacOS but use tools supported more broadly. As an admin user of an AWS account, these instructions will enable you to set up infrastructure from a local computer.

Advanced installations will likely extend and customize the infrastructure beyond what is mentioned here. For example, teams can populate settings templates with secrets in secure automated environments. This approach is not covered here, but discussion and further development is welcome within the OSMT community.

**Known Issues and potential improvements for additional security and functionality**:

- WARNING! Migrations are not yet working against RDS due to `utf8mb3_unicode_ci` encoded into migrations. To discuss with maintainer team.
- App containers are set to run migrations and reindex elasticsearch. There should be a container usually not running that is used to run migrations and reindexing. This is a TODO.
- Application is using root database credentials. A clever system for swapping out credentials for different procedures would be a good improvement.
- CloudWatch logs are not encrypted.
- Application environment variables, including some application secrets are made available to the application as environment variables. Follow the principles of least privilege and secure access to the AWS account to keep these secrets secure. Only allow need to know access to the IAM roles necessary to access this information. An integration with AWS Secrets Manager would be a well-documented additional layer of improvement from here.
- High availability: This approach is about one and a half steps away from high-availability.
  - By enabling multi-AZ capability on RDS, it becomes high availability
  - Elasticsearch is running on only one AZ, representing a single point of failure. Replicating this would be a tricky endeavor using ECS. A managed Elasticsearch service would be an option for an organization that found it needed a highly available Elasticsearch. The connection string could easily be configured on the app container.
- Additional ALB access logs could be added by creating an additional S3 bucket and configuring the ALB to write logs to it.

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

- Choose an AWS region that best serves your userbase and 2 availability zones within that region you want to deploy your app to. For example, select the `us-west-2` region with AZs `us-west-2a` and `us-west-2b`. This will be used in the Terraform configuration `config.auto.tfvars.json`.

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

## Configure your domain name and SSL certificate

- Buy a domain at your registrar of choice (you can use AWS Route53 for easiest setup), or identify a subdomain that you want to direct to the OSMT app
- Note: to serve the app at the root of a domain, your registrar needs to support the `ALIAS` record type. Route53 supports this, but some registrars do not.
- Create a hosted zone matching your domain name. Add a description, and choose type public. If you used Route53 to register, this will be created automatically
- Configure certificate: go to AWS Certificate manager in the console. Request certificate -> Public. Follow any instructions to verify your domain name.
  - Add your domain name (and `*.yourdomainname.net`) to the domain names. DNS validation, default key algorithm.
  - Go into certificate (probably pending). Click Create Records in Route53
  - Copy the ARN from the certificate detail page. Paste it into `config.auto.tfvars.json` as the value for `config.alb.certificate_arn`

## Generate configuration files

To get the app ready to deploy into your AWS account, generate settings files from templates and populate them with your custom environment settings.

- In the `terraform/invocation` directory, copy the template file `main.tf.tpl` to `main.tf`.
- In the `terraform/invocation` directory, copy the template file `config.auto.tfvars.json.tpl` to `config.auto.tfvars.json`.
  - Customize `config.alb.certificate_arn` with the ARN of the certificate you generated for your domain.
- In the `terraform/invocation` directory, copy the template file `secrets.auto.tfvars.json.tpl` to `secrets.auto.tfvars.json`.
  - Fill out values including your OKTA.
- Input correct values for each template variable in `main.tf` (see comments in the file for details).
  - Include the bucket ID and table information from the step above.
- Generate `config.auto.tfvars.json` and `secrets.auto.tfvars.json` in the same directory.
- Begin populating templates with data.

## Run terraform to create cloud resources

- The goal of the user is to generate terraform code starting point (`main.tf`) that is ready to run against their AWS account.
- Terraform needs to look at your config before it's able to do fancy terraform stuff, like generating "var.config" from stored state.
- Navigate to `/infra/terraform/invocation`. Run command `terraform init`. Troubleshoot any errors that appear (typically means missing or incorrect configuration)
- Observe resources to be created: `terraform plan`
- Apply plan: `terraform apply`

Observe the resources being created in your AWS Account. This will result in a version of the application

## Cleanup: Take down the infrastructure (don't pay for something you're not using)

For test environments, it's important to not pay for resources you're not using. Here is how to turn off your AWS resources:

- Remove `enable_deletion_protection` (e.g. in `config.auto.tfvars.json`)
- Invoke terraform to tear down resources: `terraform destroy`
