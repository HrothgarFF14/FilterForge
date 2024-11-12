package main.java.lambda;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

/**
 * This class is used to crop an image to a specified size.
 */
public class Crop {
    /**
     * This method crops an image to a specified size.
     * @param image The image to crop.
     * @param width The width to crop the image to.
     * @param height The height to crop the image to.
     * @return The cropped image.
     */
    public static BufferedImage crop(BufferedImage image, int width, int height) {
        BufferedImage croppedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = croppedImage.createGraphics();
        g2d.drawImage(image, 0, 0, width, height, 0, 0, width, height, null);
        g2d.dispose();
        return croppedImage;
    }
    
}
