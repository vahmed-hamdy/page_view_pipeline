resource "docker_network" "pipeline_network" {
  name = "pipeline_network"
}

resource "docker_container" "zookeeper" {
  image = "wurstmeister/zookeeper:latest"
  name  = "zookeeper"
  networks_advanced {
    name = docker_network.pipeline_network.name
  }

  env = [
    "ZOOKEEPER_CLIENT_PORT=2181",
    "ALLOW_ANONYMOUS_LOGIN=yes"
  ]
}

resource "docker_container" "kafka" {
  image = "wurstmeister/kafka:latest"
  name  = "kafka"
  networks_advanced {
    name = docker_network.pipeline_network.name
  }

  depends_on = [docker_container.zookeeper]

  env = [
    # TODO: Add SASL_AUTHENTICATION Paramters here
    "KAFKA_NODE_ID=1",
    "KAFKA_BROKER_ID=1",
    "KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181",
    "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT",    
   "KAFKA_ADVERTISED_LISTENERS=INSIDE://kafka:9093,OUTSIDE://localhost:9092",
    "KAFKA_LISTENERS=INSIDE://0.0.0.0:9093,OUTSIDE://0.0.0.0:9092",
    "KAFKA_INTER_BROKER_LISTENER_NAME=INSIDE",
    # TODO: Parametrize paralellism
    "KAFKA_CREATE_TOPICS=${var.kafka_topic}:1:1",
    # "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,SASL_PLAINTEXT:SASL_PLAINTEXT",



    # "KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092,SASL_PLAINTEXT://localhost:9093",
    # "KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092,SASL_PLAINTEXT://0.0.0.0:9093",
    # "KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT",
    # "KAFKA_SASL_ENABLED_MECHANISMS=SCRAM-SHA-256,SCRAM-SHA-512",
    # "KAFKA_SASL_MECHANISM_INTER_BROKER_PROTOCOL=SCRAM-SHA-256",
    # "KAFKA_LISTENER_NAME_SASL_PLAINTEXT_SCRAM_SHA_256_SASL_JAAS_CONFIG=org.apache.kafka.common.security.scram.ScramLoginModule required username=\"kafkaadmin\" password=\"kafkapassword\";",
    # "KAFKA_LISTENER_NAME_SASL_PLAINTEXT_SCRAM_SHA_512_SASL_JAAS_CONFIG=org.apache.kafka.common.security.scram.ScramLoginModule required username=\"kafkaadmin\" password=\"kafkapassword\";"
    ]
}

resource "docker_container" "generator" {
    image = var.generator_image
    name  = "generator"
    networks_advanced {
        name = docker_network.pipeline_network.name
    }

    depends_on = [ docker_container.kafka ]
    env = [
        "KAFKA_BOOTSTRAP_SERVER=kafka:${var.kafka_network_port}",
        "KAFKA_TOPIC=${var.kafka_topic}",
        "THROUGHPUT=${var.generator_throughput}",
    ]

    volumes {
        container_path = "/etc/checkout/generator/generator.properties"
        host_path      = abspath("${path.module}/config/generator.properties")
    }

}

resource "docker_container" "flink-taskmanager" {
    image = var.flink_image
    name = "flink-taskmanager"
    depends_on = [ docker_container.kafka , docker_container.flink-jobmanager]
    networks_advanced {
        name = docker_network.pipeline_network.name
    }

    env = [ 
        "FLINK_PROPERTIES=${local.flink_properties}",
        "SOURCE_KAFKA_TOPIC=${var.kafka_topic}",
        "SINK_FILE_PATH=${var.preprocess_output_dir}"
     ]

    volumes {
       container_path = "/opt/flink/conf/flink-conf.yaml"
       host_path      = local.flink_properties_file
    }
    
    volumes {
      container_path = "/tmp/flink-checkpoints"
       host_path      = var.checkpoint_dir
    }

    volumes {
      container_path = "/tmp/flink-savepoints"
       host_path      = var.savepoint_dir
    }

    volumes {
      container_path = "/tmp/flink-savepoints"
       host_path      = var.savepoint_dir
    }

    volumes {
        container_path = "/tmp/checkout/preprocess-output"
        host_path      = var.preprocess_output_dir
    }

    volumes {
        container_path = "/tmp/checkout/processed-output"
        host_path      = var.processed_output_dir
    }

     command = [ 
        "taskmanager"
      ]
}

resource "docker_container" "flink-jobmanager" {
    image = var.flink_job_image
    name  = "flink-jobmanager"
    networks_advanced {
        name = docker_network.pipeline_network.name
    }

     env = [ 
        "FLINK_PROPERTIES=${local.flink_properties}",
        "SOURCE_KAFKA_TOPIC=${var.kafka_topic}",
        "SINK_FILE_PATH=${var.preprocess_output_dir}",
        "PARALLELISM=${var.parallelism}"
     ]

    depends_on = [ docker_container.kafka ]
    
    volumes {
       container_path = "/opt/flink/conf/flink-conf.yaml"
       host_path      = local.flink_properties_file
    }

    ports {
        internal = 8081
        external = var.flink_dashboard_port
    }
    ports {
        internal = 9249
        external = 9249
    }

    command = [
        "standalone-job", "--job-classname", "${var.flink_job_entry_class}", "--jars", "/opt/flink/artifacts/${var.flink_job_jar}"
    ]
}

resource "docker_container" "prometheus" {
  image = "prom/prometheus"
  name  = "prometheus"
 networks_advanced {
    name = docker_network.pipeline_network.name
  }
  depends_on = [ docker_container.flink-jobmanager, docker_container.flink-taskmanager, docker_container.kafka, docker_container.generator ]

  volumes {
    host_path      = abspath("${path.module}/config/prometheus.yml")
    container_path = "/etc/prometheus/prometheus.yml"
  }

  volumes {
    container_path = "/etc/prometheus/flink.rules.yml"
    host_path      = abspath("${path.module}/config/flink.rules.yml")
  }
  ports {
    internal = 9090
    external = 9090
  }
}


output "flink_dashboard_url" {
  value = "http://localhost:${var.flink_dashboard_port}"
}

output "promehteus_url" {
  value = "http://localhost:9090/alerts"
}