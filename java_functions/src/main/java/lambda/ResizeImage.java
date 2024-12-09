package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import saaf.Inspector;
import saaf.Response;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;

public class ResizeImage implements RequestHandler<HashMap<String, Object>, HashMap<String, Object>> {

    @Override
    public HashMap<String, Object> handleRequest(HashMap<String, Object> request, Context context) {
        Inspector inspector = new Inspector();
        inspector.inspectContainer();
        Response response = new Response();

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();

        try {
            // Extract S3 details from the request
            String inputBucket = (String) request.get("inputBucket");
            String inputKey = (String) request.get("inputKey");
            String outputBucket = (String) request.get("outputBucket");
            String outputKey = (String) request.get("outputKey");

            if (inputBucket == null || inputKey == null || outputBucket == null || outputKey == null) {
                throw new IllegalArgumentException("Missing required S3 parameters: inputBucket, inputKey, outputBucket, outputKey");
            }

            // Download the image from S3
            BufferedImage inputImage = downloadImageFromS3(s3Client, inputBucket, inputKey);

            // Apply resize
            int targetWidth = request.containsKey("targetWidth") ? (int) request.get("targetWidth") : 100;
            int targetHeight = request.containsKey("targetHeight") ? (int) request.get("targetHeight") : 100;
            BufferedImage resizedImage = resizeImage(inputImage, targetWidth, targetHeight);

            // Upload the processed image back to S3
            uploadImageToS3(s3Client, resizedImage, outputBucket, outputKey, "png");

            response.setValue("Image resized and stored at s3://" + outputBucket + "/" + outputKey);
        } catch (Exception e) {
            response.setError("Error processing image: " + e.getMessage());
        }

        inspector.consumeResponse(response);
        return inspector.finish();
    }

    private BufferedImage downloadImageFromS3(AmazonS3 s3Client, String bucket, String key) throws IOException {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, key);
        try (InputStream inputStream = s3Client.getObject(getObjectRequest).getObjectContent()) {
            return ImageIO.read(inputStream);
        }
    }

    private void uploadImageToS3(AmazonS3 s3Client, BufferedImage image, String bucket, String key, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        byte[] imageBytes = baos.toByteArray();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(imageBytes.length);
        metadata.setContentType("image/" + format);

        try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            s3Client.putObject(new PutObjectRequest(bucket, key, inputStream, metadata));
        }
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();
        return resizedImage;
    }
}
