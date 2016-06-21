/*
Navicat MySQL Data Transfer

Source Server         : 霸王日志库
Source Server Version : 50544
Source Host           : 119.29.40.45:3306
Source Database       : log_android_cn_s9994

Target Server Type    : MYSQL
Target Server Version : 50544
File Encoding         : 65001

Date: 2016-01-30 11:11:19
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `dict_action`
-- ----------------------------
DROP TABLE IF EXISTS `dict_action`;
CREATE TABLE `dict_action` (
  `action_id` int(11) DEFAULT NULL,
  `action_name` varchar(255) DEFAULT NULL,
  `action_type_id` int(11) DEFAULT NULL,
  `level_req` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of dict_action
-- ----------------------------
INSERT INTO `dict_action` VALUES ('1', '重置技能', '3', '1');
INSERT INTO `dict_action` VALUES ('2', '军械碎片分解', '3', '1');
INSERT INTO `dict_action` VALUES ('3', '淘宝购买幸运币', '3', '1');
INSERT INTO `dict_action` VALUES ('4', '武将抽奖', '3', '1');
INSERT INTO `dict_action` VALUES ('5', '探宝', '3', '1');
INSERT INTO `dict_action` VALUES ('6', '使用道具', '3', '1');
INSERT INTO `dict_action` VALUES ('7', '分解军械', '3', '1');
INSERT INTO `dict_action` VALUES ('8', '军械进化', '3', '1');
INSERT INTO `dict_action` VALUES ('9', '作弊', '3', '1');
INSERT INTO `dict_action` VALUES ('10', '初始化资源', '3', '1');
INSERT INTO `dict_action` VALUES ('11', '物品购买使用', '3', '1');
INSERT INTO `dict_action` VALUES ('12', '商店购买获得道具', '3', '1');
INSERT INTO `dict_action` VALUES ('13', '建筑生产获取', '3', '1');
INSERT INTO `dict_action` VALUES ('14', '关卡挑战结束逻辑', '3', '1');
INSERT INTO `dict_action` VALUES ('15', '打开副本宝箱', '3', '1');
INSERT INTO `dict_action` VALUES ('16', '主线任务', '3', '1');
INSERT INTO `dict_action` VALUES ('17', '日常任务', '3', '1');
INSERT INTO `dict_action` VALUES ('18', '日常活跃', '3', '1');
INSERT INTO `dict_action` VALUES ('19', '请求祝福', '3', '1');
INSERT INTO `dict_action` VALUES ('20', '碎片合成军械', '3', '1');
INSERT INTO `dict_action` VALUES ('21', '修复佣兵', '3', '1');
INSERT INTO `dict_action` VALUES ('22', '背包扩充', '3', '1');
INSERT INTO `dict_action` VALUES ('23', '装备出售', '3', '1');
INSERT INTO `dict_action` VALUES ('24', '任务执行完成，队伍回来', '3', '1');
INSERT INTO `dict_action` VALUES ('25', '军衔升级', '3', '1');
INSERT INTO `dict_action` VALUES ('26', '攻击玩家基地', '3', '1');
INSERT INTO `dict_action` VALUES ('27', '攻打世界资源点', '3', '1');
INSERT INTO `dict_action` VALUES ('28', '建筑升级', '3', '1');
INSERT INTO `dict_action` VALUES ('29', '元宝购买繁荣度', '3', '1');
INSERT INTO `dict_action` VALUES ('30', '元宝购买体力', '3', '1');
INSERT INTO `dict_action` VALUES ('31', '好友祝福', '3', '1');
INSERT INTO `dict_action` VALUES ('32', '每日登陆', '3', '1');
INSERT INTO `dict_action` VALUES ('35', '声望升级', '3', '1');
INSERT INTO `dict_action` VALUES ('36', '购买金币', '3', '1');
INSERT INTO `dict_action` VALUES ('37', '取消建筑升级，生产', '3', '1');
INSERT INTO `dict_action` VALUES ('38', '取消生产', '3', '1');
INSERT INTO `dict_action` VALUES ('39', '购买建筑位', '3', '1');
INSERT INTO `dict_action` VALUES ('40', '自动恢复', '3', '1');
INSERT INTO `dict_action` VALUES ('41', '授勋', '3', '1');
INSERT INTO `dict_action` VALUES ('42', '邮件提取', '3', '1');
INSERT INTO `dict_action` VALUES ('43', '充值', '3', '1');
INSERT INTO `dict_action` VALUES ('44', '竞技场商城', '3', '1');
INSERT INTO `dict_action` VALUES ('45', '竞技场胜利', '3', '1');
INSERT INTO `dict_action` VALUES ('46', '竞技场胜利', '3', '1');
INSERT INTO `dict_action` VALUES ('47', '世界', '3', '1');
INSERT INTO `dict_action` VALUES ('48', '世界队伍返回', '3', '1');
INSERT INTO `dict_action` VALUES ('49', '竞技场挑战完成', '3', '1');
INSERT INTO `dict_action` VALUES ('50', '军团贡献值兑换', '3', '1');
INSERT INTO `dict_action` VALUES ('51', '军团捐献', '3', '1');
INSERT INTO `dict_action` VALUES ('52', '军团福利领取奖励', '3', '1');
INSERT INTO `dict_action` VALUES ('53', '极限挑战扫荡', '3', '1');
INSERT INTO `dict_action` VALUES ('54', '活动领取', '3', '1');
INSERT INTO `dict_action` VALUES ('55', '转盘', '3', '1');
INSERT INTO `dict_action` VALUES ('1001', '使用道具', '3', '1');
INSERT INTO `dict_action` VALUES ('1002', '技能升级', '3', '1');
INSERT INTO `dict_action` VALUES ('1003', '统帅升级', '3', '1');
INSERT INTO `dict_action` VALUES ('1004', '执行淘宝装备抽奖', '3', '1');
INSERT INTO `dict_action` VALUES ('1005', '强化军械', '3', '1');
INSERT INTO `dict_action` VALUES ('1006', '改造军械', '3', '1');
INSERT INTO `dict_action` VALUES ('1007', '建筑升级加速', '3', '1');
INSERT INTO `dict_action` VALUES ('1008', '建筑生产加速', '3', '1');
INSERT INTO `dict_action` VALUES ('1009', '装备升级', '3', '1');
INSERT INTO `dict_action` VALUES ('1010', '装备售出', '3', '1');
INSERT INTO `dict_action` VALUES ('1011', '分解军械', '3', '1');
INSERT INTO `dict_action` VALUES ('1012', '军械进化', '3', '1');
INSERT INTO `dict_action` VALUES ('1013', '军械碎片分解', '3', '1');
INSERT INTO `dict_action` VALUES ('1015', '战损', '3', '1');
INSERT INTO `dict_action` VALUES ('1016', '兵种改造', '3', '1');
INSERT INTO `dict_action` VALUES ('1017', '挑战副本', '3', '1');
INSERT INTO `dict_action` VALUES ('1018', '购买冒险次数', '3', '1');
INSERT INTO `dict_action` VALUES ('1019', '背包扩充', '3', '1');
INSERT INTO `dict_action` VALUES ('1020', '淘宝购买幸运币', '3', '1');
INSERT INTO `dict_action` VALUES ('1021', '装备抽奖', '3', '1');
INSERT INTO `dict_action` VALUES ('1022', '执行世界任务', '3', '1');
INSERT INTO `dict_action` VALUES ('1023', '军衔升级', '3', '1');
INSERT INTO `dict_action` VALUES ('1024', '购买荣誉', '3', '1');
INSERT INTO `dict_action` VALUES ('1025', '元宝购买体力', '3', '1');
INSERT INTO `dict_action` VALUES ('1026', '军械碎片合成军械', '3', '1');
INSERT INTO `dict_action` VALUES ('1027', '攻打世界', '3', '1');
INSERT INTO `dict_action` VALUES ('1028', '授勋获取声望', '3', '1');
INSERT INTO `dict_action` VALUES ('1029', '声望升级', '3', '1');
INSERT INTO `dict_action` VALUES ('1030', '坦克生产', '3', '1');
INSERT INTO `dict_action` VALUES ('1031', '建筑升级', '3', '1');
INSERT INTO `dict_action` VALUES ('1032', '购买建筑位', '3', '1');
INSERT INTO `dict_action` VALUES ('1033', '物品购买是有', '3', '1');
INSERT INTO `dict_action` VALUES ('1034', '制造车间生产', '3', '1');
INSERT INTO `dict_action` VALUES ('1035', '商店购买', '3', '1');
INSERT INTO `dict_action` VALUES ('1036', '重置技能', '3', '1');
INSERT INTO `dict_action` VALUES ('1037', '修复佣兵', '3', '1');
INSERT INTO `dict_action` VALUES ('1038', '使用金币完成任务', '3', '1');
INSERT INTO `dict_action` VALUES ('1039', '刷新任务', '3', '1');
INSERT INTO `dict_action` VALUES ('1040', '重置任务', '3', '1');
INSERT INTO `dict_action` VALUES ('1041', '科技升级', '3', '1');
INSERT INTO `dict_action` VALUES ('1042', '日常刷新', '3', '1');
INSERT INTO `dict_action` VALUES ('1044', '购买月卡', '3', '1');
INSERT INTO `dict_action` VALUES ('1045', '作弊', '3', '1');
INSERT INTO `dict_action` VALUES ('1046', '购买竞技场挑战次数', '3', '1');
INSERT INTO `dict_action` VALUES ('1047', '购买竞技场商店购买', '3', '1');
INSERT INTO `dict_action` VALUES ('1048', '创建军团', '3', '1');
INSERT INTO `dict_action` VALUES ('1049', '加速竞技场', '3', '1');
INSERT INTO `dict_action` VALUES ('1050', '侦查', '3', '1');
INSERT INTO `dict_action` VALUES ('1051', '世界战斗', '3', '1');
INSERT INTO `dict_action` VALUES ('1052', '军团商店贡献值兑换', '3', '1');
INSERT INTO `dict_action` VALUES ('1053', '被攻打', '3', '1');
INSERT INTO `dict_action` VALUES ('1054', '购买加速完成任务队伍', '3', '1');
INSERT INTO `dict_action` VALUES ('1055', '领取军团福利', '3', '1');
INSERT INTO `dict_action` VALUES ('1056', '军团捐献', '3', '1');
INSERT INTO `dict_action` VALUES ('1057', '迁城', '3', '1');
INSERT INTO `dict_action` VALUES ('1058', '活动购买', '3', '1');
INSERT INTO `dict_action` VALUES ('1059', '拆除建筑', '3', '1');
INSERT INTO `dict_action` VALUES ('1060', '购买自动升级', '3', '1');
INSERT INTO `dict_action` VALUES ('1', '重置技能', '4', '1');
INSERT INTO `dict_action` VALUES ('2', '军械碎片分解', '4', '1');
INSERT INTO `dict_action` VALUES ('3', '淘宝购买幸运币', '4', '1');
INSERT INTO `dict_action` VALUES ('4', '武将抽奖', '4', '1');
INSERT INTO `dict_action` VALUES ('5', '探宝', '4', '1');
INSERT INTO `dict_action` VALUES ('6', '使用道具', '4', '1');
INSERT INTO `dict_action` VALUES ('7', '分解军械', '4', '1');
INSERT INTO `dict_action` VALUES ('8', '军械进化', '4', '1');
INSERT INTO `dict_action` VALUES ('9', '作弊', '4', '1');
INSERT INTO `dict_action` VALUES ('10', '初始化资源', '4', '1');
INSERT INTO `dict_action` VALUES ('11', '物品购买使用', '4', '1');
INSERT INTO `dict_action` VALUES ('12', '商店购买获得道具', '4', '1');
INSERT INTO `dict_action` VALUES ('13', '建筑生产获取', '4', '1');
INSERT INTO `dict_action` VALUES ('14', '关卡挑战结束逻辑', '4', '1');
INSERT INTO `dict_action` VALUES ('15', '打开副本宝箱', '4', '1');
INSERT INTO `dict_action` VALUES ('16', '主线任务', '4', '1');
INSERT INTO `dict_action` VALUES ('17', '日常任务', '4', '1');
INSERT INTO `dict_action` VALUES ('18', '日常活跃', '4', '1');
INSERT INTO `dict_action` VALUES ('19', '请求祝福', '4', '1');
INSERT INTO `dict_action` VALUES ('20', '碎片合成军械', '4', '1');
INSERT INTO `dict_action` VALUES ('21', '修复佣兵', '4', '1');
INSERT INTO `dict_action` VALUES ('22', '背包扩充', '4', '1');
INSERT INTO `dict_action` VALUES ('23', '装备出售', '4', '1');
INSERT INTO `dict_action` VALUES ('24', '任务执行完成，队伍回来', '4', '1');
INSERT INTO `dict_action` VALUES ('25', '军衔升级', '4', '1');
INSERT INTO `dict_action` VALUES ('26', '攻击玩家基地', '4', '1');
INSERT INTO `dict_action` VALUES ('27', '攻打世界资源点', '4', '1');
INSERT INTO `dict_action` VALUES ('28', '建筑升级', '4', '1');
INSERT INTO `dict_action` VALUES ('29', '元宝购买繁荣度', '4', '1');
INSERT INTO `dict_action` VALUES ('30', '元宝购买体力', '4', '1');
INSERT INTO `dict_action` VALUES ('31', '好友祝福', '4', '1');
INSERT INTO `dict_action` VALUES ('32', '每日登陆', '4', '1');
INSERT INTO `dict_action` VALUES ('35', '声望升级', '4', '1');
INSERT INTO `dict_action` VALUES ('36', '购买金币', '4', '1');
INSERT INTO `dict_action` VALUES ('37', '取消建筑升级，生产', '4', '1');
INSERT INTO `dict_action` VALUES ('38', '取消生产', '4', '1');
INSERT INTO `dict_action` VALUES ('39', '购买建筑位', '4', '1');
INSERT INTO `dict_action` VALUES ('40', '自动恢复', '4', '1');
INSERT INTO `dict_action` VALUES ('41', '授勋', '4', '1');
INSERT INTO `dict_action` VALUES ('42', '邮件提取', '4', '1');
INSERT INTO `dict_action` VALUES ('43', '充值', '4', '1');
INSERT INTO `dict_action` VALUES ('44', '竞技场商城', '4', '1');
INSERT INTO `dict_action` VALUES ('45', '竞技场胜利', '4', '1');
INSERT INTO `dict_action` VALUES ('46', '竞技场胜利', '4', '1');
INSERT INTO `dict_action` VALUES ('47', '世界', '4', '1');
INSERT INTO `dict_action` VALUES ('48', '世界队伍返回', '4', '1');
INSERT INTO `dict_action` VALUES ('49', '竞技场挑战完成', '4', '1');
INSERT INTO `dict_action` VALUES ('50', '军团贡献值兑换', '4', '1');
INSERT INTO `dict_action` VALUES ('51', '军团捐献', '4', '1');
INSERT INTO `dict_action` VALUES ('52', '军团福利领取奖励', '4', '1');
INSERT INTO `dict_action` VALUES ('53', '极限挑战扫荡', '4', '1');
INSERT INTO `dict_action` VALUES ('54', '活动领取', '4', '1');
INSERT INTO `dict_action` VALUES ('55', '转盘', '4', '1');
INSERT INTO `dict_action` VALUES ('1001', '使用道具', '4', '1');
INSERT INTO `dict_action` VALUES ('1002', '技能升级', '4', '1');
INSERT INTO `dict_action` VALUES ('1003', '统帅升级', '4', '1');
INSERT INTO `dict_action` VALUES ('1004', '执行淘宝装备抽奖', '4', '1');
INSERT INTO `dict_action` VALUES ('1005', '强化军械', '4', '1');
INSERT INTO `dict_action` VALUES ('1006', '改造军械', '4', '1');
INSERT INTO `dict_action` VALUES ('1007', '建筑升级加速', '4', '1');
INSERT INTO `dict_action` VALUES ('1008', '建筑生产加速', '4', '1');
INSERT INTO `dict_action` VALUES ('1009', '装备升级', '4', '1');
INSERT INTO `dict_action` VALUES ('1010', '装备售出', '4', '1');
INSERT INTO `dict_action` VALUES ('1011', '分解军械', '4', '1');
INSERT INTO `dict_action` VALUES ('1012', '军械进化', '4', '1');
INSERT INTO `dict_action` VALUES ('1013', '军械碎片分解', '4', '1');
INSERT INTO `dict_action` VALUES ('1015', '战损', '4', '1');
INSERT INTO `dict_action` VALUES ('1016', '兵种改造', '4', '1');
INSERT INTO `dict_action` VALUES ('1017', '挑战副本', '4', '1');
INSERT INTO `dict_action` VALUES ('1018', '购买冒险次数', '4', '1');
INSERT INTO `dict_action` VALUES ('1019', '背包扩充', '4', '1');
INSERT INTO `dict_action` VALUES ('1020', '淘宝购买幸运币', '4', '1');
INSERT INTO `dict_action` VALUES ('1021', '装备抽奖', '4', '1');
INSERT INTO `dict_action` VALUES ('1022', '执行世界任务', '4', '1');
INSERT INTO `dict_action` VALUES ('1023', '军衔升级', '4', '1');
INSERT INTO `dict_action` VALUES ('1024', '购买荣誉', '4', '1');
INSERT INTO `dict_action` VALUES ('1025', '元宝购买体力', '4', '1');
INSERT INTO `dict_action` VALUES ('1026', '军械碎片合成军械', '4', '1');
INSERT INTO `dict_action` VALUES ('1027', '攻打世界', '4', '1');
INSERT INTO `dict_action` VALUES ('1028', '授勋获取声望', '4', '1');
INSERT INTO `dict_action` VALUES ('1029', '声望升级', '4', '1');
INSERT INTO `dict_action` VALUES ('1030', '坦克生产', '4', '1');
INSERT INTO `dict_action` VALUES ('1031', '建筑升级', '4', '1');
INSERT INTO `dict_action` VALUES ('1032', '购买建筑位', '4', '1');
INSERT INTO `dict_action` VALUES ('1033', '物品购买是有', '4', '1');
INSERT INTO `dict_action` VALUES ('1034', '制造车间生产', '4', '1');
INSERT INTO `dict_action` VALUES ('1035', '商店购买', '4', '1');
INSERT INTO `dict_action` VALUES ('1036', '重置技能', '4', '1');
INSERT INTO `dict_action` VALUES ('1037', '修复佣兵', '4', '1');
INSERT INTO `dict_action` VALUES ('1038', '使用金币完成任务', '4', '1');
INSERT INTO `dict_action` VALUES ('1039', '刷新任务', '4', '1');
INSERT INTO `dict_action` VALUES ('1040', '重置任务', '4', '1');
INSERT INTO `dict_action` VALUES ('1041', '科技升级', '4', '1');
INSERT INTO `dict_action` VALUES ('1042', '日常刷新', '4', '1');
INSERT INTO `dict_action` VALUES ('1044', '购买月卡', '4', '1');
INSERT INTO `dict_action` VALUES ('1045', '作弊', '4', '1');
INSERT INTO `dict_action` VALUES ('1046', '购买竞技场挑战次数', '4', '1');
INSERT INTO `dict_action` VALUES ('1047', '购买竞技场商店购买', '4', '1');
INSERT INTO `dict_action` VALUES ('1048', '创建军团', '4', '1');
INSERT INTO `dict_action` VALUES ('1049', '加速竞技场', '4', '1');
INSERT INTO `dict_action` VALUES ('1050', '侦查', '4', '1');
INSERT INTO `dict_action` VALUES ('1051', '世界战斗', '4', '1');
INSERT INTO `dict_action` VALUES ('1052', '军团商店贡献值兑换', '4', '1');
INSERT INTO `dict_action` VALUES ('1053', '被攻打', '4', '1');
INSERT INTO `dict_action` VALUES ('1054', '购买加速完成任务队伍', '4', '1');
INSERT INTO `dict_action` VALUES ('1055', '领取军团福利', '4', '1');
INSERT INTO `dict_action` VALUES ('1056', '军团捐献', '4', '1');
INSERT INTO `dict_action` VALUES ('1057', '迁城', '4', '1');
INSERT INTO `dict_action` VALUES ('1058', '活动购买', '4', '1');
INSERT INTO `dict_action` VALUES ('1059', '拆除建筑', '4', '1');
INSERT INTO `dict_action` VALUES ('1060', '购买自动升级', '4', '1');
