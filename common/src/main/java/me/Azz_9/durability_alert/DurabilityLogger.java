package me.Azz_9.durability_alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static me.Azz_9.durability_alert.Constants.MOD_NAME;

public class DurabilityLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    private static final String PREFIX = "[" + MOD_NAME + "] ";

    private DurabilityLogger() {
    }

    public static void info(String message, Object... args) {
        LOGGER.info(PREFIX + message, args);
    }

    public static void warn(String message, Object... args) {
        LOGGER.warn(PREFIX + message, args);
    }

    public static void error(String message, Object... args) {
        LOGGER.error(PREFIX + message, args);
    }

    public static void debug(String message, Object... args) {
        LOGGER.debug(PREFIX + message, args);
    }
}
