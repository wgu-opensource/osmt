data "aws_iam_policy_document" "assume_role_ecs_task" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}


resource "aws_iam_role" "ecs_task_execution" {
  name               = "${local.identity_prefix}_ecs_task_execution"
  assume_role_policy = data.aws_iam_policy_document.assume_role_ecs_task.json
}


resource "aws_iam_role_policy_attachment" "ecs_task_execution" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = data.aws_iam_policy.ecs_task_execution.arn
}
