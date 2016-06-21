/*
Navicat MySQL Data Transfer

Source Server         : 霸王日志服-119.29.40.45
Source Server Version : 50544
Source Host           : 119.29.40.45:3306
Source Database       : log_android_cn_s1

Target Server Type    : MYSQL
Target Server Version : 50544
File Encoding         : 65001

Date: 2016-04-15 21:18:37
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `dict_shop_type`
-- ----------------------------
DROP TABLE IF EXISTS `dict_shop_type`;
CREATE TABLE `dict_shop_type` (
  `shop_type_id` int(11) DEFAULT NULL,
  `shop_type_name` varchar(100) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of dict_shop_type
-- ----------------------------
INSERT INTO `dict_shop_type` VALUES ('1', '元宝商城', '');
INSERT INTO `dict_shop_type` VALUES ('2', '竞技场商城', '');
INSERT INTO `dict_shop_type` VALUES ('3', '军团固定商城', '');
INSERT INTO `dict_shop_type` VALUES ('4', '军团随机商城', '');
