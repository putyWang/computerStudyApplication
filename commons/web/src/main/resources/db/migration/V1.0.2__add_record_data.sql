
-- ----------------------------
-- Records of user
-- ----------------------------
BEGIN;

INSERT INTO `user`
VALUES (1, 'zs', 'DELETE_FLAG', 1, 'superAdmin');

INSERT INTO `user`
VALUES (2, 'ls', 'DELETE_FLAG', 1, 'user');

COMMIT;

-- ----------------------------
-- Records of role
-- ----------------------------
BEGIN;

INSERT INTO `authentication`
VALUES (1, '权限测试', 'test', 0);

INSERT INTO `authentication`
VALUES (2, '查询', 'query', 1);

INSERT INTO `authentication`
VALUES (3, '查看详情', 'detail', 1);

INSERT INTO `authentication`
VALUES (4, '新增数据', 'insert', 1);

INSERT INTO `authentication`
VALUES (5, '更新数据', 'update', 1);

INSERT INTO `authentication`
VALUES (6, '删除数据', 'delete', 1);

COMMIT;

-- ----------------------------
-- Records of role
-- ----------------------------
BEGIN;

INSERT INTO `role`
VALUES (1, '超级管理员', 'superAdmin');

INSERT INTO `role`
VALUES (2, '普通用户', 'user');

COMMIT;

BEGIN;

INSERT INTO `auth_role`
VALUES (1, 'superAdmin', 2);
INSERT INTO `auth_role`
VALUES (2, 'superAdmin', 3);
INSERT INTO `auth_role`
VALUES (3, 'superAdmin', 4);
INSERT INTO `auth_role`
VALUES (4, 'superAdmin', 5);
INSERT INTO `auth_role`
VALUES (5, 'superAdmin', 6);

INSERT INTO `auth_role`
VALUES (10, 'user', 2);

INSERT INTO `auth_role`
VALUES (11, 'user', 3);

COMMIT;

