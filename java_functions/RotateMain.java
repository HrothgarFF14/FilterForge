package lambda;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import saaf.Inspector;
import saaf.Response;

/**
 * uwt.lambda_test::handleRequest
 *
 * @author Wes Lloyd
 * @author Robert Cordingly
 */
public class RotateMain implements RequestHandler<HashMap<String, Object>, HashMap<String, Object>> {

    /**
     * Lambda Function Handler
     * 
     * @param request Hashmap containing request JSON attributes.
     * @param context 
     * @return HashMap that Lambda will automatically convert into JSON.
     */
    public HashMap<String, Object> handleRequest(HashMap<String, Object> request, Context context) {
        
        //Collect inital data.
        Inspector inspector = new Inspector();
        inspector.inspectAll();
        Response response = new Response();

        //****************START FUNCTION IMPLEMENTATION*************************

        try {
            // Get the base64-encoded image from the request
            String base64Img = (String) request.get("image");
            String format = getFileType(base64Img);
            Double angle = request.containsKey("angle") ? (Double) request.get("angle") : 0;

            String errorMessage = null;
            if (base64Img == null || base64Img.isEmpty()) {
                errorMessage = "No image data provided";
            } else if (angle == 0) {
                errorMessage = "Invalid angle provided";
            } else if (format == "unknown") {
                errorMessage = "Invalid base64 header. Please provide filetype";
            }
            if (errorMessage != null) {
                inspector = sendError(inspector, errorMessage, response);
                return inspector.finish();
            }

            BufferedImage image = decodeBase64(base64Img);
            image = rotateImage(image, angle);
            String encodedResizedBase64Image = encodeBase64WithHeader(image, format);

            response.setValue(encodedResizedBase64Image);
            inspector.addAttribute("rotatedImage", encodedResizedBase64Image);
            inspector.consumeResponse(response);

        } catch (IOException e) {
            response.setValue("errorMessage: " + e.getMessage());
            inspector.addAttribute("errorMessage", e.getMessage());
        }

        
        //****************END FUNCTION IMPLEMENTATION***************************
                
        //Collect final information such as total runtime and cpu deltas.
        inspector.inspectAllDeltas();
        return inspector.finish();
    }
    
    public static void main (String[] args)
    {
        Context c = new Context() {
            @Override
            public String getAwsRequestId() {
                return "";
            }

            @Override
            public String getLogGroupName() {
                return "";
            }

            @Override
            public String getLogStreamName() {
                return "";
            }

            @Override
            public String getFunctionName() {
                return "";
            }

            @Override
            public String getFunctionVersion() {
                return "";
            }

            @Override
            public String getInvokedFunctionArn() {
                return "";
            }

            @Override
            public CognitoIdentity getIdentity() {
                return null;
            }

            @Override
            public ClientContext getClientContext() {
                return null;
            }

            @Override
            public int getRemainingTimeInMillis() {
                return 0;
            }

            @Override
            public int getMemoryLimitInMB() {
                return 0;
            }

            @Override
            public LambdaLogger getLogger() {
                return new LambdaLogger() {
                    @Override
                    public void log(String string) {
                        System.out.println("LOG:" + string);
                    }
                };
            }
        };
        
        ResizeMain rm = new ResizeMain();
        
        // Create a request hash map
        HashMap<String, Object> req = new HashMap<>();
        
        // Grab the img from the cmdline from arg 0 and angle from arg 1
        String base64Img = (args.length > 0 ? args[0] : "");
        Double angle = Double.parseDouble(args.length > 1 ? args[1] : "0");

        // Load the name into the request hashmap
        req.put("image", base64Img);
        req.put("angle", angle);
        // Report name to stdout
        System.out.println("cmd-line param base64Img=" + req.get("image"));
        System.out.println("cmd-line param angle=" + req.get("angle"));
        // Run the function
        HashMap resp = rm.handleRequest(req, c);        
        
        // Print out function result
        System.out.println("function result:" + resp.toString());
        
        
        
    }

    public static BufferedImage decodeBase64(String theBase64Img) throws IOException {
        if (theBase64Img.contains(",")) {
            theBase64Img = theBase64Img.split(",")[1];
        }
        byte[] imageBytes = Base64.getDecoder().decode(theBase64Img);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
        BufferedImage image = ImageIO.read(inputStream);
        inputStream.close();
        return image;
    }

    public static String encodeBase64WithHeader(BufferedImage theImage, String theFormat) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(theImage, theFormat, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        outputStream.close();
    
        // Encode to Base64
        String base64Img = Base64.getEncoder().encodeToString(imageBytes);
    
        // Add the appropriate Base64 header
        String header = "data:image/" + theFormat.toLowerCase() + ";base64,";
        return header + base64Img;
    }

    public static BufferedImage rotateImage(BufferedImage image, double angle) {
        // Calculate the center of rotation
        int centerX = image.getWidth() / 2;
        int centerY = image.getHeight() / 2;

        // Create a new BufferedImage to hold the rotated image
        BufferedImage rotatedImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                image.getType()
        );

        // Get the Graphics2D object from the new BufferedImage
        Graphics2D g2d = rotatedImage.createGraphics();

        // Apply the rotation
        AffineTransform transform = new AffineTransform();
        transform.rotate(Math.toRadians(angle), centerX, centerY);
        g2d.setTransform(transform);

        // Draw the original image onto the rotated BufferedImage
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return rotatedImage;
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

}
