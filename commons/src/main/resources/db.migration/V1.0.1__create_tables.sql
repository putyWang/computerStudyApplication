/*
 Navicat Premium Data Transfer

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 50735
 Source Host           : localhost:3306
 Source Schema         : csm_admin

 Target Server Type    : MySQL
 Target Server Version : 50735
 File Encoding         : 65001

 Date: 07/09/2021 15:41:59
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for flyway_schema_history
-- ----------------------------
DROP TABLE IF EXISTS `flyway_schema_history`;
CREATE TABLE `flyway_schema_history`
(
    `installed_rank` int(11)       NOT NULL,
    `version`        varchar(50)            DEFAULT NULL,
    `description`    varchar(200)  NOT NULL,
    `type`           varchar(20)   NOT NULL,
    `script`         varchar(1000) NOT NULL,
    `checksum`       int(11)                DEFAULT NULL,
    `installed_by`   varchar(100)  NOT NULL,
    `installed_on`   timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `execution_time` int(11)       NOT NULL,
    `success`        tinyint(1)    NOT NULL,
    PRIMARY KEY (`installed_rank`),
    KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='表单版本历史';


-- ----------------------------
-- Table structure for uums_application
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
   `id`        bigint(20)                                              NOT NULL AUTO_INCREMENT,
   `username`  varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
   `password`  varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
   `role_code` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
   PRIMARY KEY (`id`) USING BTREE,
   UNIQUE KEY `username` (`username`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci ROW_FORMAT = Dynamic
  COMMENT ='用户表';

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `id`        bigint(20)                                              NOT NULL AUTO_INCREMENT,
  `name`      varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `role_code` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `name` (`name`) USING BTREE,
  UNIQUE KEY `role_code` (`role_code`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci ROW_FORMAT = Dynamic
  COMMENT ='角色表';

-- ----------------------------
-- Table structure for authentication
-- ----------------------------
DROP TABLE IF EXISTS `authentication`;
CREATE TABLE `authentication`  (
  `id`        bigint(20)                                                    NOT NULL AUTO_INCREMENT,
  `name`      varchar(100)       CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL                COMMENT '权限名',
  `permission_code` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL                COMMENT '权限代码',
  `pid`       bigint(20)                                                    NOT NULL                COMMENT '父权限id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `name` (`name`) USING BTREE,
  UNIQUE KEY `permission` (`permission_code`, `pid`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci ROW_FORMAT = Dynamic
  COMMENT ='权限表';

-- ----------------------------
-- Table structure for authentication
-- ----------------------------
DROP TABLE IF EXISTS `auth_role`;
CREATE TABLE `auth_role`  (
   `id`        bigint(20)                                                    NOT NULL AUTO_INCREMENT,
   `role_code`      varchar(100)       CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL                COMMENT '关联角色id',
   `auth_id`   varchar(100)       CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL                COMMENT '关联权限id',                                                   NOT NULL                COMMENT '父权限id',
   PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci ROW_FORMAT = Dynamic
  COMMENT ='权限角色表';


