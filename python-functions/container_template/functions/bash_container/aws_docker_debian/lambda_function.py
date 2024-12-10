import boto3
import os
from PIL import Image, ImageFilter
from io import BytesIO
from SAAF import Inspector
import json

s3 = boto3.client('s3')

def lambda_handler(event, context):
    try:
        inspector = Inspector()
        inspector.inspectAll()

        # Parse input from the event
        source_bucket = event['source_bucket']
        source_key = event['source_key']
        destination_key = event['destination_key']
        
        # Download the image from S3
        response = s3.get_object(Bucket=source_bucket, Key=source_key)
        img_data = response['Body'].read()
        
        # Open the image
        img = Image.open(BytesIO(img_data))
        
        # Apply Gaussian blur
        blurred_img = img.filter(ImageFilter.GaussianBlur(radius=5))
        
        # Save the processed image to a buffer
        buffer = BytesIO()
        blurred_img.save(buffer, format=img.format)
        buffer.seek(0)
        
        # Upload the image back to the S3 bucket
        s3.put_object(Bucket=source_bucket, Key=destination_key, Body=buffer)
        
        return inspector.finish()
    
    except Exception as e:
        return {
            'statusCode': 500,
            'body': f"Error processing image: {str(e)}"
        }
