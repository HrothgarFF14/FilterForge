import boto3
from PIL import Image
from io import BytesIO

# Create an S3 client
s3 = boto3.client('s3')

def lambda_handler(event, context):
    try:
        # Parse input from the event
        source_bucket = event['source_bucket']
        source_key = event['source_key']
        destination_key = event['destination_key']
        width = event['width']  # New: Desired width
        height = event['height']  # New: Desired height

        # Download the image from S3
        response = s3.get_object(Bucket=source_bucket, Key=source_key)
        img_data = response['Body'].read()

        # Open the image
        img = Image.open(BytesIO(img_data))

        # Resize the image
        resized_img = img.resize((width, height))

        # Save the processed image to a buffer
        buffer = BytesIO()
        resized_img.save(buffer, format=img.format)
        buffer.seek(0)

        # Upload the image back to the S3 bucket
        s3.put_object(Bucket=source_bucket, Key=destination_key, Body=buffer)

        return {
            'statusCode': 200,
            'body': f"Image successfully resized to {width}x{height} and saved as {destination_key} in bucket {source_bucket}"
        }

    except Exception as e:
        return {
            'statusCode': 500,
            'body': f"Error processing image: {str(e)}"
        }
