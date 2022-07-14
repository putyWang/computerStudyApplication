package com.learning.core.utils;

import com.learning.core.utils.file.transfer.SourceFileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PropertiesUtils {
    public PropertiesUtils() {
    }

    public static Properties readProperties(String path) throws IOException {
        Properties props = new Properties();
        File file = new File(path);
        String configPropertiesPath = SourceFileUtils.getConfigFilePath("custom.properties");
        if (!file.exists()) {
            boolean flag = true;
            File fileParent = file.getParentFile();
            if (!file.exists()) {
                if (!fileParent.exists()) {
                    flag = fileParent.mkdirs();
                }

                if (flag) {
                    flag = file.createNewFile();
                }
            }

            if (flag && configPropertiesPath.equals(path)) {
                Map<String, String> map = new HashMap();
                map.put("es.cluster.name", "elasticsearch");
                map.put("es.network.address", "127.0.0.1:9200");
                map.put("es.transport.tcp.address", "127.0.0.1:9300");
                writeProperties(map, path);
            }
        } else {
            FileInputStream fis = new FileInputStream(file);
            props.load(new InputStreamReader(fis, StandardCharsets.UTF_8));
        }

        return props;
    }

    public static void writeProperties(Map<String, String> properties, String path) throws IOException {
        Properties props = new Properties();
        File file = new File(path);
        File fileParent = file.getParentFile();
        boolean flag = true;
        if (!file.exists() && !file.exists()) {
            if (!fileParent.exists()) {
                flag = fileParent.mkdirs();
            }

            if (flag) {
                flag = file.createNewFile();
            }
        }

        if (flag) {
            FileInputStream fis = new FileInputStream(file);
            props.load(new InputStreamReader(fis, StandardCharsets.UTF_8));
            Map<String, String> map = new HashMap();
            Set<Object> keySet = props.keySet();
            Iterator var9 = keySet.iterator();

            while(var9.hasNext()) {
                Object object = var9.next();
                String keyTmp = (String)object;
                String valueTmp = props.getProperty(keyTmp, "");
                map.put(keyTmp, valueTmp);
            }

            var9 = properties.entrySet().iterator();

            Map.Entry entry;
            while(var9.hasNext()) {
                entry = (Map.Entry)var9.next();
                map.put(entry.getKey().toString(), entry.getValue().toString());
            }

            var9 = map.entrySet().iterator();

            while(var9.hasNext()) {
                entry = (Map.Entry)var9.next();
                props.setProperty((String)entry.getKey(), (String)entry.getValue());
            }

            FileOutputStream fos = new FileOutputStream(file);
            props.store(new OutputStreamWriter(fos), "update config file.");
            fos.close();
            fis.close();
        }

    }
}
