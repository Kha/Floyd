package edu.kit.ipd.fsdither;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Main class of the program.
 * 
 */
public final class Program {
	private Program() {

	}

	/**
	 * Entry point of the program.
	 * 
	 * @param args
	 *            command line arguments - see Configuration.java
	 */
	public static void main(String[] args) {
		Configuration config = new Configuration(args);
		if (!config.isErrorFree()) {
			return;
		}

		BufferedImage input;
		try {
			input = ImageIO.read(config.getSource());
		} catch (IOException e) {
			System.out.println("Input file couldn't be read: " + e.getMessage());
			return;
		}

		floydSteinbergDither(input);

		try {
			ImageIO.write(input, "PNG", config.getTarget());
		} catch (IOException e) {
			System.out.println("Output file couldn't be written: " + e.getMessage());
			return;
		}
	}

	// The 3x3 distribution matrix of the algorithm
	private static final double[][] DISTRIBUTION = {
			{ 0, 0, 0 },
			{ 0, 0, 7. / 16 },
			{ 3. / 16, 5. / 16, 1. / 16 }
	};

	// See http://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering
	private static void floydSteinbergDither(BufferedImage image) {
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int[] rgb = colorToRGB(image.getRGB(x, y));
				int[] newPixel = new int[3];

				// Reduce each channel to 1 bit
				for (int channel = 0; channel < 3; channel++) {
					newPixel[channel] = (rgb[channel] + 128) / 256 * 255;
				}
				image.setRGB(x, y, rgbToColor(newPixel));

				// propagate reduction error
				for (int dy = -1; dy <= 1; dy++) {
					for (int dx = -1; dx <= 1; dx++) {
						int y2 = y + dy;
						int x2 = x + dx;
						if (y2 >= 0 && y2 < image.getHeight() && x2 >= 0 && x2 < image.getWidth()) {
							int[] rgb2 = colorToRGB(image.getRGB(x2, y2));
							for (int channel = 0; channel < 3; channel++) {
								rgb2[channel] = clampToByte(rgb2[channel]
										+ (int) ((rgb[channel] - newPixel[channel]) * DISTRIBUTION[dy + 1][dx + 1])
										);
							}
							image.setRGB(x2, y2, rgbToColor(rgb2));
						}
					}
				}
			}
		}
	}

	// Extracts RGB channel values from color.
	private static int[] colorToRGB(int color) {
		return new int[] { (color >>> 16) & 0xFF, (color >>> 8) & 0xFF, color & 0xFF };
	}

	// Combines RGB channel values into color value.
	private static int rgbToColor(int[] rgb) {
		return rgb[0] << 16 | rgb[1] << 8 | rgb[2];
	}

	// Fits value into the interval [0..255]
	private static int clampToByte(int value) {
		return Math.max(0, Math.min(255, value));
	}
}
