import handler
import json
from io import BytesIO
from PIL import Image
from Inspector import Inspector

s3_client = boto3.client('s3')

#
# AWS Lambda Functions Default Function
#
# This hander is used as a bridge to call the platform neutral
# version in handler.py. This script is put into the scr directory
# when using publish.sh.
#
# @param request
#
def lambda_handler(event, context):
    
        try:
        # Extract S3 bucket details and rotation angle from request
        bucket_name = request.get("bucketName")
        object_key = request.get("objectKey")
        angle = request.get("angle", 0)
        
        if not bucket_name or not object_key:
            raise ValueError("Bucket name or object key not provided")
        if not angle:
            raise ValueError("Invalid angle provided")

        # Initialize S3 client
        s3_client = boto3.client('s3')

        # Download the image from S3
        s3_object = s3_client.get_object(Bucket=bucket_name, Key=object_key)
        img_bytes = s3_object['Body'].read()
        image = Image.open(BytesIO(img_bytes))

        # Rotate the image
        image = image.rotate(-float(angle), expand=True)

        # Save the rotated image to a buffer
        buffer = BytesIO()
        format = image.format or "PNG"  # Infer format if not present
        image.save(buffer, format=format)
        buffer.seek(0)

        # Upload the rotated image back to S3 with a new key
        rotated_key = f"rotated-{object_key}"
        s3_client.upload_fileobj(
            buffer,
            Bucket=bucket_name,
            Key=rotated_key,
            ExtraArgs={"ContentType": f"image/{format.lower()}"}
        )

        # Add result to response
        rotated_image_uri = f"s3://{bucket_name}/{rotated_key}"
        inspector.addAttribute("rotatedImageUri", rotated_image_uri)
        response["rotatedImageUri"] = rotated_image_uri
    
    except Exception as e:
        error_message = f"Error: {str(e)}"
        inspector.addAttribute("errorMessage", error_message)
        response["errorMessage"] = error_message
    
    inspector.inspectAllDeltas()
    return inspector.finish()
    