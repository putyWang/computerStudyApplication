package com.learning.shiro.config;

import com.learning.core.utils.CollectionUtils;
import com.learning.core.utils.StringUtils;
import com.learning.shiro.bean.JwtRealm;
import com.learning.shiro.filter.JwtAuthFilter;
import com.learning.shiro.handler.DefaultAuthorizationAttributeSourceAdvisor;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.Resource;
import javax.servlet.Filter;
import java.util.*;

@Configuration
public class ShiroConfig {

    private static final Logger log = LoggerFactory.getLogger(ShiroConfig.class);


    @Resource
    private ShiroProperties shiroProperties;

    private List<String> authorizedUrlList;

    private List<String> anonUrlList;

    //ShiroFilter过滤所有请求
    @Bean("shiroFilterFactoryBean")
    @DependsOn({"securityManager"})
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        //给ShiroFilter配置安全管理器
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        //配置系统受限资源
        //配置系统公共资源
        Map<String, String> map = new LinkedHashMap<>();

        //设置认证界面路径
        //表示这个资源需要认证和授权
        urlToList();
        log.info("不需要鉴权路径：" + shiroProperties.getAnonUrl());
        if (! CollectionUtils.isEmpty(anonUrlList)) {
            anonUrlList.forEach((anonUrl) -> {
                map.put(anonUrl, "anon");
            });
        }

        log.info("需要鉴权路径：" + shiroProperties.getAuthorizedUrl());
        if (! CollectionUtils.isEmpty(authorizedUrlList)) {
            authorizedUrlList.forEach((authorizedUrl) -> {
                map.put(authorizedUrl, "jwt");
            });
        }

        //设置自定义过滤器
        Map<String, Filter> filterMap = new HashMap<>();
        filterMap.put ("jwt", new JwtAuthFilter());
        shiroFilterFactoryBean.setFilters(filterMap);
        shiroFilterFactoryBean.setFilterChainDefinitionMap(map);

        return shiroFilterFactoryBean;
    }

    /**
     * 向Session管理域中注入RedisSessionDao
     * @return
     */
    @Bean
    public SessionManager sessionManager () {
        DefaultSessionManager sessionManager = new DefaultWebSessionManager();

        return sessionManager;
    }

    /**
     * 创建安全管理器
     * 禁用seesion
     * 进行无状态登录
     * @return
     */
    @Bean("securityManager")
    public DefaultWebSecurityManager getDefaultWebSecurityManager(JwtRealm jwtRealm, SessionManager sessionManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager(jwtRealm);
        //注入 sessionManager
        securityManager.setSessionManager(sessionManager);

        /*
         * 关闭shiro自带的session，详情见文档
         */
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);
        return securityManager;
    }

    /**
     * shiro启用注解授权，并注入自定义的注解授权处理类
     * @param securityManager
     * @return
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new DefaultAuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    @Bean
    public static DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator(){
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator=new DefaultAdvisorAutoProxyCreator();
        /**
         * setUsePrefix(false)用于解决一个奇怪的bug。在引入spring aop的情况下。
         * 在@Controller注解的类的方法中加入@RequiresRole等shiro注解，会导致该方法无法映射请求，导致返回404。
         * 加入这项配置能解决这个bug
         */
        defaultAdvisorAutoProxyCreator.setUsePrefix(true);
        return defaultAdvisorAutoProxyCreator;
    }

    /**
     * 为了保证实现了Shiro内部lifecycle函数的bean执行 也是shiro的生命周期
     * @return
     */
//    @Bean
//
//    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
//
//        return new LifecycleBeanPostProcessor();
//
//    }

    /**
     * 将AuthorizedUrl转换为List
     */
    private void urlToList() {

        if (shiroProperties != null) {
            String authorizedUrl = shiroProperties.getAuthorizedUrl();

            if (!StringUtils.isBlank(authorizedUrl)) {

                if (authorizedUrl.endsWith(",")) {
                    authorizedUrl = authorizedUrl.substring(0, authorizedUrl.length() - 2);
                }

                if (authorizedUrl.contains(",")) {
                    String[] split = authorizedUrl.split(",");
                    authorizedUrlList = Arrays.asList(split);
                }else {
                    authorizedUrlList = new ArrayList<>();
                    authorizedUrlList.add(authorizedUrl);
                }
            }

            String anonUrl = shiroProperties.getAnonUrl();

            if (!StringUtils.isBlank(anonUrl)) {

                if (anonUrl.endsWith(",")) {
                    anonUrl = anonUrl.substring(0, anonUrl.length() - 2);
                }

                if (anonUrl.contains(",")) {
                    String[] anonUrls = anonUrl.split(",");
                    anonUrlList = Arrays.asList(anonUrls);
                }else {
                    anonUrlList = new ArrayList<>();
                    anonUrlList.add(anonUrl);
                }
            }
        }
    }
}
