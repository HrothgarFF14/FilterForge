#!/bin/bash

# Define the variables
inputBucket="test.bucket.462562f24.mh"        # Replace with your input bucket name
outputBucket="test.bucket.462562f24.mh"      # Replace with your output bucket name
inputKey="Inputimage/med-img.png"                  # Replace with your input file name
outputKey="outputImage/resizedImage"         # Replace with your desired output file name

# Resize dimensions
targetWidth=60                         # Desired width of the resized image
targetHeight=60                        # Desired height of the resized image

# Construct JSON payload using jq
json=$(jq -n \
  --arg inputBucket "$inputBucket" \
  --arg inputKey "$inputKey" \
  --arg outputBucket "$outputBucket" \
  --arg outputKey "$outputKey" \
  --argjson targetWidth "$targetWidth" \
  --argjson targetHeight "$targetHeight" \
  '{
    inputBucket: $inputBucket,
    inputKey: $inputKey,
    outputBucket: $outputBucket,
    outputKey: $outputKey,
    targetWidth: $targetWidth,
    targetHeight: $targetHeight
  }')

echo $json|jq

echo "Invoking Lambda function using AWS CLI (Boto3)"
#time output=`aws lambda invoke --invocation-type RequestResponse --cli-binary-format raw-in-base64-out --function-name ResizeImage --region us-east-1>

time output=$(aws lambda invoke \
  --invocation-type RequestResponse \
  --cli-binary-format raw-in-base64-out \
  --function-name ResizeImage \
  --region us-east-1 \
  --payload "$json" \
  output.json)

# Print the result
echo ""
echo "Lambda Function Response:"
cat output.json | jq


