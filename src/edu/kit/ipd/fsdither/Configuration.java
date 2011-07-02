package edu.kit.ipd.fsdither;

import java.io.File;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ExampleMode;
import org.kohsuke.args4j.Option;

/**
 * This class handles the program arguments.
 * 
 * @author Thomas Karcher
 * 
 */
public class Configuration {

	@Option(name = "-s", aliases = { "--source" }, required = true,
			usage = "Sets the file where the input is read from.")
	private File source;

	@Option(name = "-t", aliases = { "--target" }, required = true,
			usage = "Sets the file where the output is written to.")
	private File target;

	@Option(
			name = "-c",
			aliases = { "--colordepth" },
			required = false,
			usage = "Sets the colordepth of the target image; possible values: 3, 6, 9, 12, 15, 18."
					+ "Default: 3 (= 8 colors)")
	private int targetColorDepth = 18;

	@Option(name = "-n", aliases = { "--numthreads" }, required = false,
			usage = "Sets the number of threads to use. Default: 1")
	private int numThreads = 1;

	private boolean errorFree = false;

	/**
	 * Parses the command line arguments using <a
	 * href="http://args4j.java.net/">args4j</a> .
	 * 
	 * @param args
	 *            The program arguments:
	 *            <ul>
	 *            <li>-s (--source) FILE</li>
	 *            <li>-t (--target) FILE</li>
	 *            <li>-c (--colordepth) INT</li>
	 *            <li>-n (--numthreads) INT</li>
	 *            </ul>
	 */
	public Configuration(String... args) {
		CmdLineParser parser = new CmdLineParser(this);
		parser.setUsageWidth(80);
		try {
			parser.parseArgument(args);
			errorFree = true;
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("java -jar FSDither.jar [options...]");
			parser.printUsage(System.err);
			System.err.println();
			System.err.println("Example: java -jar FSDither.jar"
					+ parser.printExample(ExampleMode.ALL));
		}
	}

	/**
	 * Returns whether the parameters could be parsed without an error.
	 * 
	 * @return <code>true</code> if no error occurred.
	 */
	public boolean isErrorFree() {
		return errorFree;
	}

	/**
	 * Returns the source file.
	 * 
	 * @return The source file.
	 */
	public File getSource() {
		return source;
	}

	/**
	 * Returns the target file.
	 * 
	 * @return The target file.
	 */
	public File getTarget() {
		return target;
	}

	/**
	 * Returns the number of threads to use.
	 * 
	 * @return The number of threads.
	 */
	public int getNumThreads() {
		return numThreads;
	}

	/**
	 * Returns the colordepth of the target image.
	 * 
	 * @return The number of bits.
	 */
	public int getTargetColorDepth() {
		return targetColorDepth;
	}
}
