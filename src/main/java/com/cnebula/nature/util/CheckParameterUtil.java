package com.cnebula.nature.util;

import com.cnebula.nature.configuration.DefaultConfiguration;

import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;

public class CheckParameterUtil {

    private static LinkedList<String> parameters;

    private static String attention = null;
    static {
        parameters = new LinkedList<>();
        parameters.add(DefaultConfiguration.NAME_DBHOST);
        parameters.add(DefaultConfiguration.NAME_DBNAME);
        parameters.add(DefaultConfiguration.NAME_USERNAME);
        parameters.add(DefaultConfiguration.NAME_PASSWORD);
        parameters.add(DefaultConfiguration.NAME_ZIPFILEDIR);
        parameters.add(DefaultConfiguration.NAME_PDFBASEDIR);
    }

    public static String checkParameter(Properties properties) {
        Set<Object> keySet = properties.keySet();
        if (keySet.size() == 0 || !parameters.containsAll(keySet)) {
            attention = DefaultConfiguration.CONFIG + " Can not be null, and the correct format is: " + parameters.toString();
            return attention;
        }

        for (int i = 0; i < parameters.size(); i++) {
            if (!keySet.contains(parameters.get(i)) || properties.get(parameters.get(i)) == null) {
                attention = parameters.get(i) + " can not be null";
                return attention;
            }
        }
        return attention;
    }
}
