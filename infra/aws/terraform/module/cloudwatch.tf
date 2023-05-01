resource "aws_cloudwatch_log_group" "app" {
  name = local.identity-prefix
}

resource "aws_cloudwatch_log_group" "elasticsearch" {
  name = local.identity-prefix
}
