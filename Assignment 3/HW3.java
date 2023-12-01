import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

public class HW3 {

    private final static int WIDTH = 512;
    private final static int HEIGHT = 512;

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
     * Show Image
     * 
     * @param images images to show
     */
    private static void showImgs(BufferedImage[] images) {
        JFrame frame = new JFrame();
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel lbIm1 = new JLabel();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 1;
        frame.getContentPane().add(lbIm1, c);
        frame.pack();
        frame.setVisible(true);
        for (int i = 0; i < images.length; ++i) {
            try {
                lbIm1.setIcon(new ImageIcon(images[i]));
                frame.pack();
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Error sleeping");
            }

        }
    }

    /**
     * Get Pixel
     * 
     * @param r red value
     * @param g green value
     * @param b blue value
     * @return pixel value
     */
    private static int getPixel(float r, float g, float b) {
        int ri = (int) r;
        int gi = (int) g;
        int bi = (int) b;
        return (ri << 16) | (gi << 8) | bi;
    }

    /**
     * Get RGB Array
     * 
     * @param pixel pixel value
     * @return array of rgb values
     */
    private static int[] getRGBArray(int pixel) {
        int[] rgb = new int[3];
        rgb[0] = (pixel >> 16) & 0xff;
        rgb[1] = (pixel >> 8) & 0xff;
        rgb[2] = pixel & 0xff;
        return rgb;
    }

    /**
     * Copy RGB Channels
     * 
     * @param rgb rgb channels
     * @return copy of rgb channels
     */
    private static float[][][] copyRGBChannels(float[][][] rgb) {
        float[][][] copy = new float[3][HEIGHT][WIDTH];
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < HEIGHT; ++j) {
                for (int k = 0; k < WIDTH; ++k) {
                    copy[i][j][k] = rgb[i][j][k];
                }
            }
        }
        return copy;
    }

    /**
     * Encode Image
     * 
     * @param inputImage input image
     * @param level      level of encoding
     * @return encoded image
     */
    private static float[][][] encodeImage(float[][][] inputRGB, int level) {
        int size = (int) Math.pow(2, level);

        // step 1: going through each row
        float[][][] encodeRGBChannelsRow = copyRGBChannels(inputRGB);
        for (int h = 0; h < size; ++h) {
            for (int w = 0; w < size; w += 2) {
                float avgR = (inputRGB[0][h][w] + inputRGB[0][h][w + 1]) / 2.0f;
                float avgG = (inputRGB[1][h][w] + inputRGB[1][h][w + 1]) / 2.0f;
                float avgB = (inputRGB[2][h][w] + inputRGB[2][h][w + 1]) / 2.0f;
                float diffR = (inputRGB[0][h][w] - inputRGB[0][h][w + 1]) / 2.0f;
                float diffG = (inputRGB[1][h][w] - inputRGB[1][h][w + 1]) / 2.0f;
                float diffB = (inputRGB[2][h][w] - inputRGB[2][h][w + 1]) / 2.0f;

                encodeRGBChannelsRow[0][h][w / 2] = avgR;
                encodeRGBChannelsRow[1][h][w / 2] = avgG;
                encodeRGBChannelsRow[2][h][w / 2] = avgB;
                encodeRGBChannelsRow[0][h][w / 2 + size / 2] = diffR;
                encodeRGBChannelsRow[1][h][w / 2 + size / 2] = diffG;
                encodeRGBChannelsRow[2][h][w / 2 + size / 2] = diffB;
            }
        }

        // step 2: going through each column
        float[][][] encodeRGBChannelsCol = copyRGBChannels(encodeRGBChannelsRow);
        for (int w = 0; w < size; ++w) {
            for (int h = 0; h < size; h += 2) {
                float avgR = (encodeRGBChannelsRow[0][h][w] + encodeRGBChannelsRow[0][h + 1][w]) / 2.0f;
                float avgG = (encodeRGBChannelsRow[1][h][w] + encodeRGBChannelsRow[1][h + 1][w]) / 2.0f;
                float avgB = (encodeRGBChannelsRow[2][h][w] + encodeRGBChannelsRow[2][h + 1][w]) / 2.0f;
                float diffR = (encodeRGBChannelsRow[0][h][w] - encodeRGBChannelsRow[0][h + 1][w]) / 2.0f;
                float diffG = (encodeRGBChannelsRow[1][h][w] - encodeRGBChannelsRow[1][h + 1][w]) / 2.0f;
                float diffB = (encodeRGBChannelsRow[2][h][w] - encodeRGBChannelsRow[2][h + 1][w]) / 2.0f;

                encodeRGBChannelsCol[0][h / 2][w] = avgR;
                encodeRGBChannelsCol[1][h / 2][w] = avgG;
                encodeRGBChannelsCol[2][h / 2][w] = avgB;
                encodeRGBChannelsCol[0][h / 2 + size / 2][w] = diffR;
                encodeRGBChannelsCol[1][h / 2 + size / 2][w] = diffG;
                encodeRGBChannelsCol[2][h / 2 + size / 2][w] = diffB;
            }
        }

        return encodeRGBChannelsCol;
    }

    /**
     * Black Out High Frequency
     * 
     * @param rgb   rgb channels
     * @param level level of blacking out
     * @return blacked out image
     */
    public static float[][][] blackOutHighFreq(float[][][] rgb, int level) {
        int size = (int) Math.pow(2, level);
        float[][][] blackedOut = copyRGBChannels(rgb);
        for (int h = 0; h < HEIGHT; ++h) {
            for (int w = 0; w < WIDTH; ++w) {
                if (h >= size || w >= size) {
                    blackedOut[0][h][w] = 0.0f;
                    blackedOut[1][h][w] = 0.0f;
                    blackedOut[2][h][w] = 0.0f;
                }
            }
        }
        return blackedOut;
    }

    /**
     * Decode Image
     * 
     * @param inputImage input image
     * @param level      level of decoding
     * @return decoded image
     */
    private static float[][][] decodeImage(float[][][] inputRGB, int level) {
        int size = (int) Math.pow(2, level);

        // going through each column and do the exact opposite of encoding
        float[][][] decodeRGBChannelsCol = copyRGBChannels(inputRGB);

        for (int w = 0; w < 2 * size; ++w) {
            for (int h = 0; h < size; ++h) {
                float avgR = inputRGB[0][h][w] + inputRGB[0][h + size][w];
                float avgG = inputRGB[1][h][w] + inputRGB[1][h + size][w];
                float avgB = inputRGB[2][h][w] + inputRGB[2][h + size][w];
                float diffR = inputRGB[0][h][w] - inputRGB[0][h + size][w];
                float diffG = inputRGB[1][h][w] - inputRGB[1][h + size][w];
                float diffB = inputRGB[2][h][w] - inputRGB[2][h + size][w];

                decodeRGBChannelsCol[0][h * 2][w] = avgR;
                decodeRGBChannelsCol[1][h * 2][w] = avgG;
                decodeRGBChannelsCol[2][h * 2][w] = avgB;
                decodeRGBChannelsCol[0][h * 2 + 1][w] = diffR;
                decodeRGBChannelsCol[1][h * 2 + 1][w] = diffG;
                decodeRGBChannelsCol[2][h * 2 + 1][w] = diffB;
            }
        }

        // going through each row and do the exact opposite of encoding
        float[][][] decodeRGBChannelsRow = copyRGBChannels(decodeRGBChannelsCol);
        for (int h = 0; h < size * 2; ++h) {
            for (int w = 0; w < size; ++w) {
                float avgR = decodeRGBChannelsCol[0][h][w] + decodeRGBChannelsCol[0][h][w + size];
                float avgG = decodeRGBChannelsCol[1][h][w] + decodeRGBChannelsCol[1][h][w + size];
                float avgB = decodeRGBChannelsCol[2][h][w] + decodeRGBChannelsCol[2][h][w + size];
                float diffR = decodeRGBChannelsCol[0][h][w] - decodeRGBChannelsCol[0][h][w + size];
                float diffG = decodeRGBChannelsCol[1][h][w] - decodeRGBChannelsCol[1][h][w + size];
                float diffB = decodeRGBChannelsCol[2][h][w] - decodeRGBChannelsCol[2][h][w + size];

                decodeRGBChannelsRow[0][h][w * 2] = avgR;
                decodeRGBChannelsRow[1][h][w * 2] = avgG;
                decodeRGBChannelsRow[2][h][w * 2] = avgB;
                decodeRGBChannelsRow[0][h][w * 2 + 1] = diffR;
                decodeRGBChannelsRow[1][h][w * 2 + 1] = diffG;
                decodeRGBChannelsRow[2][h][w * 2 + 1] = diffB;
            }
        }

        return decodeRGBChannelsRow;
    }

    /**
     * Get RGB Channel Array
     * 
     * @param inputImage input image
     * @return rgb channel array
     */
    private float[][][] getRGBChannelArray(BufferedImage inputImage) {
        float[][][] rgb = new float[3][HEIGHT][WIDTH];
        for (int i = 0; i < HEIGHT; ++i) {
            for (int j = 0; j < WIDTH; ++j) {
                int[] rgbArray = getRGBArray(inputImage.getRGB(j, i));
                rgb[0][i][j] = rgbArray[0];
                rgb[1][i][j] = rgbArray[1];
                rgb[2][i][j] = rgbArray[2];
            }
        }
        return rgb;
    }

    /**
     * Get Buffered Image
     * 
     * @param rgb rgb channels
     * @return buffered image
     */
    private BufferedImage getBufferedImage(float[][][] rgb) {
        BufferedImage outputImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < HEIGHT; ++i) {
            for (int j = 0; j < WIDTH; j++) {
                outputImage.setRGB(j, i, getPixel(rgb[0][i][j], rgb[1][i][j], rgb[2][i][j]));
            }
        }
        return outputImage;
    }

    /**
     * DWT Compression
     * 
     * @param inputImage input image
     * @param level      level of compression
     */
    public void DWTCompression(BufferedImage inputImage, int level) {
        float[][][] rgbChannelArray = getRGBChannelArray(inputImage);
        int waveletLevel = 9;
        while (waveletLevel > 0) {
            rgbChannelArray = encodeImage(rgbChannelArray, waveletLevel--);
        }
        if (level == -1) {
            BufferedImage[] outputImages = new BufferedImage[10];
            for (int l = 0; l <= 9; ++l) {
                float[][][] blackedOut = blackOutHighFreq(rgbChannelArray, l);
                waveletLevel = 0;
                while (waveletLevel < 9) {
                    blackedOut = decodeImage(blackedOut, waveletLevel++);
                }
                BufferedImage outputImage = getBufferedImage(blackedOut);
                outputImages[l] = outputImage;
            }
            showImgs(outputImages);
        } else {
            float[][][] blackedOut = blackOutHighFreq(rgbChannelArray, level);
            while (waveletLevel < 9) {
                blackedOut = decodeImage(blackedOut, waveletLevel++);
            }
            BufferedImage outputImage = getBufferedImage(blackedOut);
            showImgs(new BufferedImage[] { outputImage });
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Expected at least 2 arguments: input image n");
            return;
        }

        try {
            int level = Integer.parseInt(args[1]);
            BufferedImage inputImage = readImageRGB(args[0]);
            HW3 ren = new HW3();
            ren.DWTCompression(inputImage, level);
        } catch (FileNotFoundException e) {
            System.out.println("Image not found");
            return;
        } catch (IOException e) {
            System.out.println("Error reading image");
            return;
        }
    }
}
