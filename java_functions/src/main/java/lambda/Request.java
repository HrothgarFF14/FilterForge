package lambda;

/**
 *
 * @author Wes Lloyd
 */
public class Request {
    
    String base64Image;
    int kernelSize;
    int sigma;
    
    public String getImage() {
        return base64Image;
    }
    
    public void setImage(String theBase64Image) {
        base64Image = theBase64Image;
    }
    
    public int getKernelSize() {
        return kernelSize;
    }
    
    public void setKernelSize(int theKernelSize) {
        kernelSize = theKernelSize;
    }
    
    public int getSigma() {
        return sigma;
    }
    
    public void setSigma(int theSigma) {
        sigma = theSigma;
    }


    public Request(String theBase64Image) {
        base64Image = theBase64Image;
    }
    
    public Request() {

    }
}
