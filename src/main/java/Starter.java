import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Starter {

    static class Coord {
        final int x;
        final int y;

        Coord(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static List<Integer> possibleCardColor = new ArrayList<>(2);
    static {
        possibleCardColor.add(-1);
        possibleCardColor.add(-8882056);
    }

    public static void main(String[] arg) throws IOException {
        int cardWaith = 68;
        int cardHeith  = 84;
        int barWaith = 4;
        final int[] count = {0};

        Path pathToTmp = Paths.get(".\\trash");
        FileUtils.deleteDirectory(pathToTmp.toFile());
        Files.createDirectories(pathToTmp);
        Map<String, BufferedImage> types = Files.list(Paths.get(".\\data\\types")).collect(Collectors.toMap(path -> path.toFile().getName().replace(".png", ""), path -> {
            try {
                return ImageIO.read(path.toFile());
            } catch (IOException e) {
                System.out.println("Error on read data");
                System.exit(1);
            }
            return null;
        }));

        Map<String, BufferedImage> suit = Files.list(Paths.get(".\\data\\masti")).collect(Collectors.toMap(path -> path.toFile().getName().replace(".png", ""), path -> {
            try {
                BufferedImage bufferedImage = ImageIO.read(path.toFile());
                BufferedImage blackWhite = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
                Graphics2D graphics = blackWhite.createGraphics();
                graphics.drawImage(bufferedImage, 0, 0, null);
                return blackWhite;
            } catch (IOException e) {
                System.out.println("Error on read data");
                System.exit(1);
            }
            return null;
        }));

        //separate service ?
        Files.list(Paths.get(arg[0])).forEach(path -> {

            //separate function and io forEach  ?
            try {
                BufferedImage img = ImageIO.read(path.toFile());
                Coord center = new Coord(img.getWidth() / 2, img.getHeight() / 2);

                List<BufferedImage> potencialCards = new ArrayList<>();

                int currentPosition = center.x - cardWaith /2;//left end of first card

                int upperCardLine = center.y;

                //go to left from center
                int shiftTillNextCardLeftTail = 0;
                while (currentPosition + shiftTillNextCardLeftTail + cardWaith < img.getWidth()) {
                    BufferedImage subimage = img.getSubimage(currentPosition + shiftTillNextCardLeftTail, upperCardLine, cardWaith, cardHeith);
                    shiftTillNextCardLeftTail += cardWaith + barWaith;
                    potencialCards.add(subimage);
                }

                //go to right from center
                shiftTillNextCardLeftTail = cardWaith + barWaith;
                while (currentPosition - shiftTillNextCardLeftTail > 0) {
                    BufferedImage subimage = img.getSubimage(currentPosition - shiftTillNextCardLeftTail, upperCardLine, cardWaith, cardHeith);
                    shiftTillNextCardLeftTail += cardWaith + barWaith;
                    potencialCards.add(subimage);
                }

                List<String> cards = new ArrayList<>();
                //TODO make CardProcessor.class from lambda
                potencialCards.forEach(bufferedImage -> {
                    int labelCoordX = 47;
                    int labelCoordY  = 68;
                    Color typeLabelColor = new Color(bufferedImage.getRGB(labelCoordX, labelCoordY));
                    Color centerColor = new Color(bufferedImage.getRGB(bufferedImage.getWidth() /2 , bufferedImage.getHeight() /2));
                    if(centerColor.getRed() == 120 && centerColor.getGreen() == 120 && centerColor.getBlue() == 120) {
                        for(int x=0;x < bufferedImage.getWidth();x++) {
                            for(int y=0;y < bufferedImage.getHeight();y++) {
                                Color each = new Color(bufferedImage.getRGB(x, y));
                                if(each.getRed() == 120 && each.getGreen() == 120 && each.getBlue() <= 120) {
                                    Color newOne = new Color(255, 255, 255);
                                    bufferedImage.setRGB(x, y, newOne.getRGB());
                                }
                            }
                        }
                    }

                    if(typeLabelColor.getRed() > 95) {
                        String[] knowCard = searchForKnowParts(bufferedImage, types, suit);
                        if(knowCard[0] == null || knowCard[1] == null) {
                            System.out.println("cannot determinate suite : " + knowCard[0] + " type: " + knowCard[1] + " path: " + path);
                            return;
                        }
                        cards.add(knowCard[0].concat(knowCard[1]));
                    } else  if(typeLabelColor.getRed() < 50 && typeLabelColor.getGreen() < 50 && typeLabelColor.getBlue() < 50) {
                        if(centerColor.getRed() == 255 && centerColor.getGreen() == 255 && centerColor.getBlue() == 255) {
                            String[] knowCard = searchForKnowParts(bufferedImage, types, suit);
                            if(knowCard[0] == null || knowCard[1] == null) {
                                System.out.println("cannot determinate suite : " + knowCard[0] + " type: " + knowCard[1]+ " path: " + path);
                                return;
                            }
                            cards.add(knowCard[0].concat(knowCard[1]));
                        } else if (typeLabelColor.getRed() == 16 && typeLabelColor.getGreen() == 16 && typeLabelColor.getBlue() == 18) {
                            System.out.println("cards skipped color: " + typeLabelColor.getRed() + " " + typeLabelColor.getGreen() + " " + typeLabelColor.getBlue());
                        }
                    } else {
                        System.out.println("cards skipped color: " + typeLabelColor.getRed() + " " + typeLabelColor.getGreen() + " " + typeLabelColor.getBlue());
                    }
                });

                System.out.println(path + "-" + cards);
            } catch (IOException e) {
                //logger ?
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static String[] searchForKnowParts(BufferedImage bufferedImage, Map<String, BufferedImage> knowParts, Map<String, BufferedImage> suit) {
        BufferedImage blackWhite = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D graphics = blackWhite.createGraphics();
        graphics.drawImage(bufferedImage, 0, 0, null);
        String cardSuit = null;
        String cardType = null;
        //magic constant optimisations of shift
        for(int x = 3;x < 20; x++) {
            for (int y = 0; y < 11; y++) {
                if(cardType != null) {
                    break;
                }
                BufferedImage typeImage = blackWhite.getSubimage(x, y, 25, 29);
                cardType = compareImagesByColorPerPixel(knowParts, typeImage, 0.05, true);
            }

            for (int y = 30; y < 50; y++) {
                if(cardSuit != null) {
                    break;
                }
                BufferedImage suitImage = blackWhite.getSubimage(x, y, 15, 17);
                cardSuit = compareImagesByColorPerPixel(suit, suitImage, 0.07, false);
            }

            if(cardSuit != null && cardType != null) {
                break;
            }
        }

        return new String[] {cardSuit, cardType};
    }

    private static String compareImagesByColorPerPixel(Map<String, BufferedImage> examples, BufferedImage typeImage, double dopusk, boolean allowExtentionOfDupusk) {

        String result = examples.entrySet().stream().map(kv -> {
            BufferedImage example = kv.getValue();
            int pixelCount = example.getWidth() * example.getHeight();
            int failCount = 0;
            for (int xExample = 0; xExample < example.getWidth(); xExample++) {
                for (int yExample = 0; yExample < example.getHeight(); yExample++) {
                    if (example.getRGB(xExample, yExample) != typeImage.getRGB(xExample, yExample)) {
                        failCount++;
                    }


                    if (failCount > pixelCount * dopusk) {
                        return new AbstractMap.SimpleEntry<>(kv.getKey(), -1);
                    }
                }
            }
            return new AbstractMap.SimpleEntry<>(kv.getKey(), failCount);

        }).filter(resultPair -> resultPair.getValue() >= 0).sorted(Comparator.comparingInt(Map.Entry::getValue)).map(Map.Entry::getKey).findFirst().orElse(null);
        if(result == null && dopusk < 0.09 && allowExtentionOfDupusk) {
            double newDopusk = dopusk + 0.01;
            return compareImagesByColorPerPixel(examples, typeImage, newDopusk, true);
        } else {
            return result;
        }
    }
}
