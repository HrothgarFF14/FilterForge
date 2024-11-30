#!/bin/bash

# Check if the correct number of arguments is provided
if [ "$#" -ne 7 ]; then
    echo "Usage: $0 <bucketname> <filename> <x> <y> <width> <height> <outputFilename>"
    exit 1
fi

# Assign arguments to variables
bucketname=$1
filename=$2
x=$3
y=$4
width=$5
height=$6
outputFilename=$7

# Upload the image to S3
aws s3 cp "$filename" "s3://$bucketname/$filename"

# Create the JSON payload
json=$(jq -n --arg bucketname "$bucketname" --arg filename "$filename" --arg outputFilename "$outputFilename" --arg x "$x" --arg y "$y" --arg width "$width" --arg height "$height" \
    '{bucketname: $bucketname, filename: $filename, outputFilename: $outputFilename, x: ($x|tonumber), y: ($y|tonumber), width: ($width|tonumber), height: ($height|tonumber)}')

# Print the JSON payload for debugging
echo "JSON Payload: $json"

# Invoke the Lambda function
echo "Invoking Lambda function using AWS CLI..."
response=$(aws lambda invoke --invocation-type RequestResponse --function-name filterForge-java-crop --region us-east-1 --payload "$json" response.json)

# Check if the invocation was successful
if [ $? -ne 0 ]; then
    echo "Error: Failed to invoke Lambda function."
    exit 1
fi

echo "Lambda function invoked successfully. Check the output in S3 bucket."