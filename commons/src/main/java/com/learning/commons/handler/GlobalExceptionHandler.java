package com.learning.commons.handler;

import com.alibaba.fastjson.JSON;
import com.learning.commons.bean.RequestDetail;
import com.learning.commons.entity.ApiResult;
import com.learning.commons.enums.ApiCode;
import com.learning.commons.exception.*;
import com.learning.commons.utils.RequestDetailThreadLocal;
import org.apache.shiro.authz.UnauthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 全局异常过滤器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public GlobalExceptionHandler() {
    }

    /**
     * MethodArgumentNotValidException（请求参数校验异常）处理器
     * @param ex
     * @return
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<List<String>> handleMethodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {
        this.printRequestDetail();
        BindingResult bindingResult = ex.getBindingResult();
        //异常集合
        List<String> list = new ArrayList<>();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();

        for (FieldError fieldError : fieldErrors) {
            list.add(fieldError.getDefaultMessage());
        }

        Collections.sort(list);
        log.error(this.getApiCodeString(ApiCode.PARAMETER_EXCEPTION) + ":" + JSON.toJSONString(list));

        return ApiResult.fail(ApiCode.PARAMETER_EXCEPTION, list);
    }

    /**
     * SysLoginException（登录失败异常）处理器
     * @param exception
     * @return
     */
    @ExceptionHandler({SysLoginException.class})
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<Boolean> sysLoginExceptionHandler(SysLoginException exception) {

        printRequestDetail();
        printApiCodeException(ApiCode.LOGIN_EXCEPTION, exception);

        return ApiResult.fail(ApiCode.LOGIN_EXCEPTION);
    }

    /**
     * 请求参数匹配错误处理器
     * @param exception
     * @return
     */
    @ExceptionHandler({HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<String> httpMessageNotReadableException(HttpMessageNotReadableException exception) {

        printRequestDetail();
        printApiCodeException(ApiCode.PARAMETER_EXCEPTION, exception);

        String errorMsg = "请求参数匹配错误," + exception.getLocalizedMessage();

        return ApiResult.fail(ApiCode.PARAMETER_EXCEPTION.getCode(), errorMsg);
    }

    /**
     * HTTP内容类型异常处理器
     * @param exception
     * @return
     */
    @ExceptionHandler({HttpMediaTypeException.class})
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<Boolean> httpMediaTypeException(HttpMediaTypeException exception) {
        printRequestDetail();
        printApiCodeException(ApiCode.HTTP_MEDIA_TYPE_EXCEPTION, exception);

        return ApiResult.fail(ApiCode.HTTP_MEDIA_TYPE_EXCEPTION);
    }

    /**
     * 基础异常处理器
     * @param exception
     * @return
     */
    @ExceptionHandler({SpringBootException.class})
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<Boolean> springBootExceptionHandler(SpringBootException exception) {
        this.printRequestDetail();
        log.error("springBootException:", exception);
        int errorCode;
        //基础异常
        if (exception instanceof BaseException) {
            errorCode = exception.getErrorCode();
        }
        //数据库处理异常
        else if (exception instanceof DaoException) {
            errorCode = ApiCode.DAO_EXCEPTION.getCode();
        }
        //请求参数验证异常
        else if (exception instanceof VerificationCodeException) {
            errorCode = ApiCode.VERIFICATION_CODE_EXCEPTION.getCode();
        }
        //其余系统异常
        else {
            errorCode = ApiCode.SYSTEM_EXCEPTION.getCode();
        }

        return (new ApiResult<Boolean>()).setCode(errorCode).setMessage(exception.getMessage());
    }

    /**
     * 未登录异常处理器
     * @param nle
     * @return
     */
//    @ExceptionHandler({NotLoginException.class})
//    public ApiResult<String> notLoginExceptionHandler(NotLoginException nle) {
//        nle.printStackTrace();
//        String message = "";
//        if (nle.getType().equals("-1")) {
//            message = "未提供token";
//        } else if (nle.getType().equals("-2")) {
//            message = "token无效";
//        } else if (nle.getType().equals("-3")) {
//            message = "token已过期";
//        } else if (nle.getType().equals("-4")) {
//            message = "token已被顶下线";
//        } else if (nle.getType().equals("-5")) {
//            message = "token已被踢下线";
//        } else {
//            message = "当前会话未登录";
//        }
//
//        printApiCodeException(ApiCode.LOGIN_EXCEPTION, nle);
//        return ApiResult.fail(ApiCode.LOGIN_NOT.getCode(), message + "," + nle.getLoginType());
//    }
//
    /**
     * 权限验证异常处理器
     * @param exception
     * @return
     */
    @ExceptionHandler({UnauthenticatedException.class})
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<String> notPermissionExceptionHandler(UnauthenticatedException exception) {
        printRequestDetail();
        printApiCodeException(ApiCode.NOT_PERMISSION, exception);
        String message = "无此权限：" + ApiCode.NOT_PERMISSION.getCode();

        return ApiResult.fail(ApiCode.NOT_PERMISSION.getCode(), message);
    }

    /**
     * 无权限异常
     * @param exception
     * @return
     */
    @ExceptionHandler({UnauthorizedException.class})
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<Boolean> unauthorizedExceptionHandler(UnauthorizedException exception) {
        this.printRequestDetail();
        this.printApiCodeException(ApiCode.UNAUTHORIZED, exception);
        return ApiResult.fail(ApiCode.UNAUTHORIZED);
    }

    /**
     * Token解析异常
     * @param exception
     * @return
     */
    @ExceptionHandler({JWTDecodeException.class})
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<Boolean> jWTDecodeExceptionHandler(JWTDecodeException exception) {

        printRequestDetail();
        printApiCodeException(ApiCode.JWTDECODE_EXCEPTION, exception);

        return ApiResult.fail(ApiCode.JWTDECODE_EXCEPTION);
    }

    /**
     * 请求方法异常
     * @param exception
     * @return
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<Boolean> httpRequestMethodNotSupportedExceptionHandler(Exception exception) {
        this.printRequestDetail();
        this.printApiCodeException(ApiCode.HTTP_REQUEST_METHOD_NOT_SUPPORTED_EXCEPTION, exception);
        return ApiResult.fail(ApiCode.HTTP_REQUEST_METHOD_NOT_SUPPORTED_EXCEPTION);
    }

    /**
     * 基础异常
     * @param exception
     * @return
     */
    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<String> exceptionHandler(Exception exception) {
        this.printRequestDetail();
        String errorMsg = "";
        if (exception instanceof NullPointerException) {
            errorMsg = "参数空指针异常";
        } else if (exception instanceof SocketTimeoutException) {
            errorMsg = "连接超时,请检查网络环境或重试";
        } else {
            errorMsg = exception.getMessage();
        }

        this.printApiCodeException(ApiCode.SYSTEM_EXCEPTION, exception);
        return ApiResult.fail(ApiCode.SYSTEM_EXCEPTION.getCode(), errorMsg);
    }

    /**
     * 基础异常
     * @param exception
     * @return
     */
    @ExceptionHandler({BaseException.class})
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<String> businessExceptionHandler(BaseException exception) {
        this.printRequestDetail();
        this.printApiCodeException(ApiCode.SYSTEM_EXCEPTION, exception);
        return ApiResult.fail(exception.getErrorCode(), exception.getMessage());
    }

    /**
     * es相关异常
     * @param exception
     * @return
     */
    @ExceptionHandler({ElasticException.class})
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<Boolean> elasticExceptionHandler(ElasticException exception) {
        this.printRequestDetail();
        this.printApiCodeException(ApiCode.SYSTEM_EXCEPTION, exception);
        return ApiResult.fail(ApiCode.ELASTICSEARCH_REQUEST_EXCEPTION);
    }

    /**
     * 打印异常信息
     */
    private void printRequestDetail() {
        RequestDetail requestDetail = RequestDetailThreadLocal.getRequestDetail();
        if (requestDetail != null) {
            log.error("异常来源：ip: {}, path: {}", requestDetail.getIp(), requestDetail.getPath());
        }

    }

    /**
     * 获取
     * @param apiCode
     * @return
     */
    private String getApiCodeString(ApiCode apiCode) {
        return apiCode != null ? String.format("errorCode: %s, errorMessage: %s", apiCode.getCode(), apiCode.getMessage()) : null;
    }

    private void printApiCodeException(ApiCode apiCode, Exception exception) {
        log.error(this.getApiCodeString(apiCode), exception);
    }
}
