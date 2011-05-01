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
			System.err.println("Example: java -jar FSDither.jar" + parser.printExample(ExampleMode.ALL));
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
}
