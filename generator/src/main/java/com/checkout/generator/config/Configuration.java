package com.checkout.generator.config;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class Configuration {
    private static final String CONFIG_FILE_PATH = "generator.properties";
    private static final String ABSOLUTE_CONFIG_FILE_PATH = "/etc/checkout/generator/generator.properties";
    private static Configuration instance;
    private final Properties properties;

    public static Configuration getInstance(){
        if (instance == null) {
            try {
                instance = new Configuration();
            } catch (IOException e) {
                log.error("Could not load configuration", e);
                throw new RuntimeException("Could not load configuration", e);
            }
        }

        return instance;
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    private Configuration() throws IOException {
        try (InputStream inputStream = getConfigFileInputStream()) {
            if (inputStream == null) {
                log.error("Could not find configuration file");
                throw new IOException("Could not find configuration file");
            }
            properties = new Properties();
            properties.load(inputStream);
        }
    }

    private InputStream getConfigFileInputStream() {
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(getAbsoluteConfigFilePath());
            log.info("Loading configuration from absolute path: {}", getAbsoluteConfigFilePath());
        } catch (IOException e) {
            inputStream =
                    Configuration.class
                            .getClassLoader()
                            .getResourceAsStream(getRelativeConfigFilePath());
            log.info("Loading configuration from relative path: {}", getRelativeConfigFilePath());
        }
        return inputStream;
    }

    private String getRelativeConfigFilePath() {
        return CONFIG_FILE_PATH;
    }

    private String getAbsoluteConfigFilePath() {
        return ABSOLUTE_CONFIG_FILE_PATH;
    }
}
