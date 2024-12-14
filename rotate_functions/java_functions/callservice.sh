#!/bin/bash
# JSON object to pass to Lambda Function
json=$(cat <<EOF
{
  "bucketName": "filter-forge-cnlwebber",
  "objectKey": "med-img.png",
  "angle": 90.5
}
EOF
)

echo "Invoking Rotate Lambda function using API Gateway"
time output=$(curl -s -H "Content-Type: application/json" -X POST -d "$json" https://issj53f3pi.execute-api.us-east-1.amazonaws.com/rotate-deploy-final/)
echo ""
echo "JSON RESULT:"
echo "$output"
echo ""