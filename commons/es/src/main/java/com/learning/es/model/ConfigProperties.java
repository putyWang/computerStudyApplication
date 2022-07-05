package com.learning.es.model;

import com.learning.core.utils.PropertiesUtils;
import com.learning.core.utils.SourceFileUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
public class ConfigProperties
        extends Properties {

    private static Properties instance;

    public static Properties getProperties() {
        return getInstance();
    }

    public static String getKey(String key) {
        Properties props = getInstance();
        return props.getProperty(key, "");
    }

    public static void refresh() {
        instance = null;
        instance = getInstance();
    }

    /**
     * 存储相关配置
     * @param properties
     * @return
     */
    public static boolean save(Map<String, String> properties) {
        if (properties != null) {
            try {
                PropertiesUtils.writeProperties(properties, SourceFileUtils.getConfigFilePath("custom.properties"));
                refresh();
                return true;
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }

        return false;
    }

    private static Properties getInstance() {
        if (instance == null) {
            synchronized(ConfigProperties.class) {
                if (instance == null) {
                    try {
                        //根据配置文件路径获取相关配置信息
                        instance = PropertiesUtils.readProperties(SourceFileUtils.getConfigFilePath("custom.properties"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return instance;
                }
            }
        }

        return instance;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ConfigProperties)) {
            return false;
        } else {
            ConfigProperties other = (ConfigProperties)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                return super.equals(o);
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof ConfigProperties;
    }

    public int hashCode() {
        int result = super.hashCode();
        return result;
    }

    public ConfigProperties() {
    }

    public String toString() {
        return "ConfigProperties()";
    }
}
