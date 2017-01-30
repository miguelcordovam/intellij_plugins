package com.miguel.plugin.copyrest;

import java.util.Properties;

public class PropertiesUtil {

    private Properties getPropertiesFromString(String file) {
        Properties properties = new Properties();

        String[] lines = file.split("\n");

        for (String line : lines) {
            if (!lineIsComment(line) && lineContainsProperty(line)) {
                String[] prop = line.split("=");
                if (prop.length > 1) {
                    String key = prop[0].trim();
                    String value = prop[1].trim();

                    properties.put(key, value);
                }
            }
        }

        return properties;
    }

    private boolean lineIsComment(String line) {
        return line.startsWith("#");
    }

    private boolean lineContainsProperty(String line) {
        return line.contains("=");
    }

    public String getPropertyValue(String file, String key) {

        Properties properties = getPropertiesFromString(file);
        return (properties.getProperty(key) != null) ? properties.getProperty(key) : "";
    }
}
