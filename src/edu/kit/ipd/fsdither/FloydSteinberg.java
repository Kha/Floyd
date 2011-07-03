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
		data = ((DataBufferByte) image.getData().getDataBuffer()).getData();
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
		byte[] reduced = new byte[256];
		for (int i = 0; i < 256; i++) {
			reduced[i] = (byte) ((i / (256 / chanValues))
					* (255 / (chanValues - 1)));
			if (reduced[i] > 255) {
				reduced[i] = (byte) 255;
			}
		}

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int p = 3 * (y * image.getWidth() + x);
				byte oldR = data[p];
				byte oldG = data[p + 1];
				byte oldB = data[p + 2];

				byte newR = reduced[oldR + 128];
				byte newG = reduced[oldG + 128];
				byte newB = reduced[oldB + 128];
				data[p] = newR;
				data[p + 1] = newG;
				data[p + 2] = newB;

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
		data[p] = clampToByte(data[p] + errR * factor / 16);
		data[p + 1] = clampToByte(data[p + 1] + errG * factor / 16);
		data[p + 2] = clampToByte(data[p + 2] + errB * factor / 16);
	}
}
