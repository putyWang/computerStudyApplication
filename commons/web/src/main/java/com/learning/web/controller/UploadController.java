package com.learning.web.controller;

import com.learning.core.bean.ApiResult;
import com.learning.core.utils.StringUtils;
import com.learning.core.utils.UploadUtil;
import com.learning.shiro.annotion.Model;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping({"/upload"})
@Model("system")
@Api(
        value = "文件上传",
        tags = {"文件上传"}
)
public class UploadController {
    private static final Logger log = LoggerFactory.getLogger(UploadController.class);
    @Value("${csm.upload-path:#{null}}")
    private String uploadPath;
    @Value("${csm.allow-upload-file-extensions:#{null}}")
    private String allowUploadFileExtensions;

    @PostMapping
    @ApiOperation(
            value = "上传单个文件",
            response = ApiResult.class
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "file",
            value = "文件",
            required = true,
            dataType = "__file"
    ), @ApiImplicitParam(
            name = "type",
            value = "类型 head:头像",
            required = true
    )})
    public ApiResult<String> upload(
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam("type") String type
    ) throws Exception {

        log.info("multipartFile = " + multipartFile);
        log.info("ContentType = " + multipartFile.getContentType());
        log.info("OriginalFilename = " + multipartFile.getOriginalFilename());
        log.info("Name = " + multipartFile.getName());
        log.info("Size = " + multipartFile.getSize());
        log.info("type = " + type);
        String uploadDir = "";
        File file;
        File upload;

        if (StringUtils.isBlank(this.uploadPath)) {
            file = new File(ResourceUtils.getURL("classpath:").getPath());

            if (!file.exists()) {
                file = new File("");
            }

            upload = new File(file.getAbsolutePath(), "upload/");

            if (!upload.exists()) {
                upload.mkdirs();
            }

            uploadDir = upload.getAbsolutePath();
        } else {
            file = new File(this.uploadPath);

            if (file.isAbsolute()) {
                if (!file.exists()) {
                    file.mkdirs();
                }

                uploadDir = this.uploadPath;
            } else {
                upload = new File(ResourceUtils.getURL("classpath:").getPath(), uploadPath);

                if (!upload.exists()) {
                    upload = new File("");
                }

                upload = new File(upload.getAbsolutePath(), "upload/");
                if (!upload.exists()) {
                    upload.mkdirs();
                }

                uploadDir = upload.getAbsolutePath();
            }
        }

        log.info("uploadDir:{}", uploadDir);

        //定义文件名（当前时间+六位数字随机字符串.文件扩展名）
        String saveFileName = UploadUtil.upload(uploadDir, multipartFile, (originalFilename) -> {
            String fileExtension = FilenameUtils.getExtension(originalFilename);
            String dateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssS")) + RandomStringUtils.randomNumeric(6);
            String fileName = dateString + "." + fileExtension;
            return fileName;
        });
        return ApiResult.ok(saveFileName);
    }
}
