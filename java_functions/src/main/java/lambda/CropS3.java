package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import saaf.Inspector;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

public class CropS3 implements RequestHandler<Request, HashMap<String, Object>> {

    public HashMap<String, Object> handleRequest(Request theRequest, Context theContext) {

        Inspector inspector = new Inspector();
        inspector.inspectAll();

        HashMap<String, Object> responseMap = new HashMap<>();
        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

            // Get the image from S3
            S3Object s3Object = s3Client.getObject(theRequest.getBucketname(), theRequest.getFilename());
            InputStream objectData = s3Object.getObjectContent();
            BufferedImage image = ImageIO.read(objectData);

            // Validate crop dimensions
            if (!validateCropDimensions(image, theRequest.getX(), theRequest.getY(), theRequest.getWidth(), theRequest.getHeight())) {
                throw new IllegalArgumentException("Invalid crop dimensions");
            }

            // Crop the image
            BufferedImage croppedImage = image.getSubimage(theRequest.getX(), theRequest.getY(), theRequest.getWidth(), theRequest.getHeight());
            BufferedImage copyOfCroppedImage = new BufferedImage(croppedImage.getWidth(), croppedImage.getHeight(), image.getType());
            copyOfCroppedImage.createGraphics().drawImage(croppedImage, 0, 0, null);

            // Encode the cropped image to base64
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(copyOfCroppedImage, "png", bos);
            byte[] imageBytes = bos.toByteArray();
            String base64CroppedImage = Base64.getEncoder().encodeToString(imageBytes);

            // Upload the cropped image back to S3
            byte[] decodedBytes = Base64.getDecoder().decode(base64CroppedImage);
            InputStream inputStream = new ByteArrayInputStream(decodedBytes);
            s3Client.putObject(theRequest.getBucketname(), theRequest.getOutputFilename(), inputStream, null);

            responseMap.put("message", "Cropped image saved to s3://" + theRequest.getBucketname() + "/" + theRequest.getOutputFilename());
        } catch (Exception e) {
            inspector.addAttribute("error", e.getMessage());
        }

        inspector.inspectAllDeltas();
        responseMap.putAll(inspector.finish());
        return responseMap;
    }

    private boolean validateCropDimensions(BufferedImage image, int x, int y, int width, int height) {
        return x >= 0 && y >= 0 && width > 0 && height > 0 && x + width <= image.getWidth() && y + height <= image.getHeight();
    }
}