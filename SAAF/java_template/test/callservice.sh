#!/bin/bash

# Define the API Gateway URL for your Lambda function
API_URL="https://eci2oj1ks7.execute-api.us-east-1.amazonaws.com/resize-test"

# Define the image path
IMAGE_PATH="/home/maliha/FilterForge/SAAF/java_template/test/OIG1.jpeg"

# Base64 encode the image
BASE64_IMAGE=$(base64 "$IMAGE_PATH")

# Create the JSON object
json="{\"imageString\":\"$BASE64_IMAGE\",\"width\":300,\"height\":200}"

# Print a message indicating the function will be invoked
echo "Invoking ResizeImage Lambda function using API Gateway"

# Use echo and pipe to pass the JSON to curl to avoid "Argument list too long" error
time output=$(echo "$json" | curl -s -H "Content-Type: application/json" -X POST -d @- $API_URL)

# Print the JSON result from the Lambda function
echo ""
echo "JSON RESULT:"
echo "$output" | jq

