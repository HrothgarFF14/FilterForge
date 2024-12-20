package saaf;

/**
 * A basic Response object that can be consumed by FaaS Inspector
 * to be used as additional output.
 *
 * @author Wes Lloyd
 * @author Robert Cordingly
 */
public class Response {
    //
    // User Defined Attributes
    //
    //
    // ADD getters and setters for custom attributes here.
    //

    // Return value
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "value=" + this.getValue() + super.toString();
    }

<<<<<<<< HEAD:Gaussian_Blur/java_functions/src/main/java/saaf/Response.java
    public void setError(String string) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
========
    public void setError(String error) {
        throw new UnsupportedOperationException("Not supported yet."); //Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
>>>>>>>> crop_java:Crop/java_functions/src/main/java/saaf/Response.java
    }
}
