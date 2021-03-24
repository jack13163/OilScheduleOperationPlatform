package opt.easyjmetal.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class JMetalLogger implements Serializable {

	public static final Logger logger = Logger.getLogger(JMetalLogger.class.getName());

	static {
		/*
		 * Configure the loggers with the default configuration. If the
		 * configuration method is called manually, this default configuration
		 * will be called before anyway, leading to 2 configuration calls,
		 * although only the last one is considered. This is a trade off to
		 * ensure that, if this method is not called manually, then it is still
		 * called automatically and as soon as we use jMetalLogger, in order to
		 * have at least the default configuration.
		 */
		try {
			configureLoggers(null);
		} catch (IOException e) {
			throw new RuntimeException(
					"Impossible to configure the loggers in a static way", e);
		}
	}

	/**
	 * This method provides a single-call method to configure the {@link Logger}
	 * instances. A default configuration is considered, enriched with a custom
	 * property file for more convenient logging. The custom file is considered
	 * after the default configuration, so it can override it if necessary. The
	 * custom file might be provided as an argument of this method, otherwise we
	 * look for a file named "jMetal.log.ini". If no custom file is provided,
	 * then only the default configuration is considered.
	 *
	 * @param propertyFile
	 *            the property file to use for custom configuration,
	 *            <code>null</code> to use only the default configuration
	 * @throws IOException
	 */
	public static void configureLoggers(File propertyFile) throws IOException {
		// Prepare default configuration
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		PrintStream printer = new PrintStream(stream);
		printer.println(".level = INFO");
		printer.println("handlers = java.util.logging.FileHandler, java.util.logging.ConsoleHandler");
		printer.println("formatters = java.util.logging.SimpleFormatter");
		printer.println("java.util.logging.SimpleFormatter.format = %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$s: %5$s [%2$s]%6$s%n");

		printer.println("java.util.logging.FileHandler.pattern = jMetal.log");
		printer.println("java.util.logging.FileHandler.level = ALL");

		printer.println("java.util.logging.ConsoleHandler.level = ALL");

		// Retrieve custom configuration
		File defaultFile = new File("jMetal.log.ini");
		if (propertyFile != null) {
			printer.println(FileUtils.readFileToString(propertyFile));
		} else if (defaultFile.exists()) {
			printer.println(FileUtils.readFileToString(defaultFile));
		} else {
			// use only default configuration
		}
		printer.close();

		// Apply configuration
		LogManager manager = LogManager.getLogManager();
		manager.readConfiguration(IOUtils.toInputStream(new String(stream
				.toByteArray(), Charset.forName("UTF-8"))));
		logger.info("Loggers configured with " + propertyFile);
	}
}
