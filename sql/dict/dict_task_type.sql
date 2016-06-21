/*
Navicat MySQL Data Transfer

Source Server         : 霸王日志服-119.29.40.45
Source Server Version : 50544
Source Host           : 119.29.40.45:3306
Source Database       : log_android_cn_s1

Target Server Type    : MYSQL
Target Server Version : 50544
File Encoding         : 65001

Date: 2016-04-15 21:18:47
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `dict_task_type`
-- ----------------------------
DROP TABLE IF EXISTS `dict_task_type`;
CREATE TABLE `dict_task_type` (
  `task_type_id` int(11) DEFAULT NULL,
  `task_type_name` varchar(100) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of dict_task_type
-- ----------------------------
INSERT INTO `dict_task_type` VALUES ('1', '主线', '');
INSERT INTO `dict_task_type` VALUES ('2', '日常', '');
INSERT INTO `dict_task_type` VALUES ('3', '日常活跃', '');
