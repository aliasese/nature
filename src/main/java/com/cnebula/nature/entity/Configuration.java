package com.cnebula.nature.entity;

import java.util.Properties;

/**
 * @see Configuration params readed from outer config.properties will be stored in this entity object
 */
public class Configuration {

    private Properties properties;

    public Configuration() {
    }

    public Configuration(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
