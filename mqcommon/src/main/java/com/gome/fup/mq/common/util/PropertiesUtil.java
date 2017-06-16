package com.gome.fup.mq.common.util;

import java.io.*;
import java.util.Properties;

/**
 * Created by fupeng-ds on 2017/6/12.
 */
public class PropertiesUtil {

    private static Properties properties;

    static {
        properties = new Properties();
        try {
            InputStream input = new FileInputStream(new File("config.properties"));
            properties.load(input);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
