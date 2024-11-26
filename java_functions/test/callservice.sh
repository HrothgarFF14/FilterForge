#!/bin/bash
# JSON object to pass to Lambda Function
json=$(cat <<EOF
{
  "inputBucket": "gaussian.blur.bucket.test.jca",
  "inputKey": "inputFile.jpg",
  "outputBucket": "gaussian.blur.bucket.test.jca",
  "outputKey": "blurred-image-java.jpg",
  "kernelSize": 5,
  "sigma": 1.5
}
EOF
)

echo "Invoking Gaussian Blur Lambda function using API Gateway"
time output=$(curl -s -H "Content-Type: application/json" -X POST -d "$json" https://lyc84m53u8.execute-api.us-east-1.amazonaws.com/guass-test/java)
echo ""
echo "JSON RESULT:"
echo "$output" | jq
echo ""

