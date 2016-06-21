/*
Navicat MySQL Data Transfer

Source Server         : 霸王日志服-119.29.40.45
Source Server Version : 50544
Source Host           : 119.29.40.45:3306
Source Database       : log_android_cn_s1

Target Server Type    : MYSQL
Target Server Version : 50544
File Encoding         : 65001

Date: 2016-04-15 21:17:39
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `dict_chat_channel`
-- ----------------------------
DROP TABLE IF EXISTS `dict_chat_channel`;
CREATE TABLE `dict_chat_channel` (
  `channel_id` int(11) DEFAULT NULL,
  `channel_name` varchar(100) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of dict_chat_channel
-- ----------------------------
INSERT INTO `dict_chat_channel` VALUES ('1', '世界', '');
INSERT INTO `dict_chat_channel` VALUES ('2', '军团', '');
INSERT INTO `dict_chat_channel` VALUES ('3', '客服', '');
INSERT INTO `dict_chat_channel` VALUES ('4', '私聊', '');
