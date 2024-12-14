package lambda;

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

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.UIManager;

/**
 * uwt.lambda_test::handleRequest
 *
 * @author Wes Lloyd
 * @author Robert Cordingly
 */
public class Rotate implements RequestHandler<HashMap<String, Object>, HashMap<String, Object>> {

    /**
     * Lambda Function Handler
     * 
     * @param request Hashmap containing request JSON attributes.
     * @param context 
     * @return HashMap that Lambda will automatically convert into JSON.
     */
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

BufferedImage rotateImage(BufferedImage image, double theta)
    {
        //  Determine the size of the rotated image

        double cos = Math.abs(Math.cos(theta));
        double sin = Math.abs(Math.sin(theta));
        double width  = image.getWidth();
        double height = image.getHeight();
        int w = (int)(width * cos + height * sin);
        int h = (int)(width * sin + height * cos);

        //  Rotate and paint the original image onto a BufferedImage

        BufferedImage out = new BufferedImage(w, h, image.getType());
        Graphics2D g2 = out.createGraphics();
        g2.setPaint(UIManager.getColor("Panel.background"));
        g2.fillRect(0,0,w,h);
        double x = w/2;
        double y = h/2;
        AffineTransform at = AffineTransform.getRotateInstance(theta, x, y);
        x = (w - width)/2;
        y = (h - height)/2;
        at.translate(x, y);
        g2.drawRenderedImage(image, at);
        g2.dispose();
        return out;
    }

    public static HashMap<String, Object> sendError(Inspector inspector, String message, Response response) {
        response.setValue("errorMessage: " + message);
        inspector.addAttribute("errorMessage", message);
        inspector.consumeResponse(response);
        return inspector.finish();
    }

}
