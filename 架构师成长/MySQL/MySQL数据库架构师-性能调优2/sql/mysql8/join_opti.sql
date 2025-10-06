/*
 Navicat Premium Data Transfer

 Source Server         : MySQL-8
 Source Server Type    : MySQL
 Source Server Version : 80030
 Source Host           : localhost:3307
 Source Schema         : join_opti

 Target Server Type    : MySQL
 Target Server Version : 80030
 File Encoding         : 65001

 Date: 11/07/2025 14:19:30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for departments
-- ----------------------------
DROP TABLE IF EXISTS `departments`;
CREATE TABLE `departments`  (
  `dept_id` int(0) NOT NULL,
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of departments
-- ----------------------------
INSERT INTO `departments` VALUES (1, '技术部');
INSERT INTO `departments` VALUES (2, '市场部');
INSERT INTO `departments` VALUES (3, '财务部');
INSERT INTO `departments` VALUES (4, '人力资源部');
INSERT INTO `departments` VALUES (5, '销售部');

-- ----------------------------
-- Table structure for employees
-- ----------------------------
DROP TABLE IF EXISTS `employees`;
CREATE TABLE `employees`  (
  `emp_id` int(0) NOT NULL,
  `emp_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `dept_id` int(0) NULL DEFAULT NULL,
  `salary` decimal(10, 2) NULL DEFAULT NULL,
  PRIMARY KEY (`emp_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of employees
-- ----------------------------
INSERT INTO `employees` VALUES (101, '张三', 1, 8500.00);
INSERT INTO `employees` VALUES (102, '李四', 1, 9200.00);
INSERT INTO `employees` VALUES (103, '王五', 2, 7800.00);
INSERT INTO `employees` VALUES (104, '赵六', 3, 9500.00);
INSERT INTO `employees` VALUES (105, '钱七', 3, 8800.00);
INSERT INTO `employees` VALUES (106, '孙八', NULL, 7500.00);
INSERT INTO `employees` VALUES (107, '周九', 5, 8200.00);
INSERT INTO `employees` VALUES (108, '吴十', 5, 9000.00);
INSERT INTO `employees` VALUES (109, '郑十一', 2, 8700.00);
INSERT INTO `employees` VALUES (110, '王十二', NULL, 8000.00);

-- ----------------------------
-- Table structure for t1
-- ----------------------------
DROP TABLE IF EXISTS `t1`;
CREATE TABLE `t1`  (
  `m1` int(0) NOT NULL,
  `n1` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t1
-- ----------------------------
INSERT INTO `t1` VALUES (1, 'a');
INSERT INTO `t1` VALUES (2, 'b');
INSERT INTO `t1` VALUES (3, 'c');

-- ----------------------------
-- Table structure for t2
-- ----------------------------
DROP TABLE IF EXISTS `t2`;
CREATE TABLE `t2`  (
  `m2` int(0) NOT NULL,
  `n2` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t2
-- ----------------------------
INSERT INTO `t2` VALUES (2, 'b');
INSERT INTO `t2` VALUES (3, 'c');

-- ----------------------------
-- Table structure for table1
-- ----------------------------
DROP TABLE IF EXISTS `table1`;
CREATE TABLE `table1`  (
  `id` int(0) NOT NULL,
  `c1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `c2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `c3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of table1
-- ----------------------------
INSERT INTO `table1` VALUES (1, '11', 'a1', 'aa1');
INSERT INTO `table1` VALUES (2, '12', 'b1', 'bb1');
INSERT INTO `table1` VALUES (4, '14', 'c1', 'cc1');
INSERT INTO `table1` VALUES (6, '16', 'd1', 'dd1');
INSERT INTO `table1` VALUES (7, '17', 'e1', 'ee1');

-- ----------------------------
-- Table structure for table2
-- ----------------------------
DROP TABLE IF EXISTS `table2`;
CREATE TABLE `table2`  (
  `id` int(0) NOT NULL,
  `c1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `c2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of table2
-- ----------------------------
INSERT INTO `table2` VALUES (1, '11', 'aaa');
INSERT INTO `table2` VALUES (2, '22', 'bbb');
INSERT INTO `table2` VALUES (3, '33', 'ccc');
INSERT INTO `table2` VALUES (4, '44', 'ddd');
INSERT INTO `table2` VALUES (5, '55', 'eee');

SET FOREIGN_KEY_CHECKS = 1;
