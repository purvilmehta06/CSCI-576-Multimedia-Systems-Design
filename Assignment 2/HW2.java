import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class HW2 {

    // default fixed parameters
    private final static int WIDTH = 640;
    private final static int HEIGHT = 480;
    private final static boolean DEBUG = false;

    // tunable parameters
    private final static int BORDER_FILTER_SIZE = 3;
    private final static int CLUSTER_LIMIT = 2350;
    private final static int PLUS_MINUS_MAX_HUE = 20;
    private final static double WHITE_SATURATION_LIMIT = 0.15;
    private final static double BLACK_VALUE_LIMIT = 0.4;
    private final static int TOTAL_PERCENT_OBJECT_PIXELS = 75;
    private final static double MINIMUM_MATCH_HUES_WITHIN_CLUSTER = 0.8;
    private final static int SATURATION_MAX_HUE_PERCENTAGE_LIMIT = 85;
    private final static int SATURATION_MAX_HUE_VARIATIONS = 20;
    private final static int ALL_SATURATION_AROUND_MAX_HUE = 3;

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
     * Get Hue
     * 
     * @param r red
     * @param g green
     * @param b blue
     */
    private static Integer getHue(int pixel) {
        int[] rgb = getRGBComponents(pixel);
        int r = rgb[0];
        int g = rgb[1];
        int b = rgb[2];

        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        int hue = 0;
        if (max == min) {
            hue = 0;
        } else if (max == r) {
            hue = (60 * (g - b) / (max - min) + 360) % 360;
        } else if (max == g) {
            hue = (60 * (b - r) / (max - min) + 120) % 360;
        } else if (max == b) {
            hue = (60 * (r - g) / (max - min) + 240) % 360;
        }
        return hue;
    }

    /**
     * Get S value
     * 
     * @param pixel
     * @return
     */
    private static double getSaturation(int pixel) {
        int[] rgb = getRGBComponents(pixel);
        double max = Math.max(rgb[0], Math.max(rgb[1], rgb[2]));
        double min = Math.min(rgb[0], Math.min(rgb[1], rgb[2]));
        double s = 0;
        if (max == 0) {
            s = 0;
        } else {
            s = (max - min) / max;
        }
        return s;
    }

    /**
     * Get V value
     * 
     * @param pixel
     * @return
     */
    private static double getValue(int pixel) {
        int[] rgb = getRGBComponents(pixel);
        int max = Math.max(rgb[0], Math.max(rgb[1], rgb[2]));
        return max / 255.0;
    }

    /**
     * Check if the pixel is black
     * 
     * @param pixel the pixel to check
     * @return r,b,g values of the pixel
     */
    private static int[] getRGBComponents(int pixel) {
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;
        return new int[] { red, green, blue };
    }

    /**
     * Show Image
     * 
     * @param scaledImage   scaled image
     * @param originalImage original image
     * @param windowSize    window size in which the zoomed in image is shown
     * @param scalingFactor scaling factor
     */
    private static void showIms(BufferedImage image) {
        JFrame frame = new JFrame();
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel lbIm1 = new JLabel(new ImageIcon(image));

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

    /**
     * Show Rectangles
     * 
     * @param image            input image
     * @param rectangles       rectangles to show
     * @param objectImagePaths object image paths
     */
    private static void showRectangles(BufferedImage image, int[][] rectangles, String[] objectImagePaths) {
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("TimesRoman", Font.PLAIN, 10));
        g2d.setStroke(new BasicStroke(3));
        for (int i = 0; i < rectangles.length; i++) {
            g2d.drawRect(rectangles[i][0], rectangles[i][2], rectangles[i][1] - rectangles[i][0],
                    rectangles[i][3] - rectangles[i][2]);
            g2d.drawString(objectImagePaths[rectangles[i][4]], rectangles[i][0] + 4, rectangles[i][3] - 4);
        }
        showIms(image);
    }

    /**
     * Check if the pixel is green
     * 
     * @param pix the pixel to check
     * @return true if the pixel is green, false otherwise
     */
    private static boolean isGreen(int pix) {
        int[] rgb = getRGBComponents(pix);
        return rgb[0] == 0 && rgb[1] == 255 && rgb[2] == 0;
    }

    /**
     * Check if the pixel is valid
     * 
     * @param x x-coordinate
     * @param y y-coordinate
     * @return true if the pixel is valid, false otherwise
     */
    private static boolean checkForValidPixel(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    /**
     * Check if the pixel is green in the neighborhood
     * 
     * @param image input image
     * @param x     x-coordinate
     * @param y     y-coordinate
     * @return true if the pixel is green in the neighborhood, false otherwise
     */
    private static boolean checkGreenInNeighbor(BufferedImage image, int x, int y) {
        for (int i = x - BORDER_FILTER_SIZE / 2; i <= x + BORDER_FILTER_SIZE / 2; ++i) {
            for (int j = y - BORDER_FILTER_SIZE / 2; j <= y + BORDER_FILTER_SIZE / 2; ++j) {
                if (checkForValidPixel(i, j) && isGreen(image.getRGB(i, j))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Distinguish between black, white and red
     * 
     * @param h hue
     * @param s saturation
     * @param v value
     * @return 0 if red, 1 if white, 2 if black
     */
    private static int distinguishBWR(int h, double s, double v) {
        if (v <= BLACK_VALUE_LIMIT) {
            return 2;
        } else if (s <= WHITE_SATURATION_LIMIT) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Get Hue Histogram
     * 
     * @param inputImage input image
     * @return hue histogram, sorted by number of pixels
     */
    private static int[][] getHueHistogram(BufferedImage inputImage) {
        int[][] hueHistogram = new int[362 * 3][2];
        for (int i = 0; i < 362 * 3; i++) {
            hueHistogram[i][0] = i;
        }
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                int pixel = inputImage.getRGB(i, j);
                int hue = getHue(pixel);
                double s = getSaturation(pixel);
                double v = getValue(pixel);
                if (!checkGreenInNeighbor(inputImage, i, j)) {
                    hueHistogram[hue + 360 * distinguishBWR(hue, s, v)][1]++;
                }
            }
        }
        ArrayList<int[]> hueHistogramList = new ArrayList<int[]>(Arrays.asList(hueHistogram));
        Collections.sort(hueHistogramList, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return o2[1] - o1[1];
            }
        });
        return hueHistogramList.toArray(new int[hueHistogramList.size()][]);
    }

    /**
     * Get Masked Image
     * 
     * @param inputImage input image
     * @param heuSet     hue set to mask
     * @param showImg    show image or not
     * @return masked image
     */
    private static BufferedImage getMaskedImage(BufferedImage inputImage, Set<Integer> hueSet,
            Map<Integer, Set<Integer>> hueMap, boolean showImg) {
        BufferedImage outputImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int w = 0; w < WIDTH; ++w) {
            for (int h = 0; h < HEIGHT; ++h) {
                int pixel = inputImage.getRGB(w, h);
                int hue = getHue(pixel);
                double s = getSaturation(pixel);
                double v = getValue(pixel);
                hue += 360 * distinguishBWR(hue, s, v);
                if (hueMap.containsKey(hue) && hueMap.get(hue).contains((int) (s * 100))) {
                    outputImage.setRGB(w, h, 0xffffff);
                } else {
                    outputImage.setRGB(w, h, 0);
                }
            }
        }
        if (showImg) {
            showIms(outputImage);
        }
        return outputImage;
    }

    /**
     * BFS to find the range of the object
     * 
     * @param inputImage    original image
     * @param image         masked image containing 0 and 1
     * @param sx            starting x coordinate
     * @param sy            starting y coordinate
     * @param visited       visited array
     * @param visitedHueSet visited hue set for this object
     * @return range of the object
     */
    private static int[] BFS(BufferedImage inputImage, BufferedImage image, int sx, int sy,
            boolean[][] visited, Set<Integer> visitedHueSet) {

        int[] range = new int[] { sx, sx, sy, sy, 0 };
        Queue<int[]> queue = new LinkedList<int[]>();
        queue.add(new int[] { sx, sy });
        int[] dir_x = { 1, 0, -1, 0, 1, 1, -1, -1 };
        int[] dir_y = { 0, 1, 0, -1, 1, -1, 1, -1 };

        while (!queue.isEmpty()) {
            int size = queue.size();
            while (size-- > 0) {
                int[] top = queue.remove();
                int x = top[0];
                int y = top[1];

                if (visited[x][y]) {
                    continue;
                }

                int pixel = inputImage.getRGB(x, y);
                int hue = getHue(pixel);
                hue += 360 * distinguishBWR(hue, getSaturation(pixel), getValue(pixel));
                visitedHueSet.add(hue);
                visited[x][y] = true;

                range[4]++;
                range[0] = Math.min(range[0], x);
                range[1] = Math.max(range[1], x);
                range[2] = Math.min(range[2], y);
                range[3] = Math.max(range[3], y);

                for (int k = 0; k < 8; k++) {
                    int new_x = x + dir_x[k];
                    int new_y = y + dir_y[k];

                    if (!checkForValidPixel(new_x, new_y)) {
                        continue;
                    }

                    int[] rgb = getRGBComponents(image.getRGB(new_x, new_y));
                    if (visited[new_x][new_y] || (rgb[0] == 0 && rgb[1] == 0 && rgb[2] == 0)) {
                        continue;
                    } else {
                        queue.add(new int[] { new_x, new_y });
                    }
                }
            }
        }
        return range;
    }

    /**
     * Find Object Locations
     * 
     * @param inputImage   input image
     * @param image        masked image
     * @param objectNumber object number
     * @param hueSet       hue set
     * @param hueMap       hue map
     * @return list of ranges for the object
     */
    private static ArrayList<int[]> findObjectLocations(BufferedImage inputImage, BufferedImage image, int objectNumber,
            Set<Integer> hueSet, Map<Integer, Set<Integer>> hueMap) {
        boolean[][] visited = new boolean[WIDTH][HEIGHT];
        for (boolean[] vis : visited) {
            Arrays.fill(vis, false);
        }

        ArrayList<int[]> ranges = new ArrayList<int[]>();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                int[] rgb = getRGBComponents(image.getRGB(x, y));
                if (rgb[0] == 0 && rgb[1] == 0 && rgb[2] == 0) {
                    continue;
                }
                Set<Integer> visitedHueSet = new HashSet<Integer>();
                int range[] = BFS(inputImage, image, x, y, visited, visitedHueSet);
                visitedHueSet.retainAll(hueMap.keySet());
                if (range[4] > CLUSTER_LIMIT
                        && visitedHueSet.size() >= MINIMUM_MATCH_HUES_WITHIN_CLUSTER * hueSet.size()) {
                    range[4] = objectNumber;
                    ranges.add(range);
                }
            }
        }
        return ranges;
    }

    /**
     * Get Saturation Set
     * 
     * @param image  input image
     * @param hueSet hue set
     * @return saturation set for top hue values from object image
     */
    private static Set<Integer> getSaturationSet(BufferedImage image, Set<Integer> hueSet) {
        int[][] saturationHistogram = new int[362 * 3][2];
        for (int i = 0; i < 362 * 3; i++) {
            saturationHistogram[i][0] = i;
        }
        for (int i = 0; i < WIDTH; ++i) {
            for (int j = 0; j < HEIGHT; ++j) {
                int pixel = image.getRGB(i, j);
                int hue = getHue(pixel);
                hue += 360 * distinguishBWR(hue, getSaturation(pixel), getValue(pixel));
                if (hueSet.contains(hue)) {
                    double saturation = getSaturation(pixel);
                    saturationHistogram[(int) (saturation * 100)][1] += 1;
                }
            }
        }

        ArrayList<int[]> saturationHistogramList = new ArrayList<int[]>(Arrays.asList(saturationHistogram));
        Collections.sort(saturationHistogramList, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return o2[1] - o1[1];
            }
        });
        int[][] temp = saturationHistogramList.toArray(new int[saturationHistogramList.size()][]);

        int totalSaturationPixels = 0;
        for (int j = 0; j < temp.length; j++) {
            if (temp[j][1] != 0) {
                totalSaturationPixels += temp[j][1];
            } else {
                break;
            }
        }
        totalSaturationPixels = totalSaturationPixels * SATURATION_MAX_HUE_PERCENTAGE_LIMIT / 100;
        Set<Integer> saturationSet = new HashSet<Integer>();
        for (int j = 0; j < temp.length; j++) {
            if (totalSaturationPixels > 0) {
                int base = temp[j][0];
                saturationSet.add(base);
                for (int k = 1; k < SATURATION_MAX_HUE_VARIATIONS; k++) {
                    saturationSet.add(base + k);
                    saturationSet.add(base - k);
                }
                totalSaturationPixels -= temp[j][1];
            } else {
                break;
            }
        }
        return saturationSet;
    }

    /**
     * Detect Objects
     * 
     * @param inputImage       input image
     * @param objectImages     a list of object images
     * @param objectImagePaths a list of object image paths
     */
    public void detectObjects(BufferedImage inputImage, BufferedImage[] objectImages, String[] objectImagePaths) {

        if (DEBUG) {
            int[][] inputImageHistogram = getHueHistogram(inputImage);
            System.out.println("Input Image Histogram");
            for (int i = 0; i < inputImageHistogram.length; i++) {
                System.out.println(inputImageHistogram[i][0] + " " +
                        inputImageHistogram[i][1]);
            }
        }

        ArrayList<int[]> rectangleList = new ArrayList<>();
        for (int i = 0; i < objectImages.length; ++i) {
            int[][] objectHistogram = getHueHistogram(objectImages[i]);
            int totalPixels = 0, maxHue = -1;

            if (DEBUG)
                System.out.println("\nObject Histogram: " + (i + 1));
            for (int j = 0; j < objectHistogram.length; j++) {
                if (objectHistogram[j][0] != 120) {
                    totalPixels += objectHistogram[j][1];
                    if (maxHue == -1) {
                        maxHue = objectHistogram[j][0];
                    }
                    if (DEBUG)
                        System.out.println(objectHistogram[j][0] + " " + objectHistogram[j][1] + " " + totalPixels);
                }
            }
            int threshold = (totalPixels * TOTAL_PERCENT_OBJECT_PIXELS) / 100;

            Set<Integer> hueSet = new HashSet<Integer>();
            for (int j = 0; j < objectHistogram.length; j++) {
                if (objectHistogram[j][0] != 120) {
                    hueSet.add(objectHistogram[j][0]);
                    threshold -= objectHistogram[j][1];
                }
                if (threshold <= 0) {
                    break;
                }
            }

            if (DEBUG) {
                System.out.println("\nOriginal Selected Hues: " + (i + 1));
                for (Integer hue : hueSet) {
                    System.out.print(hue + " ");
                }
                System.out.println();
            }

            Map<Integer, Set<Integer>> hueMap = new HashMap<Integer, Set<Integer>>();
            for (Integer hue : hueSet) {
                Set<Integer> tempHueSet = new HashSet<Integer>();
                if (hue == maxHue) {
                    for (int j = maxHue - PLUS_MINUS_MAX_HUE; j < maxHue + PLUS_MINUS_MAX_HUE; j++) {
                        tempHueSet.add((j + 360) % 360);
                    }
                } else {
                    tempHueSet.add(hue);
                }
                Set<Integer> saturationSet = getSaturationSet(objectImages[i], tempHueSet);
                if (hue == maxHue) {
                    for (int j = maxHue - PLUS_MINUS_MAX_HUE; j < maxHue + PLUS_MINUS_MAX_HUE; j++) {
                        if (Math.abs(j - maxHue) <= ALL_SATURATION_AROUND_MAX_HUE) {
                            Set<Integer> temp = new HashSet<Integer>();
                            for (int k = 0; k <= 100; ++k) {
                                temp.add(k);
                            }
                            hueMap.put((j + 360) % 360, temp);
                        } else {
                            hueMap.put((j + 360) % 360, saturationSet);
                        }
                    }
                }
                if (!hueMap.containsKey(hue)) {
                    hueMap.put(hue, saturationSet);
                }
            }

            BufferedImage maskedImage = getMaskedImage(inputImage, hueSet, hueMap, true);
            ArrayList<int[]> identifiedObjects = findObjectLocations(inputImage, maskedImage, i, hueSet, hueMap);
            rectangleList.addAll(identifiedObjects);
        }
        showRectangles(inputImage, rectangleList.toArray(new int[rectangleList.size()][]), objectImagePaths);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Expected at least 2 arguments: input image, object1, object2,....");
            return;
        }

        try {
            String inputImagePath = args[0];
            String[] objectImagePaths = Arrays.copyOfRange(args, 1, args.length);
            for (int i = 0; i < objectImagePaths.length; i++) {
                objectImagePaths[i] = objectImagePaths[i].substring(objectImagePaths[i].lastIndexOf("/") + 1);
            }

            BufferedImage inputImage = readImageRGB(inputImagePath);
            BufferedImage[] objectImages = new BufferedImage[args.length - 1];
            for (int i = 1; i < args.length; i++) {
                objectImages[i - 1] = readImageRGB(args[i]);
            }

            HW2 ren = new HW2();
            ren.detectObjects(inputImage, objectImages, objectImagePaths);
        } catch (FileNotFoundException e) {
            System.out.println("Image not found");
            return;
        } catch (IOException e) {
            System.out.println("Error reading image");
            return;
        }
    }
}
