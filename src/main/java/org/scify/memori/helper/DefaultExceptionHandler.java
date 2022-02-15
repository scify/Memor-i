package org.scify.memori.helper;

import io.sentry.Sentry;

public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {

    public static DefaultExceptionHandler instance;

    private DefaultExceptionHandler() {
        Sentry.init(options -> options.setDsn(MemoriConfiguration.getInstance().getDataPackProperty("SENTRY_DSN")));
    }

    public static DefaultExceptionHandler getInstance() {
        if (instance == null)
            instance = new DefaultExceptionHandler();
        return instance;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Sentry.captureException(e);
    }
}
