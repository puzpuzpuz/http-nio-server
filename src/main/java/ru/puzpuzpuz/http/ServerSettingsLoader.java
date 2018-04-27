package ru.puzpuzpuz.http;

import ru.puzpuzpuz.http.util.Logger;
import ru.puzpuzpuz.http.util.PropertiesReader;

import java.io.File;
import java.io.IOException;

import static ru.puzpuzpuz.http.util.Constants.*;

/**
 * A loader class for server settings. Loads settings from the given file with a fallback to default values.
 */
class ServerSettingsLoader {

    private static final Logger LOGGER = new Logger(Launcher.class.getName());

    public ServerSettings load(String settingsPath) {
        int port = SETTINGS_PORT_DEFAULT;
        int sessionTimeoutSecs = SETTINGS_SESSION_TIMEOUT_SECS_DEFAULT;
        int maxConnections = SETTINGS_MAX_CONNECTIONS_DEFAULT;
        String wwwRoot = SETTINGS_WWW_ROOT_DEFAULT;

        try {
            PropertiesReader propertiesReader = PropertiesReader.init(settingsPath);

            Integer portSetting = propertiesReader.readIntKey(SETTINGS_PORT);
            if (portSetting != null) {
                port = portSetting;
            }

            Integer sessionTimeoutSecsSetting = propertiesReader.readIntKey(SETTINGS_SESSION_TIMEOUT_SECS);
            if (sessionTimeoutSecsSetting != null) {
                sessionTimeoutSecs = sessionTimeoutSecsSetting;
            }

            Integer maxConnectionsSetting = propertiesReader.readIntKey(SETTINGS_MAX_CONNECTIONS);
            if (maxConnectionsSetting != null) {
                maxConnections = maxConnectionsSetting;
            }

            String wwwRootSetting = propertiesReader.readStringKey(SETTINGS_WWW_ROOT);
            if (wwwRootSetting != null) {
                wwwRoot = wwwRootSetting;
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to settings from file. Falling back to defaults");
        }

        // use local folder if www root is not specified
        if (wwwRoot == null || wwwRoot.trim().isEmpty()) {
            wwwRoot = new File(".").getAbsolutePath();
        }

        return new ServerSettings(port, wwwRoot, sessionTimeoutSecs, maxConnections);
    }

}
