package net.tywrapstudios.blossombridge.api.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Manages the Configuration files for a mod.
 * @param <T> The {@link ConfigClass} that the {@link ConfigManager} will manage.
 * @author Tiazzz
 */
public class ConfigManager<T extends ConfigClass> {
    private final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);
    private final Jankson jankson;
    private final Class<T> configClass;
    private final File configFile;
    private T configInstance;

    /**
     * Constructor for a {@link ConfigManager}.
     * @param configClass The {@link ConfigClass} that the {@link ConfigManager} will manage.
     * @param configFile The {@link File} that the {@link ConfigManager} will use to save and load the configuration. Note that this HAS to be a JSON5 file.
     */
    public ConfigManager(Class<T> configClass, File configFile) {
        File finalConfigFile;
        this.jankson = Jankson.builder().build();
        this.configClass = configClass;
        LOGGER.debug("Checking file extension of {}", configFile.getName());
        if (!configFile.getName().endsWith(".json5")) {
            throw new InvalidConfigFileException("Config file must have a .json5 extension: " + configFile.getName());
        } else {
            finalConfigFile = configFile;
        }
        this.configFile = finalConfigFile;
    }

    /**
     * Loads the configuration from a file. If the file does not exist, creates a default config.
     */
    public void loadConfig() {
        try {
            if (!configFile.exists()) {
                // Create a default configuration if the file doesn't exist
                LOGGER.debug("Creating new config file for class: {}", configClass.getName());
                this.configInstance = configClass.getDeclaredConstructor().newInstance();
                saveConfig();
                return;
            }

            // Read the file and deserialize
            try {
                this.configInstance = jankson.fromJson(
                        jankson.load(configFile),
                        configClass
                );

                if (this.configInstance != null) {
                    this.configInstance.validate();
                }
            } catch (Exception e){
                throw new InvalidConfigFileException("Invalid config file: " + configFile.getName(), e);
            }

        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.error("Something went wrong while loading config file: {}", configFile.getName());
            e.printStackTrace();
        }
    }

    /**
     * Saves the current configuration instance to the file.
     */
    public void saveConfig() {
        try (FileWriter writer = new FileWriter(configFile)) {
            String json = jankson.toJson(configInstance).toJson(JsonGrammar.JANKSON);
            writer.write(json);
            LOGGER.debug("Saved to config file: {}", configFile.getName());
        } catch (IOException e) {
            LOGGER.error("Something went wrong while saving config file: {}", configFile.getName());
            e.printStackTrace();
        }
    }

    /**
     * Returns the configuration file as a JSON string.
     * @param comments Whether to include comments in the JSON string.
     * @param newlines Whether to include newlines in the JSON string.
     * @return The configuration file as a JSON string.
     */
    public String getConfigJsonAsString(boolean comments, boolean newlines) {
        return jankson.toJson(configInstance).toJson(comments, newlines).replace("\t", "  ");
    }

    /**
     * Returns the current configuration instance.
     */
    public T getConfig() {
        return configInstance;
    }
}

