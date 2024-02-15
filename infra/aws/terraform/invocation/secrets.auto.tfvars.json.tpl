{
    "secrets": {
        "rds": {
            "master_password": "<mysql_8.0_password>",
            "master_username": "<mysql_8.0_username>"
        },
        "app": {
            "base_domain": "<e.g. base_domain.net>",
            "environment": "apiserver,oauth2-okta",
            "frontend_url": "<e.g. https://base_domain.net>",
            "oauth_issuer": "<oauth_issuer e.g. https://dev-82064468.okta.com/oauth2/default>",
            "oauth_clientid": "<oauch_clientid>",
            "oauth_secret": "<oauth_secret>>",
            "oauth_audience": "<oauth_audience>",
            "migrations_enabled": "true",
            "reindex_elasticsearch": "true",
            "skip_metadata_import": "false"
        }
    }
}
