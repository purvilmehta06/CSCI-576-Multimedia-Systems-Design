import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

public class HW1 {

    // default image width and height
    private final static int WIDTH = 1920 * 4;
    private final static int HEIGHT = 1080 * 4;
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
     * Sample Image
     * 
     * @param image         the image to be sampled
     * @param scalingFactor the scaling factor
     * @param antiAliasing  whether anti-aliasing is enabled
     * @return the sampled image
     */
    private static BufferedImage sampleImageRGB(BufferedImage image, float scalingFactor, int antiAliasing) {
        int scaledImageHeight = (int) (HEIGHT * scalingFactor);
        int scaledImageWidth = (int) (WIDTH * scalingFactor);
        BufferedImage newImage = new BufferedImage(scaledImageWidth, scaledImageHeight, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < scaledImageWidth; x++) {
            for (int y = 0; y < scaledImageHeight; y++) {
                int pix;

                // if anti-aliasing is on, we need to take local average of the 3x3 neighborhood
                if (antiAliasing == 1) {
                    int count = 0, r = 0x00, g = 0x00, b = 0x00;
                    int initX = (int) (x / scalingFactor);
                    int initY = (int) (y / scalingFactor);

                    // looping from -1 to 1 in both x and y directions, to get the 3x3 neighborhood
                    // when filter size is 3. This can be generalized to any filter size
                    for (int dx = -(int) FILTER_SIZE / 2; dx < (int) (FILTER_SIZE / 2) + 1; dx++) {
                        for (int dy = -(int) FILTER_SIZE / 2; dy < (int) (FILTER_SIZE / 2) + 1; dy++) {
                            int newX = initX + dx;
                            int newY = initY + dy;

                            if (newX < 0 || newX >= image.getWidth() || newY < 0 || newY >= image.getHeight())
                                continue;

                            int samplePix = image.getRGB(newX, newY);
                            r += (samplePix >> 16 & 0x00ff);
                            g += (samplePix >> 8 & 0x0000ff);
                            b += (samplePix & 0x000000ff);
                            count += 1;
                        }
                    }
                    byte newR = (byte) (Math.min(255, r / count));
                    byte newG = (byte) (Math.min(255, g / count));
                    byte newB = (byte) (Math.min(255, b / count));
                    pix = 0xff000000 | ((newR & 0xff) << 16) | ((newG & 0xff) << 8) | (newB & 0xff);
                } else {
                    // since anti-aliasing is not enabled, we just need to take the pixel
                    // value from the original image
                    pix = image.getRGB((int) (x / scalingFactor), (int) (y / scalingFactor));
                }
                newImage.setRGB(x, y, pix);
            }
        }
        return newImage;
    }

    // ref: https://stackoverflow.com/a/19327237
    /**
     * Copy Image
     * 
     * @param source the image to be copied
     * @return the copied image
     */
    private static BufferedImage copyImage(BufferedImage source) {
        BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics g = b.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }

    /**
     * Show Overlay Image
     * 
     * @param x             x coordinate of the mouse
     * @param y             y coordinate of the mouse
     * @param scaledImage   scaled image
     * @param originalImage original image
     * @param imageCopy     copy of the scaled image
     * @param windowSize    window size in which the zoomed in image is shown
     * @param scalingFactor scaling factor
     * @param frame         the frame in which the image is shown
     */
    private static void showOverlayImage(int x, int y, BufferedImage scaledImage, BufferedImage originalImage,
            BufferedImage imageCopy, int windowSize, float scalingFactor, JFrame frame) {

        // top left point on the scaled image where the window starts
        int scaledX = (int) (x - windowSize / 2);
        int scaledY = (int) (y - windowSize / 2);

        // corner cases to resize the window when it goes out of bounds
        int width = windowSize, height = windowSize;
        int flagX = 0, flagY = 0;
        if (scaledX < 0) {
            width = windowSize + scaledX;
            scaledX = 0;
            flagX = 1;
        }

        if (scaledY < 0) {
            height = windowSize + scaledY;
            scaledY = 0;
            flagY = 1;
        }

        if (scaledX + windowSize > scaledImage.getWidth()) {
            width = scaledImage.getWidth() - scaledX;
        }

        if (scaledY + windowSize > scaledImage.getHeight()) {
            height = scaledImage.getHeight() - scaledY;
        }

        // top left point on the original image where the window starts
        int originalX = (int) ((int) (x / scalingFactor) - windowSize / 2);
        int originalY = (int) ((int) (y / scalingFactor) - windowSize / 2);

        // if we have resized the window, we need to adjust the originalX and originalY
        if (flagX == 1) {
            originalX = (int) ((int) (x / scalingFactor) - (width - windowSize / 2));
        }
        if (flagY == 1) {
            originalY = (int) ((int) (y / scalingFactor) - (height - windowSize / 2));
        }
        originalX = Math.max(0, Math.min(originalImage.getWidth() - width, originalX));
        originalY = Math.max(0, Math.min(originalImage.getHeight() - height, originalY));

        // cropping the original image to get the window and then drawing it on the
        // scaled image
        BufferedImage croppedImage = originalImage.getSubimage(originalX, originalY, width, height);
        scaledImage.getGraphics().drawImage(imageCopy, 0, 0, null);
        scaledImage.getGraphics().drawImage(croppedImage, scaledX, scaledY, null);
        frame.repaint();
    }

    /**
     * Show Image
     * 
     * @param scaledImage   scaled image
     * @param originalImage original image
     * @param windowSize    window size in which the zoomed in image is shown
     * @param scalingFactor scaling factor
     */
    public void showIms(BufferedImage scaledImage, BufferedImage originalImage, int windowSize, float scalingFactor) {
        // making copy of the input/scaled image so that we can reset the image when
        // control key is released
        final BufferedImage imageCopy = copyImage(scaledImage);

        JFrame frame = new JFrame();
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel lbIm1 = new JLabel(new ImageIcon(scaledImage));

        lbIm1.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
                    showOverlayImage(e.getX(), e.getY(), scaledImage, originalImage, imageCopy, windowSize,
                            scalingFactor,
                            frame);
                }
            }
        });

        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                Point cursorLocation = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(cursorLocation, lbIm1);
                if (e.getKeyCode() == KeyEvent.VK_CONTROL && cursorLocation.x >= 0 && cursorLocation.y >= 0
                        && cursorLocation.x < scaledImage.getWidth() && cursorLocation.y < scaledImage.getHeight()) {
                    showOverlayImage(cursorLocation.x, cursorLocation.y, scaledImage, originalImage, imageCopy,
                            windowSize,
                            scalingFactor,
                            frame);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    scaledImage.getGraphics().drawImage(imageCopy, 0, 0, null);
                    frame.repaint();
                }
            }
        });

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 1;
        frame.getContentPane().add(lbIm1, c);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Expected 4 arguments: image path, scaling factor, anti-aliasing, window_size");
            return;
        }

        String imgPath = args[0];
        float scalingFactor = Float.parseFloat(args[1]);
        int antiAliasing = Integer.parseInt(args[2]);
        int windowSize = Integer.parseInt(args[3]);

        try {
            // read and sample image
            BufferedImage inputImage = readImageRGB(imgPath);
            BufferedImage sampledImage = sampleImageRGB(inputImage, scalingFactor, antiAliasing);

            // rendering the output sampled image with window w (zoom in) logic
            HW1 ren = new HW1();
            ren.showIms(sampledImage, inputImage, windowSize, scalingFactor);
        } catch (FileNotFoundException e) {
            System.out.println("Image not found");
            return;
        } catch (IOException e) {
            System.out.println("Error reading image");
            return;
        }
    }
}
