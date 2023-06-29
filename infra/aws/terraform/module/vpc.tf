module "vpc" {
  source = "terraform-aws-modules/vpc/aws"

  name = local.identity-prefix
  cidr = var.config.vpc.vpc_cidr

  azs             = var.config.vpc.azs
  private_subnets = var.config.vpc.subnets.private
  public_subnets  = var.config.vpc.subnets.public

  enable_nat_gateway = true
}
