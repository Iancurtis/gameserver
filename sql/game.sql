/*
Navicat MySQL Data Transfer

Source Server         : 内网测试服
Source Server Version : 50709
Source Host           : 192.168.10.190:3306
Source Database       : game_s9994

Target Server Type    : MYSQL
Target Server Version : 50709
File Encoding         : 65001

Date: 2016-01-22 14:45:15
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `Activity`
-- ----------------------------
DROP TABLE IF EXISTS `Activity`;
CREATE TABLE `Activity` (
  `id` bigint(20) unsigned NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `Arena`
-- ----------------------------
DROP TABLE IF EXISTS `Arena`;
CREATE TABLE `Arena` (
  `id` bigint(20) unsigned NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of Arena
-- ----------------------------

-- ----------------------------
-- Table structure for `Armygroup`
-- ----------------------------
DROP TABLE IF EXISTS `Armygroup`;
CREATE TABLE `Armygroup` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of Armygroup
-- ----------------------------

-- ----------------------------
-- Table structure for `ArmygroupMenber`
-- ----------------------------
DROP TABLE IF EXISTS `ArmygroupMenber`;
CREATE TABLE `ArmygroupMenber` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ArmygroupMenber
-- ----------------------------

-- ----------------------------
-- Table structure for `ArmyGroupTech`
-- ----------------------------
DROP TABLE IF EXISTS `ArmyGroupTech`;
CREATE TABLE `ArmyGroupTech` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ArmyGroupTech
-- ----------------------------

-- ----------------------------
-- Table structure for `ClientCache`
-- ----------------------------
DROP TABLE IF EXISTS `ClientCache`;
CREATE TABLE `ClientCache` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ClientCache
-- ----------------------------

-- ----------------------------
-- Table structure for `Dungeo`
-- ----------------------------
DROP TABLE IF EXISTS `Dungeo`;
CREATE TABLE `Dungeo` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of Dungeo
-- ----------------------------

-- ----------------------------
-- Table structure for `Equip`
-- ----------------------------
DROP TABLE IF EXISTS `Equip`;
CREATE TABLE `Equip` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of Equip
-- ----------------------------

-- ----------------------------
-- Table structure for `FormationMember`
-- ----------------------------
DROP TABLE IF EXISTS `FormationMember`;
CREATE TABLE `FormationMember` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of FormationMember
-- ----------------------------

-- ----------------------------
-- Table structure for `Item`
-- ----------------------------
DROP TABLE IF EXISTS `Item`;
CREATE TABLE `Item` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of Item
-- ----------------------------

-- ----------------------------
-- Table structure for `ItemBuff`
-- ----------------------------
DROP TABLE IF EXISTS `ItemBuff`;
CREATE TABLE `ItemBuff` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ItemBuff
-- ----------------------------

-- ----------------------------
-- Table structure for `Mail`
-- ----------------------------
DROP TABLE IF EXISTS `Mail`;
CREATE TABLE `Mail` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of Mail
-- ----------------------------

-- ----------------------------
-- Table structure for `Museum`
-- ----------------------------
DROP TABLE IF EXISTS `Museum`;
CREATE TABLE `Museum` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of Museum
-- ----------------------------

-- ----------------------------
-- Table structure for `Ordnance`
-- ----------------------------
DROP TABLE IF EXISTS `Ordnance`;
CREATE TABLE `Ordnance` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of Ordnance
-- ----------------------------

-- ----------------------------
-- Table structure for `OrdnancePiece`
-- ----------------------------
DROP TABLE IF EXISTS `OrdnancePiece`;
CREATE TABLE `OrdnancePiece` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of OrdnancePiece
-- ----------------------------

-- ----------------------------
-- Table structure for `PerformTasks`
-- ----------------------------
DROP TABLE IF EXISTS `PerformTasks`;
CREATE TABLE `PerformTasks` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of PerformTasks
-- ----------------------------

-- ----------------------------
-- Table structure for `Player`
-- ----------------------------
DROP TABLE IF EXISTS `Player`;
CREATE TABLE `Player` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `Report`
-- ----------------------------
DROP TABLE IF EXISTS `Report`;
CREATE TABLE `Report` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of Report
-- ----------------------------

-- ----------------------------
-- Table structure for `ResFunBuilding`
-- ----------------------------
DROP TABLE IF EXISTS `ResFunBuilding`;
CREATE TABLE `ResFunBuilding` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ResFunBuilding
-- ----------------------------

-- ----------------------------
-- Table structure for `Resource`
-- ----------------------------
DROP TABLE IF EXISTS `Resource`;
CREATE TABLE `Resource` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of Resource
-- ----------------------------

-- ----------------------------
-- Table structure for `Skill`
-- ----------------------------
DROP TABLE IF EXISTS `Skill`;
CREATE TABLE `Skill` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of Skill
-- ----------------------------

-- ----------------------------
-- Table structure for `Soldier`
-- ----------------------------
DROP TABLE IF EXISTS `Soldier`;
CREATE TABLE `Soldier` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of Soldier
-- ----------------------------

-- ----------------------------
-- Table structure for `Task`
-- ----------------------------
DROP TABLE IF EXISTS `Task`;
CREATE TABLE `Task` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `TeamDate`
-- ----------------------------
DROP TABLE IF EXISTS `TeamDate`;
CREATE TABLE `TeamDate` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of TeamDate
-- ----------------------------

-- ----------------------------
-- Table structure for `Technology`
-- ----------------------------
DROP TABLE IF EXISTS `Technology`;
CREATE TABLE `Technology` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of Technology
-- ----------------------------

-- ----------------------------
-- Table structure for `Timerdb`
-- ----------------------------
DROP TABLE IF EXISTS `Timerdb`;
CREATE TABLE `Timerdb` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for `WorldBuilding`
-- ----------------------------
DROP TABLE IF EXISTS `WorldBuilding`;
CREATE TABLE `WorldBuilding` (
  `id` bigint(20) NOT NULL,
  `data` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
