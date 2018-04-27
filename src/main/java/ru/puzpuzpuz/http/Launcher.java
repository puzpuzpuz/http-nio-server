package ru.puzpuzpuz.http;

import ru.puzpuzpuz.http.util.Logger;

import static ru.puzpuzpuz.http.util.Constants.SETTINGS_FILE_DEFAULT;

/**
 * The launcher class for the HTTP server. Loads settings and starts the {@link HttpServer}.
 */
public class Launcher {

    private static final Logger LOGGER = new Logger(Launcher.class.getName());

    public static void main(String[] args) {
        String settingsPath = SETTINGS_FILE_DEFAULT;
        if (args.length > 0) {
            settingsPath = args[0];
            LOGGER.info("Found path for settings file in arguments: " + settingsPath);
        }
        ServerSettings settings = new ServerSettingsLoader().load(settingsPath);
        LOGGER.info("Loaded settings: " + settings);

        LOGGER.info("Starting server");
        HttpServer server = new HttpServer(settings);
        new Thread(server, "http-server").start();
    }

}
