import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Tester {

    public static void main(String[] arg) throws IOException {
        Files.list(Paths.get("D:\\img\\tmp")).forEach(path -> {
            try {
                BufferedImage img = ImageIO.read(path.toFile());
                int centerX = img.getWidth() / 2;
                int centerY = img.getHeight() / 2;
                System.out.println(img.getRGB(centerX, centerY));

                Raster ras = img.getRaster();

                int elem = ras.getNumDataElements();

                System.out.println("Number of Elems: " + elem);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Color myWhite = new Color(255, 255, 255); // Color white
        int rgb = myWhite.getRGB();
        System.out.println("White:" + rgb);



    }
}
