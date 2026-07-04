#!/bin/sh
set -e

echo "Creating Kafka topics..."

/opt/kafka/bin/kafka-topics.sh --create --if-not-exists \
  --topic expense-created \
  --bootstrap-server kafka:9092 \
  --partitions 1 \
  --replication-factor 1

/opt/kafka/bin/kafka-topics.sh --create --if-not-exists \
  --topic budget-alert \
  --bootstrap-server kafka:9092 \
  --partitions 1 \
  --replication-factor 1

echo "Topics created successfully"
/opt/kafka/bin/kafka-topics.sh --list --bootstrap-server kafka:9092