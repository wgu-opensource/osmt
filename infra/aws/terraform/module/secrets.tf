variable "secrets" {
  type = object({
    rds = object({
      master_password = string
      master_username = string
    })
    app = object({
      base_domain           = string
      environment           = string
      frontend_url          = string
      migrations_enabled    = string
      oauth_issuer          = string
      oauth_audience        = string
      oauth_clientid        = string
      oauth_secret          = string
      reindex_elasticsearch = string
      skip_metadata_import  = string
    })
  })
}
