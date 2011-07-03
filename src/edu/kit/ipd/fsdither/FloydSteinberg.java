package edu.kit.ipd.fsdither;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Implements the Floyd-Steinberg dithering algorithm.
 * 
 */
public final class FloydSteinberg {
	private final BufferedImage image;
	private final byte[] data;

	/**
	 * Creates a new class instance for dithering the specified image.
	 * 
	 * @param image
	 *            the image to dither
	 */
	public FloydSteinberg(BufferedImage image) {
		this.image = image;
		data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	}

	// Fits value into the interval [0..255]
	private static byte clampToByte(int value) {
		return (byte) Math.max(0, Math.min(255, value));
	}

	/**
	 * Performs Floyd-Steinberg dithering according to
	 * http://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering
	 * 
	 * @param bitsPerChan
	 *            number of bits per channel to reduce to
	 */
	public void dither(int bitsPerChan) {
		int chanValues = 1 << bitsPerChan;

		// Precalculate reduced value for each original channel value
		int[] reduced = new int[256];
		for (int i = 0; i < 256; i++) {
			reduced[i] = ((i / (256 / chanValues))
					* (255 / (chanValues - 1)));
			if (reduced[i] > 255) {
				reduced[i] = 255;
			}
		}

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int p = 3 * (y * image.getWidth() + x);
				int oldR = (int) data[p] & 0xFF;
				int oldG = (int) data[p + 1] & 0xFF;
				int oldB = (int) data[p + 2] & 0xFF;

				int newR = reduced[oldR];
				int newG = reduced[oldG];
				int newB = reduced[oldB];
				data[p] = (byte) newR;
				data[p + 1] = (byte) newG;
				data[p + 2] = (byte) newB;

				propagateError(x + 1, y, oldR - newR, oldG - newG, oldB - newB, 7);
				propagateError(x - 1, y + 1, oldR - newR, oldG - newG, oldB - newB, 3);
				propagateError(x, y + 1, oldR - newR, oldG - newG, oldB - newB, 5);
				propagateError(x + 1, y + 1, oldR - newR, oldG - newG, oldB - newB, 1);
			}
		}
	}

	private void propagateError(int x, int y, int errR, int errG, int errB, int factor) {
		if (x >= image.getWidth() || y >= image.getHeight() || x < 0 || y < 0) {
			return;
		}

		int p = 3 * (y * image.getWidth() + x);
		data[p] = clampToByte(((int) data[p] & 0xFF) + errR * factor / 16);
		data[p + 1] = clampToByte(((int) data[p + 1] & 0xFF) + errG * factor / 16);
		data[p + 2] = clampToByte(((int) data[p + 2] & 0xFF) + errB * factor / 16);
	}
}
