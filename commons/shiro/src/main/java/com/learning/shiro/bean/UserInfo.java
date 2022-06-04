package com.learning.shiro.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * principalCollection保存类
 */
@Data
public class UserInfo implements Serializable {

    /**
     * redis中保存id
     */
    private Long id;

    /**
     * Jwt生成的Token
     */
    private String token;
}
