$queueUrl = "http://localhost:4566/000000000000/cliptozip-notifications"

# Reads the JSON as a raw string (preserves double quotes)
$body = Get-Content .\msg.json -Raw

# IMPORTANT: do NOT wrap $body in double quotes again, otherwise PowerShell can mangle the JSON.
docker exec -i localstack awslocal sqs send-message `
  --queue-url $queueUrl `
  --message-body $body

Write-Host "Sent message to $queueUrl"
