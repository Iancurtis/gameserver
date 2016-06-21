/*
Navicat MySQL Data Transfer

Source Server         : 霸王日志服-119.29.40.45
Source Server Version : 50544
Source Host           : 119.29.40.45:3306
Source Database       : log_android_cn_s1

Target Server Type    : MYSQL
Target Server Version : 50544
File Encoding         : 65001

Date: 2016-04-15 21:18:53
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `dict_value_type`
-- ----------------------------
DROP TABLE IF EXISTS `dict_value_type`;
CREATE TABLE `dict_value_type` (
  `value_type_id` int(11) DEFAULT NULL,
  `value_type_name` varchar(100) DEFAULT NULL,
  `oss_show` int(11) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of dict_value_type
-- ----------------------------
INSERT INTO `dict_value_type` VALUES ('103', 'VIP经验', '1', '');
INSERT INTO `dict_value_type` VALUES ('201', '银币', '1', '');
INSERT INTO `dict_value_type` VALUES ('202', '铁锭', '1', '');
INSERT INTO `dict_value_type` VALUES ('203', '木材', '1', '');
INSERT INTO `dict_value_type` VALUES ('204', '石料', '1', '');
INSERT INTO `dict_value_type` VALUES ('205', '粮食', '1', '');
INSERT INTO `dict_value_type` VALUES ('206', '元宝', '1', '');
