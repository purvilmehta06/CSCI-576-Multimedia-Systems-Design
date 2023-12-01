import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;

public class HW1Part3 {

    // default image width and height
    private final static int WIDTH = 1920;
    private final static int HEIGHT = 1080;
    private final static int FILTER_SIZE = 5;

    /**
     * Read Image RGB
     * 
     * @param imgPath the path of the image
     * @param img     the image object to be filled
     */
    private static BufferedImage readImageRGB(String imgPath) throws IOException, FileNotFoundException {

        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        File file = new File(imgPath);
        long len = WIDTH * HEIGHT * 3;
        byte[] bytes = new byte[(int) len];

        RandomAccessFile raf = new RandomAccessFile(file, "r");
        raf.seek(0);
        raf.read(bytes);

        int ind = 0;
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                byte r = bytes[ind];
                byte g = bytes[ind + HEIGHT * WIDTH];
                byte b = bytes[ind + HEIGHT * WIDTH * 2];

                int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                img.setRGB(x, y, pix);
                ind++;
            }
        }
        raf.close();
        return img;
    }

    /**
     * Remove random pixels from the image
     * 
     * @param inputImage       the input image
     * @param removePercentage the percentage of pixels to remove
     * @return the image with random pixels removed
     */
    private static BufferedImage removeRandomPixels(BufferedImage inputImage, int removePercentage) {
        BufferedImage outputImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        int totalPixels = WIDTH * HEIGHT;
        int pixelsToRemove = (int) (totalPixels * (removePercentage / 100.0));
        int[] pixels = new int[totalPixels];
        inputImage.getRGB(0, 0, WIDTH, HEIGHT, pixels, 0, WIDTH);
        while (pixelsToRemove > 0) {
            int randomPixel = (int) (Math.random() * totalPixels);
            pixels[randomPixel] = 0;
            pixelsToRemove--;
        }
        outputImage.setRGB(0, 0, WIDTH, HEIGHT, pixels, 0, WIDTH);
        return outputImage;
    }

    /**
     * Check if the pixel is black
     * 
     * @param pixel the pixel to check
     * @return true if the pixel is black, false otherwise
     */
    private static boolean isBlackPixel(int pixel) {
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;
        return red == 0 && green == 0 && blue == 0;
    }

    /**
     * Get the RGB values of the pixel
     * 
     * @param pixel the pixel to get the RGB values from
     * @return an array of the RGB values
     */
    private static int[] getPixelValues(int pixel) {
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;
        return new int[] { red, green, blue };
    }

    /**
     * Reconstruct the image by using the average of the surrounding pixels
     * 
     * @param inputImage the image to reconstruct
     * @return the reconstructed image
     */
    private static BufferedImage reconstructImage(BufferedImage inputImage) {
        BufferedImage outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < inputImage.getWidth(); x++) {
            for (int y = 0; y < inputImage.getHeight(); y++) {

                // if pixel is not black, copy it to output image, don't need for reconstruction
                int pix = inputImage.getRGB(x, y);
                if (!isBlackPixel(pix)) {
                    outputImage.setRGB(x, y, pix);
                    continue;
                }

                int count = 0, r = 0x00, g = 0x00, b = 0x00;
                for (int dx = -(int) FILTER_SIZE / 2; dx < (int) (FILTER_SIZE / 2) + 1; dx++) {
                    for (int dy = -(int) FILTER_SIZE / 2; dy < (int) (FILTER_SIZE / 2) + 1; dy++) {
                        int newX = x + dx;
                        int newY = y + dy;

                        // if out of bounds, skip
                        if (newX < 0 || newX >= inputImage.getWidth() || newY < 0 || newY >= inputImage.getHeight())
                            continue;

                        // if pixel is black, skip
                        int samplePix = inputImage.getRGB(newX, newY);
                        // if (isBlackPixel(samplePix))
                        // continue;

                        r += (samplePix >> 16 & 0x00ff);
                        g += (samplePix >> 8 & 0x0000ff);
                        b += (samplePix & 0x000000ff);
                        count += 1;
                    }
                }

                if (count == 0) {
                    outputImage.setRGB(x, y, 0);
                    continue;
                }
                byte newR = (byte) (Math.min(255, r / count));
                byte newG = (byte) (Math.min(255, g / count));
                byte newB = (byte) (Math.min(255, b / count));
                pix = 0xff000000 | ((newR & 0xff) << 16) | ((newG & 0xff) << 8) | (newB & 0xff);
                outputImage.setRGB(x, y, pix);
            }
        }
        return outputImage;
    }

    /**
     * Calculate the error between the original image and the reconstructed image
     * 
     * @param originalImage      the original image
     * @param reconstructedImage the reconstructed image
     * @return the error between the two images
     */
    private static float calculateError(BufferedImage originalImage, BufferedImage reconstructedImage) {
        float error = 0;
        for (int x = 0; x < originalImage.getWidth(); x++) {
            for (int y = 0; y < originalImage.getHeight(); y++) {
                int[] originalRGB = getPixelValues(originalImage.getRGB(x, y));
                int[] reconstructRGB = getPixelValues(reconstructedImage.getRGB(x, y));
                error += Math.pow(originalRGB[0] - reconstructRGB[0], 2)
                        + Math.pow(originalRGB[1] - reconstructRGB[1], 2)
                        + Math.pow(originalRGB[2] - reconstructRGB[2], 2);
            }
        }
        return error;
    }

    public static void main(String[] args) {
        final File folder = new File("data_samples");
        for (final File fileEntry : folder.listFiles()) {
            String fileName = fileEntry.getName();
            if (fileName.contains(".rgb")) {
                try {
                    System.out.println("Processing " + fileName);
                    BufferedImage inputImage = readImageRGB("data_samples/" + fileName);
                    FileWriter fileWriter = new FileWriter("results/" + fileName.replace(".rgb", ".csv"));
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    bufferedWriter.write("X,Y");
                    bufferedWriter.newLine();
                    for (int i = 0; i <= 50; i++) {
                        BufferedImage removedPixelsImage = removeRandomPixels(inputImage, i);
                        BufferedImage reconstructedImage = reconstructImage(removedPixelsImage);
                        float error = calculateError(inputImage, reconstructedImage);
                        bufferedWriter.write(i + "," + String.valueOf(error));
                        bufferedWriter.newLine();
                        if (i != 0 && i % 5 == 0) {
                            String fileNameWithoutExtension = fileName.replace(".rgb", "");
                            ImageIO.write(reconstructedImage, "jpg",
                                    new File("result_images/" + fileNameWithoutExtension + "_reconstructed_" + i
                                            + ".jpg"));
                            ImageIO.write(removedPixelsImage, "jpg", new File(
                                    "result_images/" + fileNameWithoutExtension + "_removed_pixels_" + i + ".jpg"));
                        }
                    }
                    bufferedWriter.close();
                } catch (FileNotFoundException e) {
                    System.out.println("Image not found");
                    return;
                } catch (IOException e) {
                    System.out.println("Error reading image");
                    return;
                }
            }
        }
    }
}
