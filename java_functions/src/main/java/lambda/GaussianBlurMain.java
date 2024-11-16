package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import saaf.Inspector;
import saaf.Response;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class GaussianBlurMain implements RequestHandler<HashMap<String, Object>, HashMap<String, Object>> {

    /**
     * Lambda Function Handler
     *
     * @param request Hashmap containing request JSON attributes.
     * @param context
     * @return HashMap that Lambda will automatically convert into JSON.
     */
    @Override
    public HashMap<String, Object> handleRequest(HashMap<String, Object> request, Context context) {
        // Inspect incoming request (for debugging/logging)
        Inspector inspector = new Inspector();
        inspector.inspectContainer();

        Response response = new Response();

        try {
            // Get the base64-encoded image from the request
            String base64Image = (String) request.get("image");

            String errorMessage = null;
            if (base64Image == null || base64Image.isEmpty()) {
                errorMessage = "No image data provided";
            }
            if (errorMessage != null) {
                inspector = sendError(inspector, errorMessage, response);
                return inspector.finish();
            }

            // Decode the image
            BufferedImage inputImage = decodeBase64ToImage(base64Image);

            // Apply Gaussian Blur
            int kernelSize = request.containsKey("kernelSize") ? (int) request.get("kernelSize") : 5;
            System.out.println((request.containsKey("kernelSize") ? "ITS WORK" : "IT DOESNT"));
            double sigma = request.containsKey("sigma") ? (double) request.get("sigma") : 1.5;
            BufferedImage blurredImage = applyGaussianBlur(inputImage, kernelSize, sigma);

            // Encode the blurred image back to base64
            String encodedImage = encodeImageToBase64(blurredImage, "PNG");

            // Set the base64 string in the response
            response.setValue(encodedImage);
        } catch (Exception e) {
            response.setError("Error processing image: " + e.getMessage());
        }

        // Finalize and return the response
        inspector.consumeResponse(response);
        return inspector.finish();
    }

    // Apply the Gaussian Blur to the image using convolution
    public static BufferedImage applyGaussianBlur(BufferedImage image, int kernelSize, double sigma) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Create the Gaussian kernel
        double[][] kernel = createGaussianKernel(kernelSize, sigma);

        // Apply the convolution to each pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int newPixel = applyKernel(image, x, y, kernel);
                outputImage.setRGB(x, y, newPixel);
            }
        }

        return outputImage;
    }
    
    
    // Create a Gaussian Kernel
    public static double[][] createGaussianKernel(int size, double sigma) {
        double[][] kernel = new double[size][size];
        double sum = 0.0;
        int offset = size / 2;

        // Calculate the kernel values based on the Gaussian function
        for (int x = -offset; x <= offset; x++) {
            for (int y = -offset; y <= offset; y++) {
                kernel[x + offset][y + offset] = Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
                sum += kernel[x + offset][y + offset];
            }
        }

        // Normalize the kernel so the sum of all elements is 1
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                kernel[i][j] /= sum;
            }
        }

        return kernel;
    }


    // Apply the kernel at a specific pixel location
    private static int applyKernel(BufferedImage image, int x, int y, double[][] kernel) {
        int kernelSize = kernel.length;
        int offset = kernelSize / 2;

        double r = 0, g = 0, b = 0;

        for (int ky = -offset; ky <= offset; ky++) {
            for (int kx = -offset; kx <= offset; kx++) {
                // Get the pixel value from the image (with edge handling)
                int px = clamp(x + kx, 0, image.getWidth() - 1);
                int py = clamp(y + ky, 0, image.getHeight() - 1);

                int pixel = image.getRGB(px, py);
                Color color = new Color(pixel, true);

                // Get the color channels
                r += color.getRed() * kernel[kx + offset][ky + offset];
                g += color.getGreen() * kernel[kx + offset][ky + offset];
                b += color.getBlue() * kernel[kx + offset][ky + offset];
            }
        }

        // Clamp the RGB values and return a new pixel
        int red = (int) clamp(r, 0, 255);
        int green = (int) clamp(g, 0, 255);
        int blue = (int) clamp(b, 0, 255);

        return new Color(red, green, blue).getRGB();
    }

    // Clamp function to restrict values within a range
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    // Helper function to clamp integer values
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    // Convert the BufferedImage to a base64-encoded string
    public static String encodeImageToBase64(BufferedImage image, String formatName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, formatName, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    // Decode a base64-encoded image string to a BufferedImage
    public static BufferedImage decodeBase64ToImage(String base64Image) throws IOException {
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
        return ImageIO.read(bais);
    }

    public static Inspector sendError(Inspector theInspector, String theMessage, Response theResponse) {
        theResponse.setValue("errorMessage: " + theMessage);
        theInspector.addAttribute("errorMessage", theMessage);
        theInspector.consumeResponse(theResponse);
        return theInspector;
    }

    public static String getFileType(String base64Image) {
        // Regex pattern to extract the file type from the Base64 header
        String regex = "data:image/([a-zA-Z0-9]+);base64";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(base64Image);

        if (matcher.find()) {
            return matcher.group(1); // Return the captured file type
        } else {
            return "unknown"; // Return "unknown" if no match is found
        }
    }
    
    
    // Main method for local testing
    public static void main(String[] args) {
        GaussianBlurMain blurProcessor = new GaussianBlurMain();

        // Simulate input JSON
        HashMap<String, Object> request = new HashMap<>();
        
        try {
            // Load an image from a local file and encode it in base64
            BufferedImage inputImage = ImageIO.read(new File("/home/jovany/Downloads/input.jpg")); // Replace with your test image
            String base64Image = encodeImageToBase64(inputImage, "JPG");
            request.put("image", base64Image);
            request.put("kernelSize", 23);
            request.put("sigma", 7.5);
        } catch (IOException e) {
            System.err.println("Error loading input image: " + e.getMessage());
            return;
        }

        // Process the request
        HashMap<String, Object> response = blurProcessor.handleRequest(request, null);

        // Decode and save the output image
        String base64OutputImage = (String) response.get("value");
        if (base64OutputImage != null) {
            try {
                BufferedImage outputImage = decodeBase64ToImage(base64OutputImage);
                ImageIO.write(outputImage, "PNG", new File("output.png")); // Save the output image
                System.out.println("Blurred image saved as output.png");
            } catch (IOException e) {
                System.err.println("Error saving output image: " + e.getMessage());
            }
        } else {
            System.err.println("Error in response: " + response.get("error"));
        }
    }
    
    
}