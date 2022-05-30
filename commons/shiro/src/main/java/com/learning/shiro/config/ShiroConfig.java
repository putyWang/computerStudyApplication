package com.learning.shiro.config;

import com.learning.shiro.bean.JwtRealm;
import com.learning.core.utils.CollectionUtils;
import com.learning.core.utils.StringUtils;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.*;

@Configuration
public class ShiroConfig {

    //自动注入自定义权限对象
    @Resource
    private JwtRealm realm;

    @Resource
    private ShiroProperties shiroProperties;

    private List<String> authorizedUrlList;

    private List<String> anonUrlList;

    //ShiroFilter过滤所有请求
    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        //给ShiroFilter配置安全管理器
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        //配置系统受限资源
        //配置系统公共资源
        Map<String, String> map = new HashMap<>();

        //设置认证界面路径
        //表示这个资源需要认证和授权
        urlToList();

        if (! CollectionUtils.isEmpty(authorizedUrlList)) {
            authorizedUrlList.forEach((authorizedUrl) -> {
                map.put(authorizedUrl, "authc");
            });
        }

        if (! CollectionUtils.isEmpty(anonUrlList)) {
            authorizedUrlList.forEach((anonUrl) -> {
                map.put(anonUrl, "anon");
            });
        }
        shiroFilterFactoryBean.setFilterChainDefinitionMap(map);

        return shiroFilterFactoryBean;
    }

    //创建安全管理器
    @Bean
    public DefaultWebSecurityManager getDefaultWebSecurityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(realm);
        return securityManager;
    }

    /**
     * 将AuthorizedUrl转换为List
     */
    private void urlToList() {

        if (shiroProperties != null) {
            String authorizedUrl = shiroProperties.getAuthorizedUrl();

            if (! StringUtils.isBlank(authorizedUrl)) {

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

            if (! StringUtils.isBlank(anonUrl)) {

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
}
