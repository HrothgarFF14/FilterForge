package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import saaf.Inspector;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import javax.imageio.ImageIO;

/**
 * This class implements a Lambda function handler for cropping an image.
 * @author Louis Lomboy
 */
public class Crop implements RequestHandler<Request, HashMap<String, Object>> {

    /**
     * Lambda Function Handler
     *
     * @param theRequest Request POJO with defined variables from Request.java
     * @param theContext
     * @return HashMap that Lambda will automatically convert into JSON.
     */
    public HashMap<String, Object> handleRequest(Request theRequest, Context theContext) {

        // Collect initial data.
        Inspector inspector = new Inspector();
        inspector.inspectAll();

        //****************START FUNCTION IMPLEMENTATION*************************
        HashMap<String, Object> responseMap = new HashMap<>();
        try {
            // Decode the base64 image
            BufferedImage image = base64ToBufferedImage(theRequest.getImage());

            // Validate crop dimensions
            if (!validateCropDimensions(image, theRequest.getX(), theRequest.getY(), theRequest.getWidth(), theRequest.getHeight())) {
                throw new IllegalArgumentException("Invalid crop dimensions");
            }

            // Crop the image
            BufferedImage croppedImage = image.getSubimage(theRequest.getX(), theRequest.getY(), theRequest.getWidth(), theRequest.getHeight());
            BufferedImage copyOfCroppedImage = new BufferedImage(croppedImage.getWidth(), croppedImage.getHeight(), image.getType());
            copyOfCroppedImage.createGraphics().drawImage(croppedImage, 0, 0, null);

            // Encode the cropped image to base64
            String base64CroppedImage = bufferedImageToBase64(copyOfCroppedImage);

            // Add the cropped image to the response
            responseMap.put("croppedImage", base64CroppedImage);
        } catch (Exception e) {
            inspector.addAttribute("error", e.getMessage());
        }

        //****************END FUNCTION IMPLEMENTATION***************************

        // Collect final information such as total runtime and CPU deltas.
        inspector.inspectAllDeltas();
        responseMap.putAll(inspector.finish());
        return responseMap;
    }

    /**
     * Validates the crop dimensions to ensure they are within the bounds of the source image.
     *
     * @param image the source image
     * @param x the x coordinate of the upper-left corner of the specified rectangular region
     * @param y the y coordinate of the upper-left corner of the specified rectangular region
     * @param width the width of the specified rectangular region
     * @param height the height of the specified rectangular region
     * @return true if the dimensions are valid, false otherwise
     */
    private boolean validateCropDimensions(BufferedImage image, int x, int y, int width, int height) {
        return x >= 0 && y >= 0 && width > 0 && height > 0 && x + width <= image.getWidth() && y + height <= image.getHeight();
    }

    /**
     * Converts a BufferedImage to a base64 string.
     *
     * @param image the BufferedImage to convert
     * @return the base64 string representation of the image
     * @throws Exception if an error occurs during encoding
     */
    private String bufferedImageToBase64(BufferedImage image) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bos);
        byte[] imageBytes = bos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * Converts a base64 string to a BufferedImage.
     *
     * @param base64Image the base64 string to convert
     * @return the BufferedImage
     * @throws Exception if an error occurs during decoding
     */
    private BufferedImage base64ToBufferedImage(String base64Image) throws Exception {
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        return ImageIO.read(bis);
    }
}