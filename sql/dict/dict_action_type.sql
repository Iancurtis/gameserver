/*
Navicat MySQL Data Transfer

Source Server         : 霸王日志服-119.29.40.45
Source Server Version : 50544
Source Host           : 119.29.40.45:3306
Source Database       : log_android_cn_s1

Target Server Type    : MYSQL
Target Server Version : 50544
File Encoding         : 65001

Date: 2016-04-15 21:17:34
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `dict_action_type`
-- ----------------------------
DROP TABLE IF EXISTS `dict_action_type`;
CREATE TABLE `dict_action_type` (
  `action_type_id` int(11) DEFAULT NULL,
  `action_type_name` varchar(100) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of dict_action_type
-- ----------------------------
INSERT INTO `dict_action_type` VALUES ('1', '功能参与度字典', '');
INSERT INTO `dict_action_type` VALUES ('2', '物品商城购买字典', '');
INSERT INTO `dict_action_type` VALUES ('3', '道具产出与消耗字典', '');
INSERT INTO `dict_action_type` VALUES ('4', '货币变动字典', '');
INSERT INTO `dict_action_type` VALUES ('5', '副本参与度字典', '');
INSERT INTO `dict_action_type` VALUES ('6', '道具流通字典', '');
INSERT INTO `dict_action_type` VALUES ('7', '地图字典', '');
INSERT INTO `dict_action_type` VALUES ('8', '事件字典', '');
INSERT INTO `dict_action_type` VALUES ('9', '活动字典', '');
INSERT INTO `dict_action_type` VALUES ('10', '战场参与度字典', '');
INSERT INTO `dict_action_type` VALUES ('11', '战斗技能字典', '');
INSERT INTO `dict_action_type` VALUES ('12', 'PVP字典', '');
INSERT INTO `dict_action_type` VALUES ('13', '福利字典', '');
INSERT INTO `dict_action_type` VALUES ('14', '聊天频道字典', '');
