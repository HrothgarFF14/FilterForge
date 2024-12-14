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
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;

/**
 * AWS Lambda function that crops an image stored in an S3 bucket.
 */
public class Crop implements RequestHandler<HashMap<String, Object>, HashMap<String, Object>> {

    /**
     * Handles a Lambda Function request to crop an image stored in an S3 bucket.
     *
     * @param theRequest The Lambda Function input
     * @param theContext The Lambda execution environment context object.
     * @return the response containg the result of the image cropping operation.
     */
    @Override
    public HashMap<String, Object> handleRequest(HashMap<String, Object> theRequest, Context theContext) {
        Inspector myInspector = new Inspector();
        myInspector.inspectContainer();
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
        return myInspector.finish();
    }


    /**
     * Downloads an image from an S3 bucket.
     *
     * @param theS3Client The Amazon S3 client used to interact with S3.
     * @param theBucket The name of the S3 bucket.
     * @param theKey The key (filename) of the image in the S3 bucket.
     * @return The downloaded image as a BufferedImage.
     * @throws IOException If an error occurs during the download or reading of the image.
     */
    private BufferedImage downloadImageFromS3(AmazonS3 theS3Client, String theBucket, String theKey) throws IOException {
        GetObjectRequest getObjectRequest = new GetObjectRequest(theBucket, theKey);
        try (InputStream inputStream = theS3Client.getObject(getObjectRequest).getObjectContent()) {
            return ImageIO.read(inputStream);
        }
    }

    /**
     * Uploads an image to an S3 bucket.
     *
     * @param theS3Client The Amazon S3 client used to interact with S3.
     * @param theImage The image to be uploaded.
     * @param theBucket The name of the S3 bucket.
     * @param theKey The key (filename) for the uploaded image.
     * @param theFormat The format of the image (e.g., "png").
     * @throws IOException If an error occurs during the upload.
     */
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

    /**
     * Crops an image to the specified dimensions.
     *
     * @param theImage The image to be cropped.
     * @param theX The x-coordinate of the upper-left corner of the cropping rectangle.
     * @param theY The y-coordinate of the upper-left corner of the cropping rectangle.
     * @param theWidth The width of the cropping rectangle.
     * @param theHeight The height of the cropping rectangle.
     * @return The cropped image as a BufferedImage.
     */
    private BufferedImage cropImage(BufferedImage theImage, int theX, int theY, int theWidth, int theHeight) {
        return theImage.getSubimage(theX, theY, theWidth, theHeight);
    }
}