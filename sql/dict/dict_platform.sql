/*
Navicat MySQL Data Transfer

Source Server         : 霸王日志服-119.29.40.45
Source Server Version : 50544
Source Host           : 119.29.40.45:3306
Source Database       : log_android_cn_s1

Target Server Type    : MYSQL
Target Server Version : 50544
File Encoding         : 65001

Date: 2016-04-15 21:18:21
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `dict_platform`
-- ----------------------------
DROP TABLE IF EXISTS `dict_platform`;
CREATE TABLE `dict_platform` (
  `platform_id` int(11) DEFAULT NULL,
  `platform_name` varchar(100) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of dict_platform
-- ----------------------------
INSERT INTO `dict_platform` VALUES ('0', 'kkk', '');
INSERT INTO `dict_platform` VALUES ('1', 'baidu91', '');
INSERT INTO `dict_platform` VALUES ('2', 'wandoujia', '');
INSERT INTO `dict_platform` VALUES ('3', 'qihu360', '');
INSERT INTO `dict_platform` VALUES ('4', 'game3g', '');
INSERT INTO `dict_platform` VALUES ('5', 'anfan', '');
INSERT INTO `dict_platform` VALUES ('6', 'dangle', '');
INSERT INTO `dict_platform` VALUES ('7', 'duoku', '');
INSERT INTO `dict_platform` VALUES ('8', 'huawei', '');
INSERT INTO `dict_platform` VALUES ('9', 'jinli', '');
INSERT INTO `dict_platform` VALUES ('10', 'keno', '');
INSERT INTO `dict_platform` VALUES ('11', 'xiaomi', '');
INSERT INTO `dict_platform` VALUES ('12', 'oppo', '');
INSERT INTO `dict_platform` VALUES ('13', 'uc', '');
INSERT INTO `dict_platform` VALUES ('14', 'baidu', '');
INSERT INTO `dict_platform` VALUES ('15', 'lenovo', '');
INSERT INTO `dict_platform` VALUES ('16', 'union', '');
INSERT INTO `dict_platform` VALUES ('17', 'htc', '');
INSERT INTO `dict_platform` VALUES ('18', 'syzj', '');
INSERT INTO `dict_platform` VALUES ('19', 'anzhi', '');
INSERT INTO `dict_platform` VALUES ('20', 'tengxun', '');
INSERT INTO `dict_platform` VALUES ('21', 'mogoo', '');
INSERT INTO `dict_platform` VALUES ('22', 'vivo', '');
INSERT INTO `dict_platform` VALUES ('23', 'xunlei', '');
INSERT INTO `dict_platform` VALUES ('24', 'youle', '');
INSERT INTO `dict_platform` VALUES ('25', 'youmi', '');
INSERT INTO `dict_platform` VALUES ('26', 'yywan', '');
INSERT INTO `dict_platform` VALUES ('27', 'gs', '');
INSERT INTO `dict_platform` VALUES ('28', 'ddle', '');
INSERT INTO `dict_platform` VALUES ('29', 'ywan', '');
INSERT INTO `dict_platform` VALUES ('30', 'tianwan', '');
INSERT INTO `dict_platform` VALUES ('31', 'chanyou', '');
INSERT INTO `dict_platform` VALUES ('32', 'meizu', '');
INSERT INTO `dict_platform` VALUES ('33', 'yaowan_tengxun', '');
INSERT INTO `dict_platform` VALUES ('34', 'uucun', '');
INSERT INTO `dict_platform` VALUES ('35', 'zhidian', '');
INSERT INTO `dict_platform` VALUES ('36', 'baina', '');
INSERT INTO `dict_platform` VALUES ('37', 'fanyue', '');
INSERT INTO `dict_platform` VALUES ('38', 'coolpad', '');
INSERT INTO `dict_platform` VALUES ('39', 'yiguo', '');
INSERT INTO `dict_platform` VALUES ('40', 'case7', '');
INSERT INTO `dict_platform` VALUES ('41', 'zhuodong', '');
INSERT INTO `dict_platform` VALUES ('42', 'yyhui', '');
INSERT INTO `dict_platform` VALUES ('43', 'jifeng', '');
INSERT INTO `dict_platform` VALUES ('44', 'anjubao', '');
INSERT INTO `dict_platform` VALUES ('45', 'doudou', '');
INSERT INTO `dict_platform` VALUES ('46', 'lewan', '');
INSERT INTO `dict_platform` VALUES ('47', 'fanyueduowan', '');
INSERT INTO `dict_platform` VALUES ('48', 'momo', '');
INSERT INTO `dict_platform` VALUES ('49', 'huanliu', '');
INSERT INTO `dict_platform` VALUES ('50', 'mogoo360', '');
INSERT INTO `dict_platform` VALUES ('51', 'sdk4399', '');
INSERT INTO `dict_platform` VALUES ('52', 'youku', '');
INSERT INTO `dict_platform` VALUES ('53', 'xmw', '');
