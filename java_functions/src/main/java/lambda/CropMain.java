package lambda;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import javax.imageio.ImageIO;

public class CropMain {

    public static void main(String[] args) {
        if (args.length < 6) {
            System.out.println("Usage: java CropMain <imagePath> <x> <y> <width> <height> <outputPath>");
            return;
        }

        String imagePath = args[0];
        int x = Integer.parseInt(args[1]);
        int y = Integer.parseInt(args[2]);
        int width = Integer.parseInt(args[3]);
        int height = Integer.parseInt(args[4]);
        String outputPath = args[5];


        try {
            // Use an absolute path for the image file
            String absoluteImagePath = Paths.get(imagePath).toAbsolutePath().toString();
            byte[] imageBytes = Files.readAllBytes(Paths.get(absoluteImagePath));
            BufferedImage image = base64ToBufferedImage(Base64.getEncoder().encodeToString(imageBytes));

            // Validate crop dimensions
            if (!validateCropDimensions(image, x, y, width, height)) {
                throw new IllegalArgumentException("Invalid crop dimensions");
            }

            // Crop the image
            BufferedImage croppedImage = image.getSubimage(x, y, width, height);
            BufferedImage copyOfCroppedImage = new BufferedImage(croppedImage.getWidth(), croppedImage.getHeight(), image.getType());
            copyOfCroppedImage.createGraphics().drawImage(croppedImage, 0, 0, null);

            // Write the cropped image to the output file
            ImageIO.write(copyOfCroppedImage, "png", Files.newOutputStream(Paths.get(outputPath)));
            System.out.println("Cropped image saved to " + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean validateCropDimensions(BufferedImage image, int x, int y, int width, int height) {
        return x >= 0 && y >= 0 && width > 0 && height > 0 && x + width <= image.getWidth() && y + height <= image.getHeight();
    }

    private static String bufferedImageToBase64(BufferedImage image) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bos);
        byte[] imageBytes = bos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private static BufferedImage base64ToBufferedImage(String base64Image) throws Exception {
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        return ImageIO.read(bis);
    }
}