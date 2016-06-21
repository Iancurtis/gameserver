-- MySQL dump 10.13  Distrib 5.5.44, for debian-linux-gnu (x86_64)
--
-- Host: 127.0.0.1    Database: log_android_cn_s9992
-- ------------------------------------------------------
-- Server version	5.5.44-0ubuntu0.14.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `dict_action`
--

DROP TABLE IF EXISTS `dict_action`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dict_action` (
  `action_id` int(11) DEFAULT NULL,
  `action_name` varchar(100) DEFAULT NULL,
  `action_type_id` int(11) DEFAULT NULL,
  `level_req` int(11) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dict_action_type`
--

DROP TABLE IF EXISTS `dict_action_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dict_action_type` (
  `action_type_id` int(11) DEFAULT NULL,
  `action_type_name` varchar(100) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dict_chat_channel`
--

DROP TABLE IF EXISTS `dict_chat_channel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dict_chat_channel` (
  `channel_id` int(11) DEFAULT NULL,
  `channel_name` varchar(100) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dict_color`
--

DROP TABLE IF EXISTS `dict_color`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dict_color` (
  `color_id` int(11) DEFAULT NULL,
  `color_name` varchar(100) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dict_fb`
--

DROP TABLE IF EXISTS `dict_fb`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dict_fb` (
  `fb_id` int(11) DEFAULT NULL,
  `fb_type` int(11) DEFAULT NULL,
  `task_name` varchar(100) DEFAULT NULL,
  `level_req_min` int(11) DEFAULT NULL,
  `level_req_max` int(11) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dict_fb_type`
--

DROP TABLE IF EXISTS `dict_fb_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dict_fb_type` (
  `fb_type_id` int(11) DEFAULT NULL,
  `fb_type_name` varchar(100) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dict_item`
--

DROP TABLE IF EXISTS `dict_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dict_item` (
  `item_id` int(11) DEFAULT NULL,
  `item_name` varchar(100) DEFAULT NULL,
  `quality` int(11) DEFAULT NULL,
  `level_req` int(11) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dict_link_step`
--

DROP TABLE IF EXISTS `dict_link_step`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dict_link_step` (
  `step_id` int(11) DEFAULT NULL,
  `next_step_id` int(11) DEFAULT NULL,
  `step_name` varchar(255) DEFAULT NULL,
  `order_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dict_platform`
--

DROP TABLE IF EXISTS `dict_platform`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dict_platform` (
  `platform_id` int(11) NOT NULL DEFAULT '0' COMMENT '平台id',
  `platform_name` varchar(20) NOT NULL DEFAULT '' COMMENT '平台名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dict_pvp_type`
--

DROP TABLE IF EXISTS `dict_pvp_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dict_pvp_type` (
  `pvp_type_id` int(11) DEFAULT NULL,
  `pvp_type_name` varchar(100) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dict_shop_type`
--

DROP TABLE IF EXISTS `dict_shop_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dict_shop_type` (
  `shop_type_id` int(11) DEFAULT NULL,
  `shop_type_name` varchar(100) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dict_task`
--

DROP TABLE IF EXISTS `dict_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dict_task` (
  `task_id` int(11) DEFAULT NULL,
  `task_type` int(11) DEFAULT NULL,
  `task_name` varchar(100) DEFAULT NULL,
  `level_req_min` int(11) DEFAULT NULL,
  `level_req_max` int(11) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dict_task_type`
--

DROP TABLE IF EXISTS `dict_task_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dict_task_type` (
  `task_type_id` int(11) DEFAULT NULL,
  `task_type_name` varchar(100) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dict_value_type`
--

DROP TABLE IF EXISTS `dict_value_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dict_value_type` (
  `value_type_id` int(11) DEFAULT NULL,
  `value_type_name` varchar(100) DEFAULT NULL,
  `oss_show` int(11) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tblPayDimensionConfig`
--

DROP TABLE IF EXISTS `tblPayDimensionConfig`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tblPayDimensionConfig` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `Area` varchar(10) NOT NULL DEFAULT '' COMMENT '����',
  `MinPay` decimal(15,2) DEFAULT '0.00' COMMENT '��������',
  `MaxPay` decimal(15,2) DEFAULT '0.00' COMMENT '��������',
  PRIMARY KEY (`Id`),
  UNIQUE KEY `Area_MinPay_MaxPay` (`Area`,`MinPay`,`MaxPay`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8 COMMENT='�û������������ñ�';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_activity`
--

DROP TABLE IF EXISTS `tbllog_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_activity` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `dim_level` int(11) DEFAULT NULL COMMENT '玩家等级',
  `action_id` int(11) DEFAULT NULL COMMENT '功能ID(对应 dict_action.action_id)',
  `status` int(11) DEFAULT NULL COMMENT '状态（1=参与，2=完成；如果没有统计完成，系统会默认参与即是完成）',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间，索引字段',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_auction`
--

DROP TABLE IF EXISTS `tbllog_auction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_auction` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `auction_id` int(11) DEFAULT NULL COMMENT '拍卖交易ID',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `opt_type_id` int(11) DEFAULT NULL COMMENT '拍卖操作类型（1=寄售，2=流拍，3=出售）',
  `item_id` int(11) DEFAULT NULL COMMENT '拍卖物品ID',
  `item_number` bigint(20) DEFAULT NULL COMMENT '拍卖物品数量',
  `bid_price_list` varchar(200) DEFAULT NULL COMMENT '拍卖价格，如：{gold:1,bgold:2,coin:3,bcoin:4}（以json格式记录，其中gold为元宝，bgold为绑定元宝，后面数值为相应的数量）',
  `a_price_list` varchar(200) DEFAULT NULL COMMENT '一口价价格, 如: {gold:1, bgold:2,coin:3,bcoin:4} (以json格式记录, 其中gold为元宝, bgold为绑定元宝, 后面数值为相应的数量)',
  `happend_time` int(11) DEFAULT NULL COMMENT '拍卖事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_battle`
--

DROP TABLE IF EXISTS `tbllog_battle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_battle` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `dim_level` int(11) DEFAULT NULL COMMENT '玩家等级',
  `battle_id` int(11) DEFAULT NULL COMMENT '战场ID（对应 dict_action.action_id）',
  `time_duration` int(11) DEFAULT NULL COMMENT '战场停留时长',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间，索引字段',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_box`
--

DROP TABLE IF EXISTS `tbllog_box`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_box` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `box_type` int(11) DEFAULT NULL COMMENT '宝箱类型（金宝箱/银宝箱等）',
  `item_id` int(11) DEFAULT NULL COMMENT '获得道具ID',
  `item_number` bigint(20) DEFAULT NULL COMMENT '获得道具数量',
  `money_type` int(11) DEFAULT NULL COMMENT '获得货币类型',
  `amount` bigint(20) DEFAULT NULL COMMENT '获得货币数量',
  `source_data` varchar(300) DEFAULT NULL COMMENT '在一次打开宝箱时获得多个物品时，可以通过json的格式记录在该字段中，而此时，item_id与item_number留空',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_buy`
--

DROP TABLE IF EXISTS `tbllog_buy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_buy` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `sales_id` int(11) DEFAULT NULL COMMENT '寄卖流水号',
  `role_id` bigint(20) DEFAULT NULL COMMENT '寄卖品角色ID',
  `item_id` int(11) DEFAULT NULL COMMENT '寄卖品ID',
  `price_type` int(11) DEFAULT NULL COMMENT '寄卖品价格货币类型(铜钱/元宝/…) (1=金币，2=绑定金币，3=铜币，4=绑定铜币)',
  `price_unit` int(11) DEFAULT NULL COMMENT '寄卖品价格',
  `item_number` bigint(20) DEFAULT NULL COMMENT '寄卖品物品数量',
  `action_id` int(11) DEFAULT NULL COMMENT '寄卖操作行为ID',
  `happend_time` int(11) DEFAULT NULL COMMENT '寄卖行为记录时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_chat`
--

DROP TABLE IF EXISTS `tbllog_chat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_chat` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `role_name` varchar(50) DEFAULT NULL COMMENT '角色名',
  `dim_level` int(11) DEFAULT NULL COMMENT '玩家等级',
  `user_ip` varchar(30) DEFAULT NULL COMMENT '玩家IP',
  `channel` int(11) DEFAULT NULL COMMENT '聊天频道（提供字典表）',
  `msg` varchar(200) DEFAULT NULL COMMENT '聊天信息',
  `type` int(11) DEFAULT NULL COMMENT '内容类型（0代表语言，1代表文本）',
  `target_role_id` bigint(20) DEFAULT NULL COMMENT '两天对象ID',
  `happend_time` int(11) DEFAULT NULL COMMENT '聊天发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '日志记录时间',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_checkin`
--

DROP TABLE IF EXISTS `tbllog_checkin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_checkin` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `dim_level` int(11) DEFAULT NULL COMMENT '玩家等级',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间，索引字段',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_complaints`
--

DROP TABLE IF EXISTS `tbllog_complaints`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_complaints` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `complaint_id` int(11) DEFAULT NULL COMMENT '投诉编号，区服唯一',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `role_name` varchar(50) DEFAULT NULL COMMENT '角色名称',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `game_abbrv` varchar(30) DEFAULT NULL COMMENT '游戏简称（由XXX填写）',
  `sid` int(11) DEFAULT NULL COMMENT '游戏服编号（由XXX填写）',
  `complaint_type` int(11) DEFAULT NULL COMMENT '投诉类型("全部",11="bug",12="投诉",13="建议",10="其他", 15="咨询"")',
  `complaint_title` varchar(100) DEFAULT NULL COMMENT '投诉的标题',
  `complaint_content` varchar(250) DEFAULT NULL COMMENT '投诉的正文',
  `complaint_time` int(11) DEFAULT NULL COMMENT '玩家提交投诉的时间',
  `internal_id` int(11) DEFAULT NULL COMMENT '内部编号（由XXX填写）',
  `reply_cnts` int(11) DEFAULT NULL COMMENT 'GM回帖数（由XXX填写）',
  `user_ip` varchar(30) DEFAULT NULL COMMENT '用户IP（可不填）',
  `agent` varchar(100) DEFAULT NULL COMMENT '代理商名称，如XXX(可不填)',
  `pay_amount` int(11) DEFAULT NULL COMMENT '玩家已经充值总额（可不填）',
  `qq_account` bigint(20) DEFAULT NULL COMMENT '玩家的qq账号（可不填）',
  `dim_level` int(11) DEFAULT NULL COMMENT '玩家等级（可不填）',
  `evaluate` int(11) DEFAULT NULL COMMENT '评分：0未评，1优秀，2一般，3很差（可不填）',
  `sync_numbers` int(11) DEFAULT NULL COMMENT '同步次数（可不填）',
  `last_reply_time` int(11) DEFAULT NULL COMMENT '最后回复时间（可不填）',
  `is_spam` int(11) DEFAULT NULL COMMENT '是否标记为垃圾问题（可不填）',
  `spam_reporter` varchar(100) DEFAULT NULL COMMENT 'spam注释（可不填）',
  `spam_time` int(11) DEFAULT NULL COMMENT 'spam生成时间（可不填）',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_deal`
--

DROP TABLE IF EXISTS `tbllog_deal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_deal` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `deal_id` int(11) DEFAULT NULL COMMENT '交易ID',
  `role_id` bigint(20) DEFAULT NULL COMMENT '交易出物品角色ID',
  `owner_id` bigint(20) DEFAULT NULL COMMENT '获得物品角色ID',
  `item_id` int(11) DEFAULT NULL COMMENT '交易出物品ID',
  `item_number` bigint(20) DEFAULT NULL COMMENT '交易出物品数量',
  `owner_item_id` int(11) DEFAULT NULL COMMENT '交易进物品ID',
  `owner_item_number` bigint(20) DEFAULT NULL COMMENT '交易进物品数量',
  `status` int(11) DEFAULT NULL COMMENT '交易状态（1=成功，2=取消，3=失败）',
  `happend_time` int(11) DEFAULT NULL COMMENT '交易时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_equipment`
--

DROP TABLE IF EXISTS `tbllog_equipment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_equipment` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `dim_level` int(11) DEFAULT NULL COMMENT '玩家等级',
  `item_id` int(11) DEFAULT NULL COMMENT '装备ID',
  `item_property` int(11) DEFAULT NULL COMMENT '装备属性',
  `value_before` int(11) DEFAULT NULL COMMENT '装备变化前数值',
  `value_after` int(11) DEFAULT NULL COMMENT '装备变化后数值',
  `change_type` int(11) DEFAULT NULL COMMENT '装备经过锻造或合成后，装备状态变化的类型。\r\n		1=品质/强化等级2=属性变化通常品质/强化等级发生变化时，属性也会发生变化。\r\n		此时只需要记录是品质/强化等级发生变化即可。当单纯发生属性变化时才记录为属性变化。（通常对应游戏的功能叫“洗练”）',
  `material` varchar(1000) DEFAULT NULL COMMENT '所需的材料，可能是道具或货币\r\n		通过json格式数组来记录，每个json数组元素第一个值为道具ID或者货币类型，第二个值为数量，第三个值为类型，用于区分道具还是货币，道具为1，货币为2\r\n		[{item_id1, amount1, type},{item_id2, amount2, type},{money_type3, amount3, type}]\r\n		例如：[{111111, 10, 1},{222222, 5, 1},{1, 10000, 2}]',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间，索引字段',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_error`
--

DROP TABLE IF EXISTS `tbllog_error`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_error` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `did` varchar(100) DEFAULT NULL COMMENT '用户设备ID',
  `game_version` varchar(50) DEFAULT NULL COMMENT '游戏版本号',
  `os` varchar(30) DEFAULT NULL COMMENT '手游专用手机操作系统，如：android、iOS',
  `os_version` varchar(30) DEFAULT NULL COMMENT '手游专用操作系统版本号，如：2.3.4',
  `device` varchar(30) DEFAULT NULL COMMENT '手游专用设备名称，如：三星GT-S5830',
  `device_type` varchar(30) DEFAULT NULL COMMENT '手游专用设备类型，如：xiaomi、samsung、apple',
  `screen` varchar(20) DEFAULT NULL COMMENT '手游专用屏幕分辨率，如：480*800',
  `mno` varchar(30) DEFAULT NULL COMMENT '手游专用移动网络运营商(mobile network operators)，如：中国移动、中国联通',
  `nm` varchar(20) DEFAULT NULL COMMENT '手游专用联网方式(Networking mode)，如：3G、WIFI',
  `happend_time` int(11) DEFAULT NULL COMMENT '错误发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间，索引字段',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_event`
--

DROP TABLE IF EXISTS `tbllog_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_event` (
  `platform` varchar(30) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `event_id` int(11) DEFAULT NULL COMMENT '事件ID（每个游戏自定义，对应dict_link_step.StepId)',
  `user_ip` varchar(30) DEFAULT NULL COMMENT '用户IP',
  `did` varchar(100) DEFAULT NULL COMMENT '用户设备ID',
  `game_version` varchar(50) DEFAULT NULL COMMENT '游戏版本号',
  `os` varchar(30) DEFAULT NULL COMMENT '手游专用,手机操作系统,如：android,IOS',
  `os_version` varchar(50) DEFAULT NULL COMMENT '手游专用,操作系统版本号,如：2.3.4',
  `device` varchar(50) DEFAULT NULL COMMENT '手游专用,设备名称,如：三星 GT-S5830',
  `device_type` varchar(30) DEFAULT NULL COMMENT '手游专用,设备类型,如：xiaomi,samsung,apple',
  `screen` varchar(20) DEFAULT NULL COMMENT '手游专用,屏幕分辨率,如：480*800',
  `mno` varchar(20) DEFAULT NULL COMMENT '手游专用,移动网络运营商(mobile network operators)，如：中国移动、中国联通',
  `nm` varchar(10) DEFAULT NULL COMMENT '手游专用,联网方式(Networking mode)，如：3G、WIFI',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间，索引字段',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_fb`
--

DROP TABLE IF EXISTS `tbllog_fb`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_fb` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `fb_id` int(11) DEFAULT NULL COMMENT '副本ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `dim_level` int(11) DEFAULT NULL COMMENT '玩家等级',
  `fb_level` int(11) DEFAULT NULL COMMENT '副本层数/关卡数',
  `status` int(11) DEFAULT NULL COMMENT '状态（1=参与，2=完成；3=退出，4=超时）',
  `death_cnt` int(11) DEFAULT NULL COMMENT '死亡角色数量（一次副本死亡的次数）',
  `happend_time` int(11) DEFAULT NULL COMMENT '参与时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_function`
--

DROP TABLE IF EXISTS `tbllog_function`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_function` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `dim_level` int(11) DEFAULT NULL COMMENT '玩家等级',
  `action_id` int(11) DEFAULT NULL COMMENT '功能ID(对应 dict_action.action_id)',
  `status` int(11) DEFAULT NULL COMMENT '状态（1=参与，2=完成；如果没有统计完成，系统会默认参与即是完成）',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间，索引字段',
  `expand1` bigint(20) DEFAULT '0' COMMENT '扩展属性1',
  `expand2` bigint(20) DEFAULT '0' COMMENT '扩展属性2',
  `expand3` bigint(20) DEFAULT '0' COMMENT '扩展属性3',
  `expandstr` varchar(127) DEFAULT NULL COMMENT '扩展属性字符串',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_gold`
--

DROP TABLE IF EXISTS `tbllog_gold`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_gold` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `dim_level` int(11) DEFAULT NULL COMMENT '玩家等级',
  `dim_prof` int(11) DEFAULT NULL COMMENT '职业ID',
  `money_type` int(11) DEFAULT NULL COMMENT '货币类型(1=金币，2=绑定金币，3=铜币，4=绑定铜币，5=礼券，6=积分/荣誉，7=兑换)',
  `amount` bigint(20) DEFAULT NULL COMMENT '货币数量',
  `money_remain` bigint(20) DEFAULT NULL COMMENT '剩余货币数量',
  `item_id` int(11) DEFAULT NULL COMMENT '涉及的道具ID',
  `opt` int(11) DEFAULT NULL COMMENT '货币加减(1=增加，2=减少)',
  `action_1` int(11) DEFAULT NULL COMMENT '行为分类1（一级消费点）对应(dict_action.action_1)',
  `action_2` int(11) DEFAULT NULL COMMENT '若存在一级消费点，则不存在二级消费点，将二级消费点设为一级消费点的值',
  `item_number` bigint(20) DEFAULT NULL COMMENT '物品数量',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间，索引字段',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_guild`
--

DROP TABLE IF EXISTS `tbllog_guild`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_guild` (
  `member_role_id` bigint(20) DEFAULT NULL COMMENT '申请、退出等发起操作角色ID',
  `opt_role_id` bigint(20) DEFAULT NULL COMMENT '批准加入的操作的角色ID',
  `guild_id` int(11) DEFAULT NULL COMMENT '帮派ID',
  `guild_name` varchar(32) DEFAULT NULL COMMENT '帮派名称',
  `opt` tinyint(4) DEFAULT NULL COMMENT '操作类型（1-申请，2-加入，3-退出）',
  `score` bigint(20) DEFAULT NULL COMMENT '帮派积分',
  `contribution` int(11) DEFAULT NULL COMMENT '帮派贡献',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_items`
--

DROP TABLE IF EXISTS `tbllog_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_items` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `dim_level` int(11) DEFAULT NULL COMMENT '玩家等级',
  `opt` int(11) DEFAULT NULL COMMENT '操作类型(0是使用，1是增加，2是修改)',
  `action_id` int(11) DEFAULT NULL COMMENT '对应各自项目组的道具消耗项目字典，行为类型（dict_action.action_id)',
  `item_id` int(11) DEFAULT NULL COMMENT '道具ID',
  `item_number` bigint(20) DEFAULT NULL COMMENT '道具获得/消耗数量',
  `map_id` int(11) DEFAULT NULL COMMENT '物品产出所在地图，ID(dict_action.action_id)',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间，索引字段',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_level_up`
--

DROP TABLE IF EXISTS `tbllog_level_up`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_level_up` (
  `platform` varchar(30) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `last_level` int(11) DEFAULT NULL COMMENT '上一等级',
  `current_level` int(11) DEFAULT NULL COMMENT '当前等级',
  `last_exp` bigint(20) DEFAULT NULL COMMENT '上一经验值',
  `current_exp` bigint(20) DEFAULT NULL COMMENT '当前经验值',
  `happend_time` int(11) DEFAULT NULL COMMENT '变动时间',
  `log_time` int(11) DEFAULT NULL COMMENT '记录时间',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_login`
--

DROP TABLE IF EXISTS `tbllog_login`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_login` (
  `platform` varchar(30) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `dim_level` int(11) DEFAULT NULL COMMENT '等级',
  `user_ip` varchar(30) DEFAULT NULL COMMENT '登录IP',
  `login_map_id` int(11) DEFAULT NULL COMMENT '登录地图ID',
  `did` varchar(100) DEFAULT NULL COMMENT '用户设备ID',
  `game_version` varchar(50) DEFAULT NULL COMMENT '游戏版本号',
  `os` varchar(30) DEFAULT NULL COMMENT '手游专用手机操作系统，如：android、iOS',
  `os_version` varchar(30) DEFAULT NULL COMMENT '手游专用操作系统版本号，如：2.3.4',
  `device` varchar(50) DEFAULT NULL COMMENT '手游专用设备名称，如：三星GT-S5830MI 2S , Nexus 5',
  `device_type` varchar(30) DEFAULT NULL COMMENT '手游专用设备类型，如：xiaomi、samsung、apple',
  `screen` varchar(20) DEFAULT NULL COMMENT '手游专用屏幕分辨率，如：480*800',
  `mno` varchar(20) DEFAULT NULL COMMENT '手游专用,移动网络运营商(mobile network operators)，如：中国移动、中国联通',
  `nm` varchar(10) DEFAULT NULL COMMENT '手游专用,联网方式(Networking mode)，如：3G、WIFI',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间，索引字段',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_mail`
--

DROP TABLE IF EXISTS `tbllog_mail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_mail` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `mail_id` int(11) DEFAULT NULL COMMENT '邮件ID',
  `mail_sender_id` bigint(20) DEFAULT NULL COMMENT '发送者ID（角色ID）',
  `mail_sender_name` varchar(100) DEFAULT NULL COMMENT '发送者平台唯一用户标识',
  `mail_receiver_id` bigint(20) DEFAULT NULL COMMENT '接受者ID（角色ID）',
  `mail_receiver_name` varchar(100) DEFAULT NULL COMMENT '接收者平台唯一用户标识',
  `mail_title` varchar(200) DEFAULT NULL COMMENT '邮件标题',
  `mail_content` varchar(500) DEFAULT NULL COMMENT '邮件内容',
  `mail_type` int(11) DEFAULT NULL COMMENT '邮件类型（0系统邮件，1用户邮件）',
  `mail_money_list` varchar(200) DEFAULT NULL COMMENT '货币类型：数量，组合用逗号隔，如<gold:1,bind_gold:2>',
  `mail_item_list` varchar(200) DEFAULT NULL COMMENT '道具id：数量，组合用逗号隔开，如<item1:1，item2:2>',
  `mail_status` int(11) DEFAULT NULL COMMENT '邮件接收状态（1=已读，2=未读）',
  `get_status` int(11) DEFAULT NULL COMMENT '物品领取状态（1=已领取，2=未领取）',
  `happend_time` int(11) DEFAULT NULL COMMENT '变动时间',
  `log_time` int(11) DEFAULT NULL COMMENT '邮件发送时间',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_map_online`
--

DROP TABLE IF EXISTS `tbllog_map_online`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_map_online` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `map_id` int(11) DEFAULT NULL COMMENT '地图ID',
  `player_num` int(11) DEFAULT NULL COMMENT '地图人数',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间，索引字段',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_money_remain`
--

DROP TABLE IF EXISTS `tbllog_money_remain`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_money_remain` (
  `money_type` int(11) DEFAULT NULL COMMENT '货币类型',
  `money_remain` bigint(20) DEFAULT NULL COMMENT '剩余货币',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_online`
--

DROP TABLE IF EXISTS `tbllog_online`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_online` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `people` int(11) DEFAULT NULL COMMENT '当前在线玩家总人数',
  `device_cnt` bigint(20) DEFAULT NULL COMMENT '当前在线玩家总设备数',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间，索引字段',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_pay`
--

DROP TABLE IF EXISTS `tbllog_pay`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_pay` (
  `platform` varchar(30) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `user_ip` varchar(30) DEFAULT NULL COMMENT '玩家IP',
  `dim_level` int(11) DEFAULT NULL COMMENT '等级',
  `pay_type` int(11) DEFAULT NULL COMMENT '充值类型，0为测试订单',
  `order_id` varchar(50) DEFAULT NULL COMMENT '订单号',
  `pay_money` float(7,2) DEFAULT NULL COMMENT '充值金额(总充值金额)',
  `pay_gold` bigint(20) DEFAULT NULL COMMENT '充值获得的元宝/金币数',
  `did` varchar(100) DEFAULT NULL COMMENT '用户ID设备',
  `game_version` varchar(50) DEFAULT NULL COMMENT '游戏版本号',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间，索引字段',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_player`
--

DROP TABLE IF EXISTS `tbllog_player`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_player` (
  `platform` varchar(30) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `role_name` varchar(50) DEFAULT NULL COMMENT '角色名',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识（UID）',
  `user_name` varchar(100) DEFAULT NULL COMMENT '玩家帐号名称',
  `dim_nation` varchar(30) DEFAULT NULL COMMENT '阵营',
  `dim_prof` int(11) DEFAULT NULL COMMENT '职业',
  `dim_sex` tinyint(4) DEFAULT NULL COMMENT '性别(0=女，1=男，2=未知)',
  `reg_time` int(11) DEFAULT NULL COMMENT '注册时间',
  `reg_ip` varchar(30) DEFAULT NULL COMMENT '注册IP',
  `did` varchar(100) DEFAULT NULL COMMENT '用户设备ID',
  `dim_level` int(11) DEFAULT NULL COMMENT '用户等级',
  `dim_vip_level` int(11) DEFAULT NULL COMMENT 'VIP等级',
  `dim_exp` bigint(20) DEFAULT NULL COMMENT '当前经验',
  `dim_guild` varchar(100) DEFAULT NULL COMMENT '帮派名称',
  `dim_power` bigint(11) DEFAULT NULL COMMENT '战斗力',
  `dim_iron` bigint(20) DEFAULT NULL COMMENT '铁锭(铁矿)',
  `dim_tael` bigint(20) DEFAULT NULL COMMENT '银两（宝石）',
  `dim_wood` bigint(20) DEFAULT NULL COMMENT '木材（铜矿）',
  `dim_stones` bigint(20) DEFAULT NULL COMMENT '石料（石油）',
  `dim_food` bigint(20) DEFAULT NULL COMMENT '粮食（硅矿）',
  `gold_number` bigint(20) DEFAULT NULL COMMENT '元宝数（充值兑换货币）',
  `bgold_number` bigint(20) DEFAULT NULL COMMENT '绑定元宝数（非充值兑换货币）',
  `coin_number` bigint(20) DEFAULT NULL COMMENT '金币数',
  `bcoin_number` bigint(20) DEFAULT NULL COMMENT '绑定金币数',
  `pay_money` bigint(20) DEFAULT NULL COMMENT '总充值',
  `first_pay_time` bigint(20) DEFAULT NULL COMMENT '首充时间',
  `last_pay_time` bigint(20) DEFAULT NULL COMMENT '最后充值时间',
  `last_login_time` int(11) DEFAULT NULL COMMENT '最后登录时间',
  `happend_time` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_pvp`
--

DROP TABLE IF EXISTS `tbllog_pvp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_pvp` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `pvp_id` int(11) DEFAULT NULL COMMENT 'PVP唯一ID',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `dim_level` int(11) DEFAULT NULL COMMENT '玩家等级',
  `status` int(11) DEFAULT NULL COMMENT '状态（1=战胜，2战败）',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间，索引字段',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_quit`
--

DROP TABLE IF EXISTS `tbllog_quit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_quit` (
  `platform` varchar(30) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `login_level` int(11) DEFAULT NULL COMMENT '登录等级',
  `logout_level` int(11) DEFAULT NULL COMMENT '登出等级',
  `logout_ip` varchar(30) DEFAULT NULL COMMENT '登出IP',
  `login_time` int(11) DEFAULT NULL COMMENT '登录时间',
  `logout_time` int(11) DEFAULT NULL COMMENT '退出时间',
  `time_duration` int(11) DEFAULT NULL COMMENT '在线时长',
  `logout_map_id` int(11) DEFAULT NULL COMMENT '退出地图ID',
  `reason_id` int(11) DEFAULT NULL COMMENT '退出异常，或者reason对应字典表(0表示正常退出)',
  `msg` varchar(200) DEFAULT NULL COMMENT '特殊信息',
  `did` varchar(100) DEFAULT NULL COMMENT '用户设备ID',
  `game_version` varchar(50) DEFAULT NULL COMMENT '游戏版本号',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间，索引字段'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_role`
--

DROP TABLE IF EXISTS `tbllog_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_role` (
  `platform` varchar(30) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `role_name` varchar(50) DEFAULT NULL COMMENT '角色名称',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `user_ip` varchar(30) DEFAULT NULL COMMENT '玩家IP',
  `dim_prof` int(11) DEFAULT NULL COMMENT '职业ID',
  `dim_sex` tinyint(4) DEFAULT NULL COMMENT '性别(0=女,1=男,2=未知)',
  `did` varchar(100) DEFAULT NULL COMMENT '用户设备ID',
  `game_version` varchar(50) DEFAULT NULL COMMENT '游戏版本号',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间,索引字段',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_sale`
--

DROP TABLE IF EXISTS `tbllog_sale`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_sale` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `sales_id` int(11) DEFAULT NULL COMMENT '寄卖流水号',
  `role_id` bigint(11) DEFAULT NULL COMMENT '寄卖品角色ID',
  `item_id` int(11) DEFAULT NULL COMMENT '寄卖品ID',
  `price_type` int(11) DEFAULT NULL COMMENT '寄卖品价格货币类型（铜钱/元宝...）(1=元宝，2=绑定金币，3=铜币，4=绑定铜币)',
  `price_unit` bigint(20) DEFAULT NULL COMMENT '寄卖品价格',
  `item_number` bigint(20) DEFAULT NULL COMMENT '寄卖品物品数量',
  `action_id` int(11) DEFAULT NULL COMMENT '寄卖操作行为ID',
  `happend_time` int(11) DEFAULT NULL COMMENT '寄卖行为记录时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_shop`
--

DROP TABLE IF EXISTS `tbllog_shop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_shop` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `shopId` int(11) DEFAULT NULL COMMENT '商城类型ID',
  `dim_level` int(11) DEFAULT NULL COMMENT '玩家等级',
  `dim_prof` int(11) DEFAULT NULL COMMENT '职业ID',
  `money_type` int(11) DEFAULT NULL COMMENT '货币类型（1=金币，2=绑定金币，3=铜币，4=绑定铜币，5=礼券，6=积分/荣誉, 7=兑换）',
  `amount` int(11) DEFAULT NULL COMMENT '货币数量',
  `item_type_1` int(11) DEFAULT NULL COMMENT '物品分类1',
  `item_type_2` int(11) DEFAULT NULL COMMENT '物品分类2',
  `item_id` int(11) DEFAULT NULL COMMENT '物品ID',
  `item_number` int(11) DEFAULT NULL COMMENT '物品数量',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间，索引字段',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_skill`
--

DROP TABLE IF EXISTS `tbllog_skill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_skill` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `skill_id` int(11) DEFAULT NULL COMMENT '技能ID（对应 dict_action.action_id）',
  `used_num` int(11) DEFAULT NULL COMMENT '技能使用次数',
  `dim_level` int(11) DEFAULT NULL COMMENT '玩家等级',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间，索引字段',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_task`
--

DROP TABLE IF EXISTS `tbllog_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_task` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `dim_prof` int(11) DEFAULT NULL COMMENT '职业ID',
  `dim_level` int(11) DEFAULT NULL COMMENT '玩家等级',
  `task_id` int(11) DEFAULT NULL COMMENT '任务ID',
  `status` int(11) DEFAULT NULL COMMENT '状态（1=接任务，2=完成任务；3=取消任务(退出任务)，4=提交任务）',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间，索引字段',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_user_friend`
--

DROP TABLE IF EXISTS `tbllog_user_friend`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_user_friend` (
  `opt_role_id` bigint(20) DEFAULT NULL COMMENT '发起动作的角色ID',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `opt` tinyint(2) DEFAULT NULL COMMENT '操作类型（1-申请，2-接受好友）',
  `opt_role_friend_number` int(11) DEFAULT NULL COMMENT '发起动作的角色的好友数量',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_user_item`
--

DROP TABLE IF EXISTS `tbllog_user_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_user_item` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `role_name` varchar(50) DEFAULT NULL COMMENT '角色名',
  `item_id` bigint(20) DEFAULT NULL COMMENT '道具ID',
  `is_bind` int(11) DEFAULT NULL COMMENT '是否绑定，可以忽略',
  `strengthen_level` int(11) DEFAULT NULL COMMENT '强化等级，可以忽略',
  `item_amount` bigint(20) DEFAULT NULL COMMENT '道具数量',
  `item_position` bigint(20) DEFAULT NULL COMMENT '道具位置',
  `happend_time` int(11) DEFAULT NULL COMMENT '记录时间',
  `state` int(11) DEFAULT NULL COMMENT '道具状态，可忽略',
  `bag_type` int(11) DEFAULT NULL COMMENT '背包类型，与item_position共同指定位置（bag_type指定道具在背包，银行或者身上等，而item_position指定所在的具体坐标）,可忽略',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_user_team`
--

DROP TABLE IF EXISTS `tbllog_user_team`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_user_team` (
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `team_id` bigint(20) DEFAULT NULL COMMENT '组队ID',
  `team_type_id` int(11) DEFAULT NULL COMMENT '组队类型ID（提供字典表）',
  `leader_role_id` bigint(20) DEFAULT NULL COMMENT '队长角色ID',
  `opt` tinyint(4) DEFAULT NULL COMMENT '操作类型（1-组队[加入]，2-解散，3-退出）',
  `player_num` bigint(20) DEFAULT NULL COMMENT '队伍人数',
  `task_id` int(11) DEFAULT NULL COMMENT '组队对应的任务',
  `time_duration` int(11) DEFAULT NULL COMMENT '战斗时长（解散时记录）',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbllog_weal`
--

DROP TABLE IF EXISTS `tbllog_weal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbllog_weal` (
  `platform` varchar(100) DEFAULT NULL COMMENT '所属平台',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '平台唯一用户标识',
  `weal_type` int(11) DEFAULT NULL COMMENT '福利类型（登录/充值/手工等）',
  `item_id` int(11) DEFAULT NULL COMMENT '获得道具ID',
  `item_number` bigint(20) DEFAULT NULL COMMENT '获得道具数量',
  `money_type` int(11) DEFAULT NULL COMMENT '获得货币类型',
  `amount` bigint(20) DEFAULT NULL COMMENT '获得货币数量',
  `source_data` varchar(300) DEFAULT NULL COMMENT '在一次获得福利时获得多个物品时，可以通过json的格式记录在该字段中，而此时，item_id与item_number留空',
  `happend_time` int(11) DEFAULT NULL COMMENT '事件发生时间',
  `log_time` int(11) DEFAULT NULL COMMENT '写日志时间,索引字段',
  KEY `index_name` (`happend_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'log_android_cn_s9992'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-02-29 20:26:39
