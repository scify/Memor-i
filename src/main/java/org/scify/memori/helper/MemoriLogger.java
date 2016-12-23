package org.scify.memori.helper;

import java.io.IOException;
import java.util.logging.*;

/**
 * Class that handles logging messages within the application
 */
public class MemoriLogger {
    public static final Logger LOGGER = Logger.getLogger(MemoriLogger.class.getName());

    public static void initLogger() {

        try {
            // initialise a file and a console handler
            Handler fileHandler = new FileHandler("./memori.log");
            Handler consoleHandler = new ConsoleHandler();

            LOGGER.addHandler(consoleHandler);
            LOGGER.addHandler(fileHandler);
            consoleHandler.setLevel(Level.ALL);
            fileHandler.setLevel(Level.ALL);
            LOGGER.setLevel(Level.ALL);
            LOGGER.config("Configuration done.");
            LOGGER.log(Level.FINE, "Fine level logged");
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "Error occurred in FileHandler.", ioe.getMessage());
        }
    }

//    public static void log(String message, Level level) {
//        LOGGER.log(level, message);
//    }

}
