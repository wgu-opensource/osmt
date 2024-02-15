{
    "config": {
        "alb": {
            "container_name": "app",
            "enable_deletion_protection": true,
            "certificate_arn": "<Copy from Route53, the certificate for your domain>"
        },
        "ecs": {
            "task": {
                "app": {
                  "cpu": 2048,
                  "desired_count": 2,
                  "docker_image": "wguopensource/osmt-app",
                  "docker_tag": "latest",
                  "memory": 8192  
                },
                "elasticsearch": {
                  "cpu": 2048,
                  "desired_count": 1,
                  "docker_tag": "8.11.3",
                  "docker_image": "docker.elastic.co/elasticsearch/elasticsearch",
                  "memory": 4096
                }
            }
        },
        "env": "test",
        "elasticache": {
            "engine_version": "6.x",
            "node_type": "cache.t3.small",
            "parameter_group_name": "default.redis6.x"
        },
        "rds": {
            "db_name": "osmt",
            "engine_version": "8.0.32",
            "instance_class": "db.t3.small",
            "multi_az": true,
            "parameter_group_family": "mysql8.0",
            "skip_final_snapshot":  false
        },
        "vpc": {
            "azs": [
                "us-west-2a",
                "us-west-2b"
            ],
            "subnets": {
                "private": [
                    "192.168.100.0/27",
                    "192.168.100.64/27"
                ],
                "public": [
                    "192.168.100.128/27",
                    "192.168.100.192/27"
                ]
            },
            "vpc_cidr": "192.168.100.0/24"
        }
    } 
}
