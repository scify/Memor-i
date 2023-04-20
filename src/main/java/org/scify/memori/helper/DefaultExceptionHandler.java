package org.scify.memori.helper;

import io.sentry.Sentry;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;

public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {

    public static DefaultExceptionHandler instance;

    private DefaultExceptionHandler() {
        Sentry.init(options -> {
            options.setDsn(MemoriConfiguration.getInstance().getDataPackProperty("SENTRY_DSN"));
            // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
            // We recommend adjusting this value in production.
            options.setTracesSampleRate(1.0);
            // When first trying Sentry it's good to see what the SDK is doing:
            // options.setDebug(true);
        });
    }

    public static DefaultExceptionHandler getInstance() {
        if (instance == null)
            instance = new DefaultExceptionHandler();
        return instance;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Sentry.captureException(e);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString();
        MemoriLogger.LOGGER.log(Level.SEVERE, "EXCEPTION: " + e.getMessage() + "\n" + e.getLocalizedMessage() + "\n" + sStackTrace);
    }
}
