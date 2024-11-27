#!/bin/bash

# Check if the correct number of arguments is provided
if [ "$#" -ne 6 ]; then
    echo "Usage: $0 <imagePath> <x> <y> <width> <height> <outputPath>"
    exit 1
fi

# Assign arguments to variables
imagePath=$1
x=$2
y=$3
width=$4
height=$5
outputPath=$6

# Read the image file and encode it to base64
base64Image=$(base64 "$imagePath")

# Create the JSON payload
payload=$(jq -n --arg img "$base64Image" --arg x "$x" --arg y "$y" --arg width "$width" --arg height "$height" \
    '{image: $img, x: ($x|tonumber), y: ($y|tonumber), width: ($width|tonumber), height: ($height|tonumber)}')

# Invoke the Lambda function
response=$(aws lambda invoke --function-name filterForge-java-crop --payload "$payload" response.json)

# Check if the invocation was successful
if [ $? -ne 0 ]; then
    echo "Error: Failed to invoke Lambda function."
    exit 1
fi

# Extract the base64 encoded cropped image from the response
croppedImageBase64=$(jq -r '.croppedImage' response.json)

# Decode the base64 image and save it to the output path
echo "$croppedImageBase64" | base64 --decode > "$outputPath"

echo "Cropped image saved to $outputPath"