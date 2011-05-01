package edu.kit.ipd.fsdither;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Main class of the program.
 * 
 */
public class Program {

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
	}
}
