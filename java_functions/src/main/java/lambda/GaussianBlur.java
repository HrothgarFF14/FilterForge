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

public class GaussianBlur implements RequestHandler<HashMap<String, Object>, HashMap<String, Object>> {

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

            // Apply Gaussian blur
            int kernelSize = request.containsKey("kernelSize") ? (int) request.get("kernelSize") : 5;
            double sigma = request.containsKey("sigma") ? (double) request.get("sigma") : 1.5;
            BufferedImage blurredImage = applyGaussianBlur(inputImage, kernelSize, sigma);

            // Upload the processed image back to S3
            uploadImageToS3(s3Client, blurredImage, outputBucket, outputKey, "png");

            response.setValue("Image processed and stored at s3://" + outputBucket + "/" + outputKey);
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

    public static BufferedImage applyGaussianBlur(BufferedImage image, int kernelSize, double sigma) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        double[][] kernel = createGaussianKernel(kernelSize, sigma);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int newPixel = applyKernel(image, x, y, kernel);
                outputImage.setRGB(x, y, newPixel);
            }
        }

        return outputImage;
    }

    private static double[][] createGaussianKernel(int size, double sigma) {
        double[][] kernel = new double[size][size];
        double sum = 0.0;
        int offset = size / 2;

        for (int x = -offset; x <= offset; x++) {
            for (int y = -offset; y <= offset; y++) {
                kernel[x + offset][y + offset] = Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
                sum += kernel[x + offset][y + offset];
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                kernel[i][j] /= sum;
            }
        }

        return kernel;
    }

    private static int applyKernel(BufferedImage image, int x, int y, double[][] kernel) {
        int kernelSize = kernel.length;
        int offset = kernelSize / 2;

        double r = 0, g = 0, b = 0;

        for (int ky = -offset; ky <= offset; ky++) {
            for (int kx = -offset; kx <= offset; kx++) {
                int px = clamp(x + kx, 0, image.getWidth() - 1);
                int py = clamp(y + ky, 0, image.getHeight() - 1);

                int pixel = image.getRGB(px, py);
                Color color = new Color(pixel, true);

                r += color.getRed() * kernel[kx + offset][ky + offset];
                g += color.getGreen() * kernel[kx + offset][ky + offset];
                b += color.getBlue() * kernel[kx + offset][ky + offset];
            }
        }

        int red = (int) clamp((int) r, 0, 255);
        int green = (int) clamp((int) g, 0, 255);
        int blue = (int) clamp((int) b, 0, 255);

        return new Color(red, green, blue).getRGB();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
