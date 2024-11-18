package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import saaf.Inspector;
import saaf.Response;
import java.util.HashMap;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Base64;

/**
 * uwt.lambda_test::handleRequest
 *
 * @author Wes Lloyd
 * @author Robert Cordingly
 */




public class ResizeImage implements RequestHandler<Request, HashMap<String, Object>> {

    @Override
    public HashMap<String, Object> handleRequest(Request request, Context context) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            // Decode the image from Base64 string
            byte[] decodedBytes = Base64.getDecoder().decode(request.getImageString());
            ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes);

            // Read the image
            BufferedImage inputImage = ImageIO.read(inputStream);

            // Resize the image
            int width = request.getWidth();
            int height = request.getHeight();
            BufferedImage resizedImage = new BufferedImage(width, height, inputImage.getType());
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.drawImage(inputImage, 0, 0, width, height, null);
            g2d.dispose();

            // Save the resized image to the /tmp directory
            String resizedImagePath = "/tmp/resized-image.jpg";
            File outputFile = new File(resizedImagePath);
            ImageIO.write(resizedImage, "jpg", outputFile);

            // Return success message with the file path
            response.put("status", "success");
            response.put("message", "Image resized successfully. Saved to: " + resizedImagePath);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error resizing image: " + e.getMessage());
        }
        return response;
    }
}
