/*
Navicat MySQL Data Transfer

Source Server         : 霸王日志服-119.29.40.45
Source Server Version : 50544
Source Host           : 119.29.40.45:3306
Source Database       : log_android_cn_s1

Target Server Type    : MYSQL
Target Server Version : 50544
File Encoding         : 65001

Date: 2016-04-15 21:18:30
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `dict_pvp_type`
-- ----------------------------
DROP TABLE IF EXISTS `dict_pvp_type`;
CREATE TABLE `dict_pvp_type` (
  `pvp_type_id` int(11) DEFAULT NULL,
  `pvp_type_name` varchar(100) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of dict_pvp_type
-- ----------------------------
INSERT INTO `dict_pvp_type` VALUES ('1', '竞技场', '');
INSERT INTO `dict_pvp_type` VALUES ('2', '野外基地对战', '');
INSERT INTO `dict_pvp_type` VALUES ('3', '野外资源对战', '');
INSERT INTO `dict_pvp_type` VALUES ('4', '野外资源占领', '');
