import json
import logging
import boto3
from PIL import Image
from io import BytesIO
from Inspector import Inspector

def lambda_handler(request, context):
    inspector = Inspector()
    inspector.inspectAll()

    s3_client = boto3.client('s3')

    try:
        # Extract S3 details from the request
        input_bucket = request.get('inputBucket')
        output_bucket = request.get('outputBucket')
        filename = request.get('filename')
        output_filename = request.get('outputFilename')
        x = int(request.get('x'))
        y = int(request.get('y'))
        width = int(request.get('width'))
        height = int(request.get('height'))

        if not input_bucket or not output_bucket or not filename or not output_filename:
            raise ValueError("Missing required S3 parameters: inputBucket, outputBucket, filename, outputFilename")

        # Download the image from S3
        input_image = download_image_from_s3(s3_client, input_bucket, filename)

        # Crop the image
        cropped_image = crop_image(input_image, x, y, width, height)

        # Upload the processed image back to S3
        upload_image_to_s3(s3_client, cropped_image, output_bucket, output_filename, "png")

        inspector.addAttribute("message", f"Image processed and stored at s3://{output_bucket}/{output_filename}")
    except Exception as e:
        inspector.addAttribute("error", f"Error processing image: {str(e)}")

    inspector.inspectAllDeltas()
    return inspector.finish()

def download_image_from_s3(s3_client, bucket, key):
    response = s3_client.get_object(Bucket=bucket, Key=key)
    return Image.open(BytesIO(response['Body'].read()))

def upload_image_to_s3(s3_client, image, bucket, key, format):
    buffer = BytesIO()
    image.save(buffer, format)
    buffer.seek(0)
    s3_client.put_object(Bucket=bucket, Key=key, Body=buffer, ContentType=f'image/{format}')

def crop_image(image, x, y, width, height):
    return image.crop((x, y, x + width, y + height))