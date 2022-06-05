package com.learning.shiro.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.core.bean.ApiResult;
import com.learning.core.enums.ApiCode;
import com.learning.core.utils.StringUtils;
import com.learning.shiro.bean.JwtToken;
import com.learning.shiro.contants.JwtConstants;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 自定义shiro过滤器，所有需要授权的接口都从这里走 配置了authc的接口
 */
public class JwtAuthFilter
        extends BasicHttpAuthenticationFilter implements Filter {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 对跨域提供支持
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        // 跨域时会首先发送一个option请求，这里我们给option请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        // 从Header里面获取
        String token = httpRequest.getHeader(JwtConstants.ACESS_TOKEN);

        return new JwtToken(token);
    }

    /**
     * 初次处理Token
     * 如果返回true，则会跳转到下一个链式调用（过滤器、拦截器、 控制层等）
     * 如果返回false，则会跳转到onAccessDenied方法进行处理
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        return false;
    }

    /**
     * 第二次处理token
     * isAccessAllowed的结果经过处理如果返回true，则会跳转到下一个链式调用（过滤器、拦截器、 控制层等）
     * 否则直接直接将处理失败结果的反馈信息返回到前端
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        log.info("request接口地址：{}",((HttpServletRequest)request).getRequestURI());
        log.info("request接口请求方式：{}",((HttpServletRequest)request).getMethod());
        //直接进行Token验证
        return executeLogin(request, response);
    }

    /**
     * 登陆验证失败后执行
     * @param token
     * @param e
     * @param request
     * @param response
     * @return
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletResponse servletResponse = (HttpServletResponse) response;
        log.info(ApiCode.LOGIN_NOT.getMessage());
        ApiResult<Boolean> fail = ApiResult.fail(ApiCode.LOGIN_NOT);
        try {
            //设置前端相关属性
            servletResponse.setCharacterEncoding("UTF-8");
            servletResponse.setContentType("application/json;charset=UTF-8");
            servletResponse.setHeader("Access-Control-Allow-Origin","*");
            ObjectMapper objectMapper = new ObjectMapper();
            //将失败结果返回前端
            response.getWriter().write(objectMapper.writeValueAsString(fail));
        } catch (IOException IOException) {
            IOException.printStackTrace();
        }
        return false;
    }
}
