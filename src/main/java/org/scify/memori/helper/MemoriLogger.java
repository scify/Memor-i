package org.scify.memori.helper;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

/**
 * Class that handles logging messages within the application
 */
public class MemoriLogger {

    public static final Logger LOGGER = Logger.getLogger(MemoriLogger.class.getName());

    public static void initLogger() {
        try {
            boolean append = true;
            // initialise a file and a console handler
            String logFilePath = Utils.isWindows() ? System.getenv("APPDATA") : System.getProperty("user.home") + ".local/share";
            logFilePath += "/Memor-i";
            String logFileName = "memori.log";
            File file = new File(logFilePath, logFileName);
            if (!file.exists()) {
                file = new File(logFilePath + "/" + logFileName);
                File fileLck = new File(logFilePath + "/" + logFileName + ".lck");
                file.getParentFile().mkdirs(); // Will create parent directories if not exists
                file.createNewFile();
                fileLck.getParentFile().mkdirs(); // Will create parent directories if not exists
                fileLck.createNewFile();
            }
            Handler fileHandler = new FileHandler(logFilePath + "/" + logFileName, append);
            Handler consoleHandler = new ConsoleHandler();
            LOGGER.addHandler(consoleHandler);
            LOGGER.addHandler(fileHandler);
            consoleHandler.setLevel(Level.ALL);
            fileHandler.setLevel(Level.ALL);
            LOGGER.config("Configuration done.");

        } catch (IOException e) {
            DefaultExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
        }
    }


}
