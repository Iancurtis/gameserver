/*
Navicat MySQL Data Transfer

Source Server         : 霸王日志库
Source Server Version : 50544
Source Host           : 119.29.40.45:3306
Source Database       : log_android_cn_s9994

Target Server Type    : MYSQL
Target Server Version : 50544
File Encoding         : 65001

Date: 2016-01-30 11:07:48
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `dict_platform`
-- ----------------------------
DROP TABLE IF EXISTS `dict_platform`;
CREATE TABLE `dict_platform` (
  `platform_id` int(11) NOT NULL DEFAULT '0' COMMENT '平台id',
  `platform_name` varchar(20) NOT NULL DEFAULT '' COMMENT '平台名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of dict_platform
-- ----------------------------
INSERT INTO `dict_platform` VALUES ('0', 'kkk');
INSERT INTO `dict_platform` VALUES ('1', 'baidu91');
INSERT INTO `dict_platform` VALUES ('2', 'wandoujia');
INSERT INTO `dict_platform` VALUES ('3', 'unknown');
