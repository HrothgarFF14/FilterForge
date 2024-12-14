package lambda;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import saaf.Inspector;
import saaf.Response;

public class RotateMain implements RequestHandler<HashMap<String, Object>, HashMap<String, Object>> {

    @Override
    public HashMap<String, Object> handleRequest(HashMap<String, Object> request, Context context) {
        LambdaLogger logger = context.getLogger();
        Inspector inspector = new Inspector();
        inspector.inspectAll();
        Response response = new Response();

        try {
            // Validate input parameters
            String bucketName = (String) request.get("bucketName");
            String objectKey = (String) request.get("objectKey");
            Object angleObj = request.get("angle");
            Double angle = (angleObj instanceof Number) ? ((Number) angleObj).doubleValue() : 0.0;


            if (bucketName == null || bucketName.isEmpty() || objectKey == null || objectKey.isEmpty() || angle == null) {
                return sendError(inspector, "Invalid input parameters", response);
            }

            // Initialize S3 client
            AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

            logger.log("Downloading image from S3...");
            S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, objectKey));
            InputStream objectContent = s3Object.getObjectContent();

            // Read image
            BufferedImage image = ImageIO.read(objectContent);
            objectContent.close();

            logger.log("Rotating image...");
            BufferedImage rotatedImage = rotateImage(image, angle);

            // Stream back to S3
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(rotatedImage, "png", outputStream);
            byte[] rotatedImageBytes = outputStream.toByteArray();
            outputStream.close();

            logger.log("Uploading rotated image to S3...");
            InputStream inputStream = new ByteArrayInputStream(rotatedImageBytes);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(rotatedImageBytes.length);
            metadata.setContentType("image/png");

            String rotatedObjectKey = "rotated-" + objectKey;
            s3Client.putObject(new PutObjectRequest(bucketName, rotatedObjectKey, inputStream, metadata));
            inputStream.close();

            String rotatedImageUri = String.format("s3://%s/%s", bucketName, rotatedObjectKey);
            logger.log("Rotated image uploaded to: " + rotatedImageUri);

            response.setValue(rotatedImageUri);
            inspector.addAttribute("rotatedImageUri", rotatedImageUri);
            inspector.consumeResponse(response);

        } catch (IOException e) {
            logger.log("Error: " + e.getMessage());
            return sendError(inspector, e.getMessage(), response);
        }

        inspector.inspectAllDeltas();
        return inspector.finish();
    }

    public static BufferedImage rotateImage(BufferedImage image, double angle) {
        int width = image.getWidth();
        int height = image.getHeight();
    
        // Calculate the new dimensions for the rotated image
        double radians = Math.toRadians(angle);
        int newWidth = (int) Math.abs(width * Math.cos(radians)) + (int) Math.abs(height * Math.sin(radians));
        int newHeight = (int) Math.abs(height * Math.cos(radians)) + (int) Math.abs(width * Math.sin(radians));
    
        // Create a new BufferedImage with the adjusted dimensions
        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotatedImage.createGraphics();
    
        // Move the origin to the center of the new canvas
        g2d.translate((newWidth - width) / 2.0, (newHeight - height) / 2.0);
    
        // Apply the rotation around the center of the original image
        g2d.rotate(radians, width / 2.0, height / 2.0);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
    
        return rotatedImage;
    }
    

    public static HashMap<String, Object> sendError(Inspector inspector, String message, Response response) {
        response.setValue("errorMessage: " + message);
        inspector.addAttribute("errorMessage", message);
        inspector.consumeResponse(response);
        return inspector.finish();
    }

    public static void main(String[] args) {
        Context context = new Context() {
            @Override
            public String getAwsRequestId() { return ""; }
            @Override public String getLogGroupName() { return ""; }
            @Override public String getLogStreamName() { return ""; }
            @Override public String getFunctionName() { return ""; }
            @Override public String getFunctionVersion() { return ""; }
            @Override public String getInvokedFunctionArn() { return ""; }
            @Override public CognitoIdentity getIdentity() { return null; }
            @Override public ClientContext getClientContext() { return null; }
            @Override public int getRemainingTimeInMillis() { return 0; }
            @Override public int getMemoryLimitInMB() { return 0; }
            @Override public LambdaLogger getLogger() {
                return (message) -> System.out.println("LOG: " + message);
            }
        };

        RotateMain rotateMain = new RotateMain();
        HashMap<String, Object> request = new HashMap<>();
        request.put("bucketName", "example-bucket");
        request.put("objectKey", "example.png");
        request.put("angle", 45.0);

        HashMap<String, Object> response = rotateMain.handleRequest(request, context);
        System.out.println("Response: " + response);
    }
}
