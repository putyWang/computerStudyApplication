package com.learning.core.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class ContentTypeUtil {
    private static final Logger log = LoggerFactory.getLogger(ContentTypeUtil.class);
    private static String MIME_TYPE_CONFIG_FILE = "config/mime-type.properties";
    private static String DEFAULT_MIME_TYPE = "application/octet-stream";
    private static Properties properties;

    /**
     * 获取文件类型
     * @param file
     * @return
     */
    public static String getContentType(File file) {
        if (file == null) {
            return null;
        } else {
            Path path = Paths.get(file.toURI());
            if (path == null) {
                return null;
            } else {
                String contentType = null;

                try {
                    contentType = Files.probeContentType(path);
                } catch (IOException var4) {
                    log.error("获取文件ContentType异常", var4);
                }

                if (contentType == null) {
                    contentType = getContentTypeByExtension(file);
                }

                if (contentType == null) {
                    contentType = DEFAULT_MIME_TYPE;
                }

                return contentType;
            }
        }
    }

    /**
     * 通过文件扩展类型名获取文件类型
     * @param file
     * @return
     */
    private static String getContentTypeByExtension(File file) {
        if (properties == null) {
            return null;
        } else {
            String extension = FilenameUtils.getExtension(file.getName());
            if (StringUtils.isBlank(extension)) {
                return null;
            } else {
                return properties.getProperty(extension);
            }
        }
    }

    static {
        try {
            properties = PropertiesLoaderUtils.loadProperties(new ClassPathResource(MIME_TYPE_CONFIG_FILE));
        } catch (IOException var1) {
            log.error("读取配置文件" + MIME_TYPE_CONFIG_FILE + "异常", var1);
        }

        log.info(MIME_TYPE_CONFIG_FILE + " = " + properties);
    }
}
