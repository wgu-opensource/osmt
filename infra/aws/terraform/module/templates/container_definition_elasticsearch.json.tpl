[{
    "cpu": ${cpu},
    "environment": [
        {
            "name": "discovery.type",
            "value": "single-node"
        },
        {
            "name": "net",
            "value": "host"
        },
        {
            "name": "xpack.security.enabled",
            "value": "false"
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
    "memory": ${memory},
    "name": "${container_name}",
    "portMappings": [
        {
            "containerPort": ${port}
        }
    ]
}]
