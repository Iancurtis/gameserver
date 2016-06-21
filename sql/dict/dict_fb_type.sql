/*
Navicat MySQL Data Transfer

Source Server         : 霸王日志服-119.29.40.45
Source Server Version : 50544
Source Host           : 119.29.40.45:3306
Source Database       : log_android_cn_s1

Target Server Type    : MYSQL
Target Server Version : 50544
File Encoding         : 65001

Date: 2016-04-15 21:17:57
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `dict_fb_type`
-- ----------------------------
DROP TABLE IF EXISTS `dict_fb_type`;
CREATE TABLE `dict_fb_type` (
  `fb_type_id` int(11) DEFAULT NULL,
  `fb_type_name` varchar(100) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of dict_fb_type
-- ----------------------------
INSERT INTO `dict_fb_type` VALUES ('1', '征战主线', '');
INSERT INTO `dict_fb_type` VALUES ('2', '武将切磋', '');
INSERT INTO `dict_fb_type` VALUES ('3', '军械争夺', '');
INSERT INTO `dict_fb_type` VALUES ('4', '西域远征', '');
