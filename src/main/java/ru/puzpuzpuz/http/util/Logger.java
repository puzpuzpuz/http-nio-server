package ru.puzpuzpuz.http.util;

import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * A very primitive wrapper for JUL logger. Registers it's own shutdown-friendly {@link LogManager} that keeps
 * outputting after shutdown signal.
 */
public class Logger {

    static {
        // must be called before any Logger method is used
        System.setProperty("java.util.logging.manager", ShutdownFriendlyLogManager.class.getName());
    }

    private final java.util.logging.Logger _logger;

    public Logger(String name) {
        this._logger = java.util.logging.Logger.getLogger(name);
    }

    public void info(String msg) {
        _logger.log(Level.INFO, msg);
    }

    public void warn(String msg) {
        _logger.log(Level.WARNING, msg);
    }

    public void warn(String msg, Throwable throwable) {
        _logger.log(Level.WARNING, msg, throwable);
    }

    public void error(String msg, Throwable throwable) {
        _logger.log(Level.SEVERE, msg, throwable);
    }

    /**
     * Resets all settings of JUL LogManager.
     */
    public static void resetFinally() {
        ShutdownFriendlyLogManager.resetFinally();
    }

    public static class ShutdownFriendlyLogManager extends LogManager {

        static ShutdownFriendlyLogManager instance;

        public ShutdownFriendlyLogManager() {
            instance = this;
        }

        @Override
        public void reset() {
            // don't reset yet
        }

        private void reset0() {
            super.reset();
        }

        static void resetFinally() {
            instance.reset0();
        }
    }

}
