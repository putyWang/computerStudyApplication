package com.learning.web.controller;

import com.learning.core.bean.ApiResult;
import com.learning.core.cache.RedisCache;
import com.learning.core.constants.RedisConstants;
import com.learning.core.utils.UUIDUtil;
import com.learning.core.utils.VerificationCode;
import com.learning.shiro.annotion.Model;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Controller
@Api(
        value = "验证码API",
        tags = {"验证码"}
)
@Model("system")
@RequestMapping({"/verificationCode"})
@ConditionalOnProperty(
        value = {"csm.enable-verify-code"},
        matchIfMissing = true
)
public class VerificationCodeController {
    private static final Logger log = LoggerFactory.getLogger(VerificationCodeController.class);

    /**
     * redis缓存工具类
     */
    @Autowired
    private RedisCache redisCache;

    public VerificationCodeController() {
    }

    @GetMapping({"/getImage"})
    @ApiOperation(
            value = "获取验证码",
            response = ApiResult.class
    )
    public void getImage(HttpServletResponse response) throws Exception {
        VerificationCode verificationCode = new VerificationCode();
        BufferedImage image = verificationCode.getImage();
        String code = verificationCode.getText();
        String verifyToken = UUIDUtil.getUuid();
        //将验证码存储到redis缓存之中(过期时间为一分钟)
        redisCache.set(String.format("verify.code:%s", verifyToken), code, RedisConstants.DEFAULT_VERIFICATION_CODE_TIME_OUT);
        response.setHeader("verifyToken", verifyToken);
        response.setContentType("image/jpeg");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expire", 0L);
        ServletOutputStream outputStream = response.getOutputStream();
        ImageIO.write(image, "JPEG", outputStream);
    }

    @GetMapping({"/getBase64Image"})
    @ResponseBody
    @ApiOperation(
            value = "获取图片Base64验证码",
            response = ApiResult.class
    )
    public ApiResult<Map<String, Object>> getCode()
            throws Exception {
        
        VerificationCode verificationCode = new VerificationCode();
        //获取图片验证码
        BufferedImage image = verificationCode.getImage();
        //获取验证码字符串
        String code = verificationCode.getText();
        //将图片进行Base64编码
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "JPEG", outputStream);
        String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
        //获取验证码相应key
        String verifyToken = String.format("verify.code:%s", UUIDUtil.getUuid());
        // 设置返回数据
        Map<String, Object> map = new HashMap<>(2);
        map.put("image", "data:image/png;base64," + base64);
        map.put("verifyToken", verifyToken);
        //将验证码存储到redis缓存之中(过期时间为一分钟)
        redisCache.set(verifyToken, code, RedisConstants.DEFAULT_VERIFICATION_CODE_TIME_OUT);
//        redisCache.set(verifyToken, code);

        return ApiResult.ok(map);
    }
}
