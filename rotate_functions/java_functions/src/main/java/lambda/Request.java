package lambda;

/**
 *
 * @author Wes Lloyd
 */
public class Request {

    String base64Img;
    int targetHeight;
    int targetWidth;

    public String getImage() {
        return base64Img;
    }
    
    public void setName(String base64Img) {
        this.base64Img = base64Img;
    }

    public Request(String base64Img) {
        this.base64Img = base64Img;
    }

    public Request() {

    }
}
