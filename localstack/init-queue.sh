#!/usr/bin/env bash
set -euo pipefail

echo "[localstack] creating SQS queue + DLQ..."

QUEUE_NAME="${APP_SQS_QUEUE_NAME:-cliptozip-notifications}"
DLQ_NAME="${QUEUE_NAME}-dlq"

# Create DLQ
awslocal sqs create-queue --queue-name "$DLQ_NAME" >/dev/null
DLQ_URL="$(awslocal sqs get-queue-url --queue-name "$DLQ_NAME" --query 'QueueUrl' --output text)"
DLQ_ARN="$(awslocal sqs get-queue-attributes --queue-url "$DLQ_URL" --attribute-names QueueArn --query 'Attributes.QueueArn' --output text)"

# Create main queue (attributes applied after to avoid CLI quoting issues)
awslocal sqs create-queue --queue-name "$QUEUE_NAME" >/dev/null
QUEUE_URL="$(awslocal sqs get-queue-url --queue-name "$QUEUE_NAME" --query 'QueueUrl' --output text)"

# Set RedrivePolicy via file input (more robust across shells/CLI versions)
cat > /tmp/sqs-attrs.json <<EOF
{
  "RedrivePolicy": "{\"deadLetterTargetArn\":\"$DLQ_ARN\",\"maxReceiveCount\":\"3\"}"
}
EOF

awslocal sqs set-queue-attributes --queue-url "$QUEUE_URL" --attributes file:///tmp/sqs-attrs.json >/dev/null

echo "[localstack] done."
