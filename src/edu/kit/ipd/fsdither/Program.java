package edu.kit.ipd.fsdither;

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
		if (config.isErrorFree()) {

		}
	}
}
