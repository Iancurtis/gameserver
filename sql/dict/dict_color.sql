/*
Navicat MySQL Data Transfer

Source Server         : 霸王日志服-119.29.40.45
Source Server Version : 50544
Source Host           : 119.29.40.45:3306
Source Database       : log_android_cn_s1

Target Server Type    : MYSQL
Target Server Version : 50544
File Encoding         : 65001

Date: 2016-04-15 21:17:47
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `dict_color`
-- ----------------------------
DROP TABLE IF EXISTS `dict_color`;
CREATE TABLE `dict_color` (
  `color_id` int(11) DEFAULT NULL,
  `color_name` varchar(100) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of dict_color
-- ----------------------------
INSERT INTO `dict_color` VALUES ('1', '白色', '');
INSERT INTO `dict_color` VALUES ('2', '绿色', '');
INSERT INTO `dict_color` VALUES ('3', '蓝色', '');
INSERT INTO `dict_color` VALUES ('4', '紫色', '');
INSERT INTO `dict_color` VALUES ('5', '橙色', '');
