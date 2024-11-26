package lambda;

public class Request {

    // S3-related fields
    private String inputBucket;
    private String inputKey;
    private String outputBucket;
    private String outputKey;

    // Gaussian blur parameters
    private int kernelSize = 5; // Default kernel size
    private double sigma = 1.5; // Default sigma value

    public Request() {
        // Default constructor
    }

    // Getters and setters for S3 fields
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

    // Getters and setters for Gaussian blur parameters
    public int getKernelSize() {
        return kernelSize;
    }

    public void setKernelSize(int kernelSize) {
        if (kernelSize < 1) {
            throw new IllegalArgumentException("Kernel size must be a positive integer.");
        }
        this.kernelSize = kernelSize;
    }

    public double getSigma() {
        return sigma;
    }

    public void setSigma(double sigma) {
        if (sigma <= 0) {
            throw new IllegalArgumentException("Sigma must be a positive value.");
        }
        this.sigma = sigma;
    }

    @Override
    public String toString() {
        return "Request{"
                + "inputBucket='" + inputBucket + '\''
                + ", inputKey='" + inputKey + '\''
                + ", outputBucket='" + outputBucket + '\''
                + ", outputKey='" + outputKey + '\''
                + ", kernelSize=" + kernelSize
                + ", sigma=" + sigma
                + '}';
    }
}
