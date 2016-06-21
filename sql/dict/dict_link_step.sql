/*
Navicat MySQL Data Transfer

Source Server         : 霸王日志服-119.29.40.45
Source Server Version : 50544
Source Host           : 119.29.40.45:3306
Source Database       : log_android_cn_s1

Target Server Type    : MYSQL
Target Server Version : 50544
File Encoding         : 65001

Date: 2016-04-15 21:18:14
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `dict_link_step`
-- ----------------------------
DROP TABLE IF EXISTS `dict_link_step`;
CREATE TABLE `dict_link_step` (
  `step_id` int(11) DEFAULT NULL,
  `next_step_id` int(11) DEFAULT NULL,
  `step_name` varchar(100) DEFAULT NULL,
  `order_id` int(11) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of dict_link_step
-- ----------------------------
INSERT INTO `dict_link_step` VALUES ('101001', '101002', '主公！您终于来了！赶紧击溃黄巾军吧！', '6', '');
INSERT INTO `dict_link_step` VALUES ('101002', '101003', '让臣妾辅助主公战斗吧！', '7', '');
INSERT INTO `dict_link_step` VALUES ('101003', '101004', '等待玩家退出战斗', '8', '');
INSERT INTO `dict_link_step` VALUES ('101004', '101005', '开局大胜，再拿下一城可获得弓兵奖励！', '9', '');
INSERT INTO `dict_link_step` VALUES ('101005', '101006', '等待玩家退出战斗', '10', '');
INSERT INTO `dict_link_step` VALUES ('101006', '102001', '主公注定流芳百世！请留下名号！', '11', '');
INSERT INTO `dict_link_step` VALUES ('102001', '102002', '恭喜主公，凯旋归营！这次战利品真丰厚。', '12', '');
INSERT INTO `dict_link_step` VALUES ('102002', '102003', '打关卡掉落战法秘籍、统率书、经验', '13', '');
INSERT INTO `dict_link_step` VALUES ('102003', '102004', '升统率、出战更多士兵，战力飙得快', '14', '');
INSERT INTO `dict_link_step` VALUES ('102004', '102005', '干得漂亮，是时候提升战法了', '15', '');
INSERT INTO `dict_link_step` VALUES ('102005', '102006', '升级命中战法，提升攻击命中率', '16', '');
INSERT INTO `dict_link_step` VALUES ('102006', '102007', '主公战力更强，我们检验一番', '17', '');
INSERT INTO `dict_link_step` VALUES ('102007', '102008', '试试提升了统率、战法的威力', '18', '');
INSERT INTO `dict_link_step` VALUES ('102008', '102009', '等待玩家退出战斗', '19', '');
INSERT INTO `dict_link_step` VALUES ('102009', '102010', '再下一城，可以获得更多奖励！', '20', '');
INSERT INTO `dict_link_step` VALUES ('102010', '102011', '等待玩家退出战斗', '21', '');
INSERT INTO `dict_link_step` VALUES ('102011', '102012', '恭喜主公，成功招降武将！', '22', '');
INSERT INTO `dict_link_step` VALUES ('102012', '102013', '移动到将军府', '23', '');
INSERT INTO `dict_link_step` VALUES ('102013', '102014', '有了武将，记得来将军府上阵', '24', '');
INSERT INTO `dict_link_step` VALUES ('102014', '102015', '一键上阵，战力快速提升', '25', '');
INSERT INTO `dict_link_step` VALUES ('102015', '102016', '试试武将的威力', '26', '');
INSERT INTO `dict_link_step` VALUES ('102016', '102017', '摧毁敌军可获得步兵小队', '27', '');
INSERT INTO `dict_link_step` VALUES ('102017', '102018', '等待玩家退出战斗', '28', '');
INSERT INTO `dict_link_step` VALUES ('102018', '102019', '摧毁敌军，缴获军资训练步兵', '29', '');
INSERT INTO `dict_link_step` VALUES ('102019', '102020', '等待玩家退出战斗', '30', '');
INSERT INTO `dict_link_step` VALUES ('102020', '102021', '缴获军资，征招士兵', '31', '');
INSERT INTO `dict_link_step` VALUES ('102021', '102022', '移动到兵营', '32', '');
INSERT INTO `dict_link_step` VALUES ('102022', '102023', '兵营中可征招步兵', '33', '');
INSERT INTO `dict_link_step` VALUES ('102023', '102024', '点击招兵', '34', '');
INSERT INTO `dict_link_step` VALUES ('102024', '102025', '兵营可征召步、骑、枪、弓四类兵种', '35', '');
INSERT INTO `dict_link_step` VALUES ('102025', '102026', '我们先征招步兵，提升战力', '36', '');
INSERT INTO `dict_link_step` VALUES ('102026', '102027', '加速征召，立即获得步兵', '37', '');
INSERT INTO `dict_link_step` VALUES ('102027', '102028', '要快，敌军来了', '38', '');
INSERT INTO `dict_link_step` VALUES ('102028', '102029', '关闭兵营，检验新的步兵部队吧', '39', '');
INSERT INTO `dict_link_step` VALUES ('102029', '102030', '带领新部队，再攻下新的城池吧', '40', '');
INSERT INTO `dict_link_step` VALUES ('102030', '102031', '用压倒性的步兵数量压碾他们', '41', '');
INSERT INTO `dict_link_step` VALUES ('102031', '102032', '等待玩家退出战斗', '42', '');
INSERT INTO `dict_link_step` VALUES ('102032', '102033', '张角善于弓兵，我们用步兵克制它', '43', '');
INSERT INTO `dict_link_step` VALUES ('102033', '102034', '等待玩家退出战斗', '44', '');
INSERT INTO `dict_link_step` VALUES ('102034', '102035', '战绩不错，回营发展', '45', '');
INSERT INTO `dict_link_step` VALUES ('102035', '102036', '主公，资源是升建筑是发展的保证。', '46', '');
INSERT INTO `dict_link_step` VALUES ('102036', '102037', '移动到野外空地', '47', '');
INSERT INTO `dict_link_step` VALUES ('102037', '102038', '空地可建造铁、石、木三种资源建筑', '48', '');
INSERT INTO `dict_link_step` VALUES ('102038', '102039', '下达建造铁矿场的命令', '49', '');
INSERT INTO `dict_link_step` VALUES ('102039', '102040', '移动到官邸', '50', '');
INSERT INTO `dict_link_step` VALUES ('102040', '102041', '官邸是其他建筑升级的前提', '51', '');
INSERT INTO `dict_link_step` VALUES ('102041', '102042', '升级官邸，建造高级建筑', '52', '');
INSERT INTO `dict_link_step` VALUES ('102042', '102043', '升级建筑，完成任务加快发展！', '53', '');
INSERT INTO `dict_link_step` VALUES ('102043', '102044', '领取任务奖励', '54', '');
INSERT INTO `dict_link_step` VALUES ('102044', '102045', '黄巾军又来偷袭，赶紧反击', '55', '');
INSERT INTO `dict_link_step` VALUES ('102045', '102046', '再下一城，继续摧毁黄巾贼的弓兵', '56', '');
INSERT INTO `dict_link_step` VALUES ('102046', '102047', '等待玩家退出战斗', '57', '');
INSERT INTO `dict_link_step` VALUES ('102047', '0', '恭喜，新手引导就要结束了，要记得多升级建筑，多招士兵哦', '58', '');
INSERT INTO `dict_link_step` VALUES ('101999', '0', '跳过', '59', '');
INSERT INTO `dict_link_step` VALUES ('102999', '0', '跳过', '60', null);
