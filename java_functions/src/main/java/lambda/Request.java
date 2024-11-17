package lambda;

/**
 * Request class for handling input parameters.
 */
public class Request {

    private String name;
    private String image;
    private int x;
    private int y;
    private int width;
    private int height;

    public String getName() {
        return name;
    }

    public String getNameALLCAPS() {
        return name.toUpperCase();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
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

    public Request(String name, String image, int x, int y, int width, int height) {
        this.name = name;
        this.image = image;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Request() {
    }
}