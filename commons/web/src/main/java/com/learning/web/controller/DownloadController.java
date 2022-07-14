package com.learning.web.controller;

import com.learning.core.bean.ApiResult;
import com.learning.core.utils.file.transfer.DownloadUtil;
import com.learning.core.utils.StringUtils;
import com.learning.shiro.annotion.Model;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping({"/download"})
@Model("system")
@Api(
        value = "文件下载",
        tags = {"文件下载"}
)
public class DownloadController {
    private static final Logger log = LoggerFactory.getLogger(DownloadController.class);
    @Value("${learning.upload-path:#{null}}")
    private String uploadPath;
    @Value("${learning.allow-download-file-extensions:#{null}}")
    private String allowDownloadFileExtensions;

    @GetMapping({"/{downloadFileName}"})
    @ApiOperation(
            value = "下载文件",
            notes = "下载文件",
            response = ApiResult.class
    )
    public void download(
            @PathVariable(required = true) String downloadFileName,
            HttpServletResponse response
    ) throws Exception {

        String downloadDir = "";
        File file;
        File upload;

        //上传文件夹不存在时，使用类加载路径中的upload子路径作为上传文件夹
        if (StringUtils.isBlank(this.uploadPath)) {
            //加载类加载器相关区域文件
            file = new File(ResourceUtils.getURL("classpath:").getPath());

            if (!file.exists()) {
                file = new File("");
            }

            //获取upload子路径下文件
            upload = new File(file.getAbsolutePath(), "upload/");
            if (!upload.exists()) {
                upload.mkdirs();
            }

            downloadDir = upload.getAbsolutePath();
        } else {
            //获取上传文件夹中的文件
            file = new File(this.uploadPath);

            if (file.isAbsolute()) {
                if (!file.exists()) {
                    file.mkdirs();
                }

                //将上传文件夹绝对路径作为下载文件夹
                downloadDir = this.uploadPath;
            } else {
                //获取upload文件夹
                upload = new File(ResourceUtils.getURL("classpath:").getPath(), uploadPath);
                //文件夹不存在时，创建文件夹
                if (!upload.exists()) {
                    upload.mkdirs();
                }
                //将上传文件夹绝对路径作为下载文件夹
                downloadDir = upload.getAbsolutePath();
            }
        }

        //创建可被下载的全部文件类型，默认所有文件类型均可被下载
        List<String> allowFileExtensions = new ArrayList<>();

        if (StringUtils.isBlank(this.allowDownloadFileExtensions)) {
            this.allowDownloadFileExtensions = "txt,doc,docx,xls,xlsx,xml,jpg,jpeg,png,bmp,zip,tar,war,jar,tar,exe,bat,sh,cmd,json";
        }

        String[] commas = this.allowDownloadFileExtensions.split(",");

        for (String item : commas) {
            allowFileExtensions.add(item.trim());
        }

        //日志记录下载文件相关参数
        DownloadUtil.download(downloadDir, downloadFileName, allowFileExtensions, response, (dir, fileName, filex, fileExtension, contentType, length) -> {
            log.info("dir = " + dir);
            log.info("fileName = " + fileName);
            log.info("file = " + filex);
            log.info("fileExtension = " + fileExtension);
            log.info("contentType = " + contentType);
            log.info("length = " + length);
            return true;
        });
    }
}
