#cloud_function(platforms=[Platform.AWS], memory=512, config=config)

def rotate(request, context):
    import json
    import base64
    from io import BytesIO
    from PIL import Image
    from Inspector import Inspector
    import boto3
    import re

    # Initialize the inspector
    inspector = Inspector()
    inspector.inspectAll()

    response = {}

    try:
        bucket_name = request.get("bucketName")
        object_key = request.get("objectKey")
        angle = request.get("angle", 0)
        
        if not bucket_name or not object_key:
            raise ValueError("Bucket name or object key not provided")
        if not angle:
            raise ValueError("Invalid angle provided")

        s3_client = boto3.client('s3')

        s3_object = s3_client.get_object(Bucket=bucket_name, Key=object_key)
        img_bytes = s3_object['Body'].read()
        image = Image.open(BytesIO(img_bytes))

        image = image.rotate(-float(angle), expand=True)

        buffer = BytesIO()
        format = image.format or "PNG"
        image.save(buffer, format=format)
        buffer.seek(0)

        rotated_key = f"rotated-{object_key}"
        s3_client.upload_fileobj(
            buffer,
            Bucket=bucket_name,
            Key=rotated_key,
            ExtraArgs={"ContentType": f"image/{format.lower()}"}
        )

        rotated_image_uri = f"s3://{bucket_name}/{rotated_key}"
        inspector.addAttribute("rotatedImageUri", rotated_image_uri)
        response["rotatedImageUri"] = rotated_image_uri
    
    except Exception as e:
        error_message = f"Error: {str(e)}"
        inspector.addAttribute("errorMessage", error_message)
        response["errorMessage"] = error_message
    
    inspector.inspectAllDeltas()
    return inspector.finish()
