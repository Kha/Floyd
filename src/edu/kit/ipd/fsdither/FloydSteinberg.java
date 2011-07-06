package edu.kit.ipd.fsdither;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Implements the Floyd-Steinberg dithering algorithm.
 * 
 */
public final class FloydSteinberg {
	private final BufferedImage image;
	// raw data of the 24-bit image for superior access time
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

	// Because of Java's maniac omission of an unsigned byte type,
	// we need an extra mask to get the full range of 0.255 into an int
	private static int unsignedByteToInt(byte b) {
		return (int) b & 0xFF;
	}

	// number of columns between synchronization points of two adjacent threads
	private static final int SYNC_FREQ = 30;

	// index of the next line to be processesed
	AtomicInteger nextLine;
	// column positions of the individual threads. linePositions[y]=x means the
	// first SYNQ_FREQ * x pixels of line y have been processed.
	AtomicIntegerArray linePositions;
	// precalculated reduced values for each original channel value
	int[] reduced;

	/**
	 * Performs Floyd-Steinberg dithering according to
	 * http://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering
	 * 
	 * @param bitsPerChan
	 *            number of bits per channel to reduce to
	 * @param numThreads
	 *            number of threads to use
	 */
	public void dither(int bitsPerChan, int numThreads) {
		int chanValues = 1 << bitsPerChan;

		// Precalculate reduced value for each original channel value
		reduced = new int[256];
		for (int i = 0; i < 256; i++) {
			reduced[i] = ((i / (256 / chanValues)) * (255 / (chanValues - 1)));
			if (reduced[i] > 255) {
				reduced[i] = 255;
			}
		}

		nextLine = new AtomicInteger(0);
		linePositions = new AtomicIntegerArray(image.getHeight());

		// Spawn (numThreads-1) additional threads
		Thread[] threads = new Thread[numThreads - 1];
		for (int i = 0; i < numThreads - 1; i++) {
			threads[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					doWork();
				}
			});
			threads[i].start();
		}

		// Let the current thread work on the data, too
		doWork();

		// Wait for completion
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				throw new Error(e); // this shouldn't happen
			}
		}
	}

	// Called numThreads times
	private void doWork() {
		int y;
		// Get next line to be dithered in a thread-safe but efficient manner
		while ((y = nextLine.getAndIncrement()) < image.getHeight()) {
			for (int x = 0; x < image.getWidth(); x++) {
				// Before entering a new block of SYNC_FREQ pixels, spin-wait
				// for thread above me
				if (y > 0 && x % SYNC_FREQ == 0) {
					while (linePositions.get(y - 1) <= x / SYNC_FREQ) {
						doNothingParticular();
					}
					// thread at line y-1 has left the block, lets get to work
				}

				int p = 3 * (y * image.getWidth() + x);
				int oldR = unsignedByteToInt(data[p]);
				int oldG = unsignedByteToInt(data[p + 1]);
				int oldB = unsignedByteToInt(data[p + 2]);

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

				if (x > 0 && x % SYNC_FREQ == 0) {
					// advanced to next block
					linePositions.incrementAndGet(y);
				}
			}
			// finished all blocks of line y
			linePositions.set(y, Integer.MAX_VALUE);
		}
	}

	private void propagateError(int x, int y, int errR, int errG, int errB, int factor) {
		if (x >= image.getWidth() || y >= image.getHeight() || x < 0 || y < 0) {
			return;
		}

		int p = 3 * (y * image.getWidth() + x);
		data[p] = clampToByte(unsignedByteToInt(data[p]) + errR * factor / 16);
		data[p + 1] = clampToByte(unsignedByteToInt(data[p + 1]) + errG * factor / 16);
		data[p + 2] = clampToByte(unsignedByteToInt(data[p + 2]) + errB * factor / 16);
	}

	// Dummy method to avoid CheckStyle error on empty spin-wait loop
	private void doNothingParticular() {
	}
}
