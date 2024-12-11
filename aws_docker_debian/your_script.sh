#!/bin/bash

json=$1

bucket_name=$(echo "$json" | jq -r '.bucketName')
object_key=$(echo "$json" | jq -r '.objectKey')
angle=$(echo "$json" | jq -r '.angle')

if [ -z "$angle" ]; then
    angle=0
fi

if [ -z "$bucket_name" ] || [ -z "$object_key" ]; then
    echo "Error: Missing bucketName or objectKey"
    exit 1
fi

echo "Bucket Name: $bucket_name"
echo "Object Key: $object_key"
echo "Angle: $angle"

python3 rotate.py "$bucket_name" "$object_key" "$angle"
