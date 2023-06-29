locals {
    identity_prefix = "osmt_${var.config.env}"  // TODO see if we never use this
    identity-prefix = "osmt-${var.config.env}"

    identity_prefix_path = "/osmt/${var.config.env}"
}
