package lambda;

public class Request {

    private String imageString; // Base64-encoded image string
    private int width;          // Desired width
    private int height;         // Desired height

    // Getters and Setters
    public String getImageString() {
        return imageString;
    }

    public void setImageString(String imageString) {
        this.imageString = imageString;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    // Constructors
    public Request() {}

    public Request(String imageString, int width, int height) {
        this.imageString = imageString;
        this.width = width;
        this.height = height;
    }
}
