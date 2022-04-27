//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.learaning.demo.holder;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 用户信息类
 */
public class UserContext {
    private String uuid;

    /**
     * 用户id
     */
    private Long userId = 0L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实名字
     */
    private String realName;

    /**
     * 是否为超级管理员
     */
    private boolean isSuperAdmin;

    /**
     * 用户角色集合
     */
    private List<String> roles;

    /**
     * 用户权限集合
     */
    private Set<String> authorities;


    private List<String> menus;

    /**
     * 机构代码
     */
    private String orgCode;

    /**
     * 系统代码
     */
    private String sysCode;

    /**
     * 本地信息对象
     */
    private Locale locale;
    private Integer type;
    private String tenantCode;
    private HashMap<Long, List<String>> desensitiseMap = new HashMap();
    private Map<String, Object> params = new HashMap();

    public UserContext() {
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRealName() {
        return this.realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return this.roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Set<String> getAuthorities() {
        return this.authorities;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

    public List<String> getMenus() {
        return this.menus;
    }

    public void setMenus(List<String> menus) {
        this.menus = menus;
    }

    public String getOrgCode() {
        return this.orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public Map<String, Object> getParams() {
        return this.params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public HashMap<Long, List<String>> getDesensitiseMap() {
        return this.desensitiseMap;
    }

    public void setDesensitiseMap(HashMap<Long, List<String>> desensitiseMap) {
        this.desensitiseMap = desensitiseMap;
    }

    public boolean isSuperAdmin() {
        return this.isSuperAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        this.isSuperAdmin = superAdmin;
    }

    public String getSysCode() {
        return this.sysCode;
    }

    public void setSysCode(String sysCode) {
        this.sysCode = sysCode;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTenantCode() {
        return this.tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }
}
