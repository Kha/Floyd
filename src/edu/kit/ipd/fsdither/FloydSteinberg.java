package edu.kit.ipd.fsdither;

import java.awt.image.BufferedImage;

/**
 * Implements the Floyd-Steinberg dithering algorithm.
 * 
 */
public final class FloydSteinberg {
  private FloydSteinberg() {

  }

  // The 3x3 distribution matrix of the algorithm
  private static final double[][] DISTRIBUTION = {
      { 0, 0, 0 },
      { 0, 0, 7. / 16 },
      { 3. / 16, 5. / 16, 1. / 16 }
  };

  // Fits value into the interval [0..255]
  private static int clampToByte(int value) {
    return Math.max(0, Math.min(255, value));
  }

  // Extracts RGB channel values from color.
  private static int[] colorToRGB(int color) {
    return new int[] { (color >>> 16) & 0xFF, (color >>> 8) & 0xFF,
        color & 0xFF };
  }

  /**
   * Performs Floyd-Steinberg dithering according to
   * http://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering
   * 
   * @param image
   *          the image to dither
   * @param bitsPerChan
   *          number of bits per channel to reduce to
   */
  public static void floydSteinbergDither(BufferedImage image,
      int bitsPerChan) {
    int chanValues = 1 << bitsPerChan;
    double colorsPerChanValue = 256.0 / chanValues;

    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        int[] rgb = colorToRGB(image.getRGB(x, y));
        int[] newPixel = new int[3];

        // Reduce each channel
        for (int channel = 0; channel < 3; channel++) {
          newPixel[channel] = (int) (Math.floor(rgb[channel]
              / colorsPerChanValue) * 255 / (chanValues - 1));
        }
        image.setRGB(x, y, rgbToColor(newPixel));

        // propagate reduction error
        for (int dy = -1; dy <= 1; dy++) {
          for (int dx = -1; dx <= 1; dx++) {
            int y2 = y + dy;
            int x2 = x + dx;
            if (y2 >= 0 && y2 < image.getHeight() && x2 >= 0
                && x2 < image.getWidth()) {
              int[] rgb2 = colorToRGB(image.getRGB(x2, y2));
              for (int channel = 0; channel < 3; channel++) {
                rgb2[channel] = clampToByte(rgb2[channel]
                    + (int) ((rgb[channel] - newPixel[channel]) * DISTRIBUTION[dy + 1][dx + 1]));
              }
              image.setRGB(x2, y2, rgbToColor(rgb2));
            }
          }
        }
      }
    }
  }

  // Combines RGB channel values into color value.
  private static int rgbToColor(int[] rgb) {
    return rgb[0] << 16 | rgb[1] << 8 | rgb[2];
  }
}
