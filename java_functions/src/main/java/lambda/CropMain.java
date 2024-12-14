package lambda;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import saaf.Inspector;
import saaf.Response;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;

/**
 * CropMain class to execute the Crop function from the terminal.
 */
public class CropMain {

    public static void main(String[] args) {
        if (args.length < 7) {
            System.out.println("Usage: java CropMain <inputBucket> <outputBucket> <filename> <x> <y> <width> <height> <outputFilename>");
            return;
        }

        String myInputBucket = args[0];
        String myOutputBucket = args[1];
        String myFilename = args[2];
        int myX = Integer.parseInt(args[3]);
        int myY = Integer.parseInt(args[4]);
        int myWidth = Integer.parseInt(args[5]);
        int myHeight = Integer.parseInt(args[6]);
        String myOutputFilename = args[7];

        // Create a request map
        HashMap<String, Object> myRequest = new HashMap<>();
        myRequest.put("inputBucket", myInputBucket);
        myRequest.put("outputBucket", myOutputBucket);
        myRequest.put("filename", myFilename);
        myRequest.put("x", myX);
        myRequest.put("y", myY);
        myRequest.put("width", myWidth);
        myRequest.put("height", myHeight);
        myRequest.put("outputFilename", myOutputFilename);

        // Create a context
        Context myContext = new Context() {
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

        CropMain myCropMain = new CropMain();

        // Run the function
        HashMap<String, Object> myResponse = myCropMain.handleRequest(myRequest, myContext);

        // Print out function result
        System.out.println("function result:" + myResponse.toString());
    }

    public HashMap<String, Object> handleRequest(HashMap<String, Object> theRequest, Context theContext) {

        // Collect initial data.
        Inspector myInspector = new Inspector();
        myInspector.inspectAll();

        //****************START FUNCTION IMPLEMENTATION*************************

        Response myResponse = new Response();
        AmazonS3 myS3Client = AmazonS3ClientBuilder.standard().build();

        try {
            // Extract S3 details from the request
            String myInputBucket = (String) theRequest.get("inputBucket");
            String myOutputBucket = (String) theRequest.get("outputBucket");
            String myFilename = (String) theRequest.get("filename");
            String myOutputFilename = (String) theRequest.get("outputFilename");
            int myX = (int) theRequest.get("x");
            int myY = (int) theRequest.get("y");
            int myWidth = (int) theRequest.get("width");
            int myHeight = (int) theRequest.get("height");

            if (myInputBucket == null || myOutputBucket == null || myFilename == null || myOutputFilename == null) {
                throw new IllegalArgumentException("Missing required S3 parameters: inputBucket, outputBucket, filename, outputFilename");
            }

            // Download the image from S3
            BufferedImage myInputImage = downloadImageFromS3(myS3Client, myInputBucket, myFilename);

            // Crop the image
            BufferedImage myCroppedImage = cropImage(myInputImage, myX, myY, myWidth, myHeight);

            // Upload the processed image back to S3
            uploadImageToS3(myS3Client, myCroppedImage, myOutputBucket, myOutputFilename, "png");

            myResponse.setValue("Image processed and stored at s3://" + myOutputBucket + "/" + myOutputFilename);
        } catch (Exception e) {
            myResponse.setError("Error processing image: " + e.getMessage());
        }

        myInspector.consumeResponse(myResponse);

        //****************END FUNCTION IMPLEMENTATION***************************

        // Collect final information such as total runtime and cpu deltas.
        myInspector.inspectAllDeltas();
        return myInspector.finish();
    }

    private BufferedImage downloadImageFromS3(AmazonS3 theS3Client, String theBucket, String theKey) throws IOException {
        GetObjectRequest getObjectRequest = new GetObjectRequest(theBucket, theKey);
        try (InputStream inputStream = theS3Client.getObject(getObjectRequest).getObjectContent()) {
            return ImageIO.read(inputStream);
        }
    }

    private void uploadImageToS3(AmazonS3 theS3Client, BufferedImage theImage, String theBucket, String theKey, String theFormat) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(theImage, theFormat, baos);
        byte[] imageBytes = baos.toByteArray();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(imageBytes.length);
        metadata.setContentType("image/" + theFormat);

        try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            theS3Client.putObject(new PutObjectRequest(theBucket, theKey, inputStream, metadata));
        }
    }

    private BufferedImage cropImage(BufferedImage theImage, int theX, int theY, int theWidth, int theHeight) {
        return theImage.getSubimage(theX, theY, theWidth, theHeight);
    }
}