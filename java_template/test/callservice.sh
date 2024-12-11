#!/bin/bash

# Define the variables
inputBucket="filterforge-uploads"
outputBucket="filterforge-uploads"
filename="med-img.png"
x=0
y=0
width=100
height=100
outputFilename="myCroppedMedImg.png"

# JSON object to pass to Lambda Function
json=$(jq -n --arg inputBucket "$inputBucket" --arg outputBucket "$outputBucket" --arg filename "$filename" --arg x "$x" --arg y "$y" --arg width "$width" --arg height "$height" --arg outputFilename "$outputFilename" \
    '{inputBucket: $inputBucket, outputBucket: $outputBucket, filename: $filename, x: ($x|tonumber), y: ($y|tonumber), width: ($width|tonumber), height: ($height|tonumber), outputFilename: $outputFilename}')

echo "Invoking Lambda function using API Gateway"
time output=$(aws lambda invoke --invocation-type RequestResponse --cli-binary-format raw-in-base64-out --function-name filterForge-java-crop --region us-east-1 --payload "$json" /dev/stdout | head -n 1 | head -c -2 ; echo)

echo ""
echo "JSON RESULT:"
echo "$output" | jq