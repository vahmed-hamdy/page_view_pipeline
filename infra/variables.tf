variable "kafka_topic" {
    description = "kafka topic to use for data ingestion and processing"
    type        = string
    default     = "page-user-views"
}

variable "kafka_network_port" {
    description = "kafka network port"
    type        = number
    default     = 9093
}

variable "generator_image" {
    description = "Generator image to use for the data generator"
    type        = string
    default     = "generator:latest"
  
}

variable "flink_image" {
    description = "Flink image to use for the job manager and task manager"
    type        = string
    default     = "apache/flink:1.19.1-scala_2.12-java11"
}

variable "flink_job_image" {
    description = "Flink image to use for the job"
    type        = string
    default     = "flink-processor:latest"  
}

variable "flink_job_entry_class" {
    description = "Flink job entry class"
    type        = string
    default     = "com.checkout.flink.FlinkRunner"
}

variable "flink_job_jar" {
    description = "Flink job jar"
    type        = string
    default     = "flink-job-1.0-SNAPSHOT.jar"
  
}

variable "checkpoint_dir" {
    description = "Flink checkpoint directory"
    type        = string
    default     = "/tmp/flink-checkpoints"
}


variable "savepoints_dir" {
    description = "Flink savepoints directory"
    type        = string
    default     = "/tmp/flink-savepoints"
}

variable "preprocess_output_dir" {
    description = "Preprocess output directory"
    type        = string
    default     = "/tmp/checkout/preprocess-output"
}

variable "processed_output_dir" {
    description = "Processed output directory"
    type        = string
    default     = "/tmp/checkout/processed-output"
  
}

variable "flink_dashboard_port" {
    description = "Flink dashboard port"
    type        = number
    default     = 8081
}

locals {
    flink_properties = "jobmanager.rpc.address: flink-jobmanager\ntaskmanager.numberOfTaskSlots: 2"
    flink_properties_file = abspath("${path.module}/config/flink.conf")
    bootstrap_servers = "kafka:${var.kafka_network_port}"
}