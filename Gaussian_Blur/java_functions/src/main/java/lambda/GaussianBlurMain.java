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

    public static BufferedImage applyGaussianBlur(BufferedImage image, int kernelSize, double sigma) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Determine tile size
        int tileSize = 256; // Adjust this size based on memory constraints
        int overlap = kernelSize / 2; // Overlap to handle kernel edges

        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        double[][] kernel = createGaussianKernel(kernelSize, sigma);

        // Process each tile
        for (int ty = 0; ty < height; ty += tileSize) {
            for (int tx = 0; tx < width; tx += tileSize) {
                int tileWidth = Math.min(tileSize, width - tx);
                int tileHeight = Math.min(tileSize, height - ty);

                // Extend the tile with overlap
                int extendedX = Math.max(0, tx - overlap);
                int extendedY = Math.max(0, ty - overlap);
                int extendedWidth = Math.min(width, tx + tileWidth + overlap) - extendedX;
                int extendedHeight = Math.min(height, ty + tileHeight + overlap) - extendedY;

                BufferedImage tile = image.getSubimage(extendedX, extendedY, extendedWidth, extendedHeight);

                BufferedImage processedTile = processTile(tile, kernel, tx - extendedX, ty - extendedY, tileWidth, tileHeight);

                // Write the processed tile back to the output image
                for (int y = 0; y < tileHeight; y++) {
                    for (int x = 0; x < tileWidth; x++) {
                        int rgb = processedTile.getRGB(x, y);
                        outputImage.setRGB(tx + x, ty + y, rgb);
                    }
                }
            }
        }

        return outputImage;
    }

    private static BufferedImage processTile(BufferedImage tile, double[][] kernel, int offsetX, int offsetY, int tileWidth, int tileHeight) {
        BufferedImage result = new BufferedImage(tile.getWidth(), tile.getHeight(), BufferedImage.TYPE_INT_ARGB);

        // Apply the kernel to the tile
        for (int y = 0; y < tile.getHeight(); y++) {
            for (int x = 0; x < tile.getWidth(); x++) {
                if (x >= offsetX && x < offsetX + tileWidth && y >= offsetY && y < offsetY + tileHeight) {
                    int newPixel = applyKernel(tile, x, y, kernel);
                    result.setRGB(x, y, newPixel);
                }
            }
        }

        return result;
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
        byte[] imageBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, formatName, baos);
            imageBytes = baos.toByteArray();
        }
        
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String header = "data:image/"+ formatName.toLowerCase() + ";base64,";
        return header + base64Image;
    }

    // Decode a base64-encoded image string to a BufferedImage
    public static BufferedImage decodeBase64ToImage(String base64Image) throws IOException {
        if(base64Image.contains(",")) base64Image = base64Image.split(",")[1];
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
            BufferedImage inputImage = ImageIO.read(new File("/home/jovany/Pictures/large_image.png")); // Replace with your test image
            
            
            String base64Image = encodeImageToBase64(inputImage, "PNG");
            
            
            request.put("image", base64Image);
            request.put("kernelSize", 5);
            request.put("sigma", 1.5);
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
                System.out.println("Function Result: " + response.toString());
            } catch (IOException e) {
                System.err.println("Error saving output image: " + e.getMessage());
            }
        } else {
            System.err.println("Error in response: " + response.get("error"));
        }
    }
    
    
}
