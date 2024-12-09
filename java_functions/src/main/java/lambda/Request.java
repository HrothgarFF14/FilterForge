package lambda;

public class Request {

    private String inputBucket;  // Name of the input S3 bucket
    private String inputKey;     // Key of the input image in the bucket
    private String outputBucket; // Name of the output S3 bucket
    private String outputKey;    // Key for the resized image in the bucket
    private int targetWidth;     // Desired width for resizing
    private int targetHeight;    // Desired height for resizing

    // Getters and Setters
    public String getInputBucket() {
        return inputBucket;
    }

    public void setInputBucket(String inputBucket) {
        this.inputBucket = inputBucket;
    }

    public String getInputKey() {
        return inputKey;
    }

    public void setInputKey(String inputKey) {
        this.inputKey = inputKey;
    }

    public String getOutputBucket() {
        return outputBucket;
    }

    public void setOutputBucket(String outputBucket) {
        this.outputBucket = outputBucket;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public int getTargetWidth() {
        return targetWidth;
    }

    public void setTargetWidth(int targetWidth) {
        this.targetWidth = targetWidth;
    }

    public int getTargetHeight() {
        return targetHeight;
    }

    public void setTargetHeight(int targetHeight) {
        this.targetHeight = targetHeight;
    }

    // Constructors
    public Request() {}

    public Request(String inputBucket, String inputKey, String outputBucket, String outputKey, int targetWidth, int targetHeight) {
        this.inputBucket = inputBucket;
        this.inputKey = inputKey;
        this.outputBucket = outputBucket;
        this.outputKey = outputKey;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
    }
}
