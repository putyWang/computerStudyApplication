package com.learning.core.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 文件上传工具类
 */
public final class UploadUtil {
    private static final Logger log = LoggerFactory.getLogger(UploadUtil.class);

    public static String upload(String uploadPath, MultipartFile multipartFile) throws Exception {
        return upload(uploadPath, multipartFile, new UploadUtil.DefaultUploadFileNameHandleImpl());
    }

    public static String upload(
            String uploadPath,
            MultipartFile multipartFile,
            UploadUtil.UploadFileNameHandle uploadFileNameHandle
    ) throws Exception {
        //获取上传文件输入流
        InputStream inputStream = multipartFile.getInputStream();
        //创建上传存储文件夹
        File saveDir = new File(uploadPath);

        if (!saveDir.exists()) {
            boolean flag = saveDir.mkdirs();
            if (!flag) {
                throw new RuntimeException("创建" + saveDir + "目录失败！");
            }
        }

        //获取系统存储原始文件名
        String originalFilename = multipartFile.getOriginalFilename();
        //创建文件存储名
        String saveFileName;

        if (uploadFileNameHandle == null) {
            saveFileName = (new UploadUtil.DefaultUploadFileNameHandleImpl()).handle(originalFilename);
        } else {
            saveFileName = uploadFileNameHandle.handle(originalFilename);
        }

        log.info("saveFileName = " + saveFileName);
        //按照文件名在相应文件夹创建相关文件
        File saveFile = new File(saveDir, saveFileName);
        //将上传文件写入到本地文件中
        FileUtils.copyToFile(inputStream, saveFile);

        return saveFileName;
    }

    /**
     * 删除本地相关文件
     * @param uploadPath
     * @param saveFileName
     */
    public static void deleteQuietly(String uploadPath, String saveFileName) {
        File saveDir = new File(uploadPath);
        File saveFile = new File(saveDir, saveFileName);
        log.debug("删除文件：" + saveFile);
        FileUtils.deleteQuietly(saveFile);
    }

    public static class DefaultUploadFileNameHandleImpl implements UploadUtil.UploadFileNameHandle {
        public DefaultUploadFileNameHandleImpl() {
        }

        //默认设置文件名为时间.文件扩展名
        public String handle(String originalFilename) {
            String fileExtension = FilenameUtils.getExtension(originalFilename);
            String dateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssS"));

            return dateString + "." + fileExtension;
        }
    }

    public interface UploadFileNameHandle {
        String handle(String var1);
    }
}
