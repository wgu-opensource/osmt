{
    "cpu": ${cpu},
    "environment": [
        {
            "name": "BASE_DOMAIN",
            "value": "${base_domain}"
        },
        {
            "name": "DB_NAME",
            "value": "${db_name}"
        },
        {
            "name": "DB_PASSWORD",
            "value": "${db_password}"
        },
        {
            "name": "DB_URI",
            "value": "${db_user}:${db_password}@${rds_endpoint}:3306"
        },
        {
            "name": "DB_USER",
            "value": "${db_user}"
        },
        {
            "name": "ENVIRONMENT",
            "value": "${environment}"
        },
        {
            "name": "FRONTEND_URL",
            "value": "${frontend_url}"
        },
        {
            "name": "ELASTICSEARCH_URI",
            "value": "${elasticsearch_uri}"
        },
        {
            "name": "OAUTH_ISSUER",
            "value": "${oauth_issuer}"
        },
        {
            "name": "OAUTH_CLIENTID",
            "value": "${oauth_clientid}"
        },
        {
            "name": "OAUTH_CLIENTSECRET",
            "value": "${oauth_secret}"
        },
        {
            "name": "OAUTH_AUDIENCE",
            "value": "${oauth_audience}"
        },
        {
            "name": "REDIS_URI",
            "value": "${redis_uri}"
        },
        {
            "name": "MIGRATIONS_ENABLED",
            "value": "${migrations_enabled}"
        },
        {
            "name": "REINDEX_ELASTICSEARCH",
            "value": "${reindex_elasticsearch}"
        },
        {
            "name": "SKIP_METADATA_IMPORT",
            "value": "${skip_metadata_import}"
        }
    ],
    "image": "${docker_image}:${docker_tag}",
    "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
            "awslogs-group": "${log_group}",
            "awslogs-region": "${aws_region}",
            "awslogs-stream-prefix": "${log_stream_prefix}"
        }
    },
    "memory": "${memory}",
    "name": "{container_name}",
    "portMappings": [
        {
            "containerPort": ${port}
        }
    ]
}