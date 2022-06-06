package com.learning.core.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class DownloadUtil {
    private static final Logger log = LoggerFactory.getLogger(DownloadUtil.class);

    public static void download(
            String downloadDir,
            String downloadFileName,
            List<String> allowFileExtensions,
            HttpServletResponse response
    ) throws Exception {
        download(downloadDir, downloadFileName, allowFileExtensions, response, new DownloadUtil.DefaultDownloadHandler());
    }

    /**
     * 文件下载方法
     * @param downloadDir
     * @param downloadFileName
     * @param allowFileExtensions
     * @param response
     * @param downloadHandler
     * @throws Exception
     */
    public static void download (
            String downloadDir,
            String downloadFileName,
            List<String> allowFileExtensions,
            HttpServletResponse response,
            DownloadUtil.DownloadHandler downloadHandler
    ) throws Exception {
        log.info("downloadDir:{}", downloadDir);
        log.info("downloadFileName:{}", downloadFileName);

        if (StringUtils.isBlank(downloadDir)) {
            throw new IOException("文件目录不能为空");
        } else if (StringUtils.isBlank(downloadFileName)) {
            throw new IOException("文件名称不能为空");
        } else if (
                !downloadFileName.contains("..") && !downloadFileName.contains("../")
        ) {

            if (CollectionUtils.isEmpty(allowFileExtensions)) {
                throw new IllegalArgumentException("请设置允许下载的文件后缀");
            } else {
                //获取文件扩展名
                String fileExtension = FilenameUtils.getExtension(downloadFileName);

                //当文件扩展名不能被下载时
                if (! allowFileExtensions.contains(fileExtension)) {
                    log.info("当前下载不支持该扩展名");
                    throw new IOException("不支持该的文件扩展名");
                }

                //获取下载文件
                File downloadFile = new File(downloadDir, downloadFileName);

                if (!downloadFile.exists()) {
                    throw new IOException("文件不存在");
                } else {
                    //获取文件类型并输出到日志
                    String contentType = ContentTypeUtil.getContentType(downloadFile);
                    log.info("contentType:{}", contentType);
                    //获取文件大小并输出到日志
                    long length = downloadFile.length();
                    log.info("length:{}", length);

                    //校验器为空时，使用默认校验器（直接校验失败）
                    if (downloadHandler == null) {
                        downloadHandler = new DownloadUtil.DefaultDownloadHandler();
                    }

                    //校验文件
                    boolean flag = (downloadHandler).handle(downloadDir, downloadFileName, downloadFile, fileExtension, contentType, length);

                    //校验文件失败时，直接取消下载
                    if (!flag) {
                        log.info("下载自定义校验失败，取消下载");
                        throw new IOException("下载自定义校验失败，取消下载");
                    } else {
                        //获取请求
                        HttpServletRequest request = HttpServletRequestUtil.getRequest();
                        //获取当前浏览器类型
                        String browser = BrowserUtil.getCurrent(request);
                        String encodeDownFileName;

                        //根据浏览器类型设置相应文件名称
                        if ("firefox".equals(browser)) {
                            encodeDownFileName = "=?UTF-8?B?" + Base64Utils.encodeToString(downloadFileName.getBytes(StandardCharsets.UTF_8)) + "?=";
                        } else {
                            encodeDownFileName = URLEncoder.encode(downloadFileName, "utf-8").replaceAll("\\+", "%20");
                        }

                        log.info("encodeDownFileName:{}", encodeDownFileName);
                        log.info("下载文件：" + downloadFile.getAbsolutePath());

                        //设置相应相关内容
                        response.reset();
                        response.setHeader("Content-Disposition", "attachment;fileName=\"" + encodeDownFileName + "\"");
                        response.setContentType(contentType);
                        response.setContentLengthLong(length);
                        InputStream in = new BufferedInputStream(new FileInputStream(downloadFile));
                        FileCopyUtils.copy(in, response.getOutputStream());
                    }
                }
            }
        } else {
            throw new IOException("非法的文件名称");
        }
    }

    /**
     * 默认文件处理器
     */
    public static class DefaultDownloadHandler implements DownloadUtil.DownloadHandler {
        public DefaultDownloadHandler() {
        }

        //直接校验失败，中止文件下载
        @Override
        public boolean handle(String downloadDir,
                              String downloadFileName,
                              File downloadFile,
                              String fileExtension,
                              String contentType,
                              long length
        ) throws Exception {
            return false;
        }
    }

    /**
     * 自定义下载文件校验器
     */
    public interface DownloadHandler {
        boolean handle (
                String downloadDir,
                String downloadFileName,
                File downloadFile,
                String fileExtension,
                String contentType,
                long length
        ) throws Exception;
    }
}
