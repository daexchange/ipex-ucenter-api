/*
SQLyog Ultimate v12.08 (64 bit)
MySQL - 5.7.18 : Database - exchange
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`exchange` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_bin */;

USE `exchange`;

/*Table structure for table `admin` */

DROP TABLE IF EXISTS `admin`;

CREATE TABLE `admin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `avatar` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `enable` int(11) DEFAULT NULL,
  `last_login_ip` varchar(255) DEFAULT NULL,
  `last_login_time` datetime DEFAULT NULL,
  `mobile_phone` varchar(255) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `qq` varchar(255) DEFAULT NULL,
  `real_name` varchar(255) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  `status` int(11) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  `department_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_gfn44sntic2k93auag97juyij` (`username`) USING BTREE,
  KEY `FKibjnyhe6m46qfkc6vgbir1ucq` (`department_id`) USING BTREE,
  CONSTRAINT `FKnmmt6f2kg0oaxr11uhy7qqf3w` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`),
  CONSTRAINT `admin_ibfk_1` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`),
  CONSTRAINT `admin_ibfk_2` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`),
  CONSTRAINT `admin_ibfk_3` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

/*Table structure for table `admin_access_log` */

DROP TABLE IF EXISTS `admin_access_log`;

CREATE TABLE `admin_access_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `access_ip` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `access_method` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `access_time` datetime DEFAULT NULL,
  `admin_id` bigint(20) DEFAULT NULL,
  `module` int(11) DEFAULT NULL,
  `operation` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `uri` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7632 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `admin_permission` */

DROP TABLE IF EXISTS `admin_permission`;

CREATE TABLE `admin_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `sort` int(11) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=190 DEFAULT CHARSET=utf8;

/*Table structure for table `admin_role` */

DROP TABLE IF EXISTS `admin_role`;

CREATE TABLE `admin_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `role` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=87 DEFAULT CHARSET=utf8;

/*Table structure for table `admin_role_permission` */

DROP TABLE IF EXISTS `admin_role_permission`;

CREATE TABLE `admin_role_permission` (
  `role_id` bigint(20) NOT NULL,
  `rule_id` bigint(20) NOT NULL,
  UNIQUE KEY `UKplesprlvm1sob8nl9yc5rgh3m` (`role_id`,`rule_id`),
  KEY `FK52rddd3qje4p49iubt08gplb5` (`role_id`) USING BTREE,
  KEY `FKqf3fhgl5mjqqb0jeupx7yafh0` (`rule_id`) USING BTREE,
  CONSTRAINT `FK52rddd3qje4p49iubt08gplb5` FOREIGN KEY (`role_id`) REFERENCES `admin_role` (`id`),
  CONSTRAINT `FKqf3fhgl5mjqqb0jeupx7yafh0` FOREIGN KEY (`rule_id`) REFERENCES `admin_permission` (`id`),
  CONSTRAINT `admin_role_permission_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `admin_role` (`id`),
  CONSTRAINT `admin_role_permission_ibfk_2` FOREIGN KEY (`rule_id`) REFERENCES `admin_permission` (`id`),
  CONSTRAINT `admin_role_permission_ibfk_3` FOREIGN KEY (`role_id`) REFERENCES `admin_role` (`id`),
  CONSTRAINT `admin_role_permission_ibfk_4` FOREIGN KEY (`rule_id`) REFERENCES `admin_permission` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `advertise` */

DROP TABLE IF EXISTS `advertise`;

CREATE TABLE `advertise` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `advertise_type` int(11) NOT NULL,
  `auto` int(11) DEFAULT NULL,
  `autoword` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `coin_unit` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `deal_amount` decimal(18,8) DEFAULT NULL COMMENT '交易中数量',
  `level` int(11) DEFAULT NULL,
  `limit_money` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `max_limit` decimal(18,2) DEFAULT NULL COMMENT '最高单笔交易额',
  `min_limit` decimal(18,2) DEFAULT NULL COMMENT '最低单笔交易额',
  `number` decimal(18,8) DEFAULT NULL COMMENT '计划数量',
  `pay_mode` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `premise_rate` decimal(18,6) DEFAULT NULL COMMENT '溢价百分比',
  `price` decimal(18,2) DEFAULT NULL COMMENT '交易价格',
  `price_type` int(11) NOT NULL,
  `remain_amount` decimal(18,8) DEFAULT NULL COMMENT '计划剩余数量',
  `remark` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `time_limit` int(11) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `username` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `coin_id` bigint(20) NOT NULL,
  `country` varchar(255) COLLATE utf8_bin NOT NULL,
  `member_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK75rse9iecdnimf8ugtf20c43l` (`coin_id`),
  CONSTRAINT `FK75rse9iecdnimf8ugtf20c43l` FOREIGN KEY (`coin_id`) REFERENCES `otc_coin` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `announcement` */

DROP TABLE IF EXISTS `announcement`;

CREATE TABLE `announcement` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` text COLLATE utf8_bin,
  `create_time` datetime DEFAULT NULL,
  `img_url` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `is_show` bit(1) DEFAULT NULL,
  `is_top` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `sort` int(11) NOT NULL,
  `title` varchar(255) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `app_revision` */

DROP TABLE IF EXISTS `app_revision`;

CREATE TABLE `app_revision` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `download_url` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `platform` int(11) DEFAULT NULL,
  `publish_time` datetime DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `version` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `appeal` */

DROP TABLE IF EXISTS `appeal`;

CREATE TABLE `appeal` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `associate_id` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `deal_with_time` datetime DEFAULT NULL,
  `initiator_id` bigint(20) DEFAULT NULL,
  `is_success` int(11) DEFAULT NULL,
  `remark` varchar(500) COLLATE utf8_bin DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `admin_id` bigint(20) DEFAULT NULL,
  `order_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_todwxorutclquf69bwow70kml` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `business_auth_apply` */

DROP TABLE IF EXISTS `business_auth_apply`;

CREATE TABLE `business_auth_apply` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(19,2) DEFAULT NULL,
  `auditing_time` datetime DEFAULT NULL,
  `auth_info` text COLLATE utf8_bin,
  `certified_business_status` int(11) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `deposit_record_id` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `detail` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `business_auth_deposit_id` bigint(20) DEFAULT NULL,
  `member_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `business_auth_deposit` */

DROP TABLE IF EXISTS `business_auth_deposit`;

CREATE TABLE `business_auth_deposit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(18,8) DEFAULT NULL COMMENT '保证金数额',
  `create_time` datetime DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `admin_id` bigint(20) DEFAULT NULL,
  `coin_id` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `bussiness_cancel_apply` */

DROP TABLE IF EXISTS `bussiness_cancel_apply`;

CREATE TABLE `bussiness_cancel_apply` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cancel_apply_time` datetime DEFAULT NULL,
  `deposit_record_id` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `detail` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `handle_time` datetime DEFAULT NULL,
  `reason` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `member_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `coin` */

DROP TABLE IF EXISTS `coin`;

CREATE TABLE `coin` (
  `name` varchar(255) NOT NULL,
  `can_auto_withdraw` int(11) DEFAULT NULL,
  `can_recharge` int(11) DEFAULT NULL,
  `can_transfer` int(11) DEFAULT NULL,
  `can_withdraw` int(11) DEFAULT NULL,
  `cny_rate` double NOT NULL,
  `enable_rpc` int(11) DEFAULT NULL,
  `is_platform_coin` int(11) DEFAULT NULL,
  `max_tx_fee` double NOT NULL,
  `max_withdraw_amount` decimal(18,8) DEFAULT NULL COMMENT '最大提币数量',
  `min_tx_fee` double NOT NULL,
  `min_withdraw_amount` decimal(18,8) DEFAULT NULL COMMENT '最小提币数量',
  `name_cn` varchar(255) NOT NULL,
  `sort` int(11) NOT NULL,
  `status` int(11) DEFAULT NULL,
  `unit` varchar(255) NOT NULL,
  `usd_rate` double NOT NULL,
  `withdraw_threshold` decimal(18,8) DEFAULT NULL COMMENT '提现阈值',
  `has_legal` bit(1) NOT NULL DEFAULT b'0',
  `cold_wallet_address` varchar(255) DEFAULT NULL,
  `miner_fee` decimal(18,8) DEFAULT '0.00000000' COMMENT '矿工费',
  `withdraw_scale` int(11) DEFAULT '4' COMMENT '提币精度',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `country` */

DROP TABLE IF EXISTS `country`;

CREATE TABLE `country` (
  `zh_name` varchar(255) NOT NULL,
  `area_code` varchar(255) DEFAULT NULL,
  `en_name` varchar(255) DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `local_currency` varchar(255) DEFAULT NULL,
  `sort` int(11) NOT NULL,
  PRIMARY KEY (`zh_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `data_dictionary` */

DROP TABLE IF EXISTS `data_dictionary`;

CREATE TABLE `data_dictionary` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bond` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `comment` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `creation_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `value` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `department` */

DROP TABLE IF EXISTS `department`;

CREATE TABLE `department` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime DEFAULT NULL,
  `leader_id` bigint(20) DEFAULT NULL,
  `name` varchar(255) COLLATE utf8_bin NOT NULL,
  `remark` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_1t68827l97cwyxo9r1u6t4p7d` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `deposit_record` */

DROP TABLE IF EXISTS `deposit_record`;

CREATE TABLE `deposit_record` (
  `id` varchar(255) COLLATE utf8_bin NOT NULL,
  `amount` decimal(19,2) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `coin_id` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `member_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `dividend_start_record` */

DROP TABLE IF EXISTS `dividend_start_record`;

CREATE TABLE `dividend_start_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(18,6) DEFAULT NULL COMMENT '数量',
  `date` datetime DEFAULT NULL,
  `end` bigint(20) DEFAULT NULL,
  `end_date` datetime DEFAULT NULL,
  `rate` decimal(18,2) DEFAULT NULL COMMENT '比例',
  `start` bigint(20) DEFAULT NULL,
  `start_date` datetime DEFAULT NULL,
  `unit` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `admin_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `exchange_coin` */

DROP TABLE IF EXISTS `exchange_coin`;

CREATE TABLE `exchange_coin` (
  `symbol` varchar(255) NOT NULL,
  `base_coin_scale` int(11) NOT NULL,
  `base_symbol` varchar(255) DEFAULT NULL,
  `coin_scale` int(11) NOT NULL,
  `coin_symbol` varchar(255) DEFAULT NULL,
  `enable` int(11) NOT NULL,
  `fee` decimal(8,4) DEFAULT NULL COMMENT '交易手续费',
  `sort` int(11) NOT NULL,
  `enable_market_sell` int(11) DEFAULT '1' COMMENT '是否启用市价卖',
  `enable_market_buy` int(11) DEFAULT '1' COMMENT '是否启用市价买',
  `min_sell_price` decimal(18,8) DEFAULT '0.00000000' COMMENT '最低挂单卖价',
  `flag` int(11) DEFAULT '0',
  `max_trading_order` int(11) DEFAULT '0' COMMENT '最大允许同时交易的订单数，0表示不限制',
  `max_trading_time` int(11) DEFAULT '0' COMMENT '委托超时自动下架时间，单位为秒，0表示不过期',
  `instrument` varchar(20) DEFAULT NULL COMMENT '交易类型，B2C2特有',
  `min_turnover` decimal(18,8) NOT NULL DEFAULT '0.00000000' COMMENT '最小挂单成交额',
  `max_volume` decimal(18,8) DEFAULT '0.00000000' COMMENT '最大下单量',
  `min_volume` decimal(18,8) DEFAULT '0.00000000' COMMENT '最小下单量',
  `zone` int(11) DEFAULT '0',
  PRIMARY KEY (`symbol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `exchange_coin_settlement` */

DROP TABLE IF EXISTS `exchange_coin_settlement`;

CREATE TABLE `exchange_coin_settlement` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `symbol` varchar(255) NOT NULL,
  `enable` int(11) NOT NULL,
  `sort` int(11) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

/*Table structure for table `exchange_favor_symbol` */

DROP TABLE IF EXISTS `exchange_favor_symbol`;

CREATE TABLE `exchange_favor_symbol` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `add_time` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `member_id` bigint(20) DEFAULT NULL,
  `symbol` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=122 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `exchange_order` */

DROP TABLE IF EXISTS `exchange_order`;

CREATE TABLE `exchange_order` (
  `order_id` varchar(255) COLLATE utf8_bin NOT NULL,
  `amount` decimal(18,8) DEFAULT '0.00000000',
  `base_symbol` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `canceled_time` bigint(20) DEFAULT NULL,
  `coin_symbol` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `completed_time` bigint(20) DEFAULT NULL,
  `direction` int(11) DEFAULT NULL,
  `member_id` bigint(20) DEFAULT NULL,
  `price` decimal(18,8) DEFAULT '0.00000000',
  `status` int(11) DEFAULT NULL,
  `symbol` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `time` bigint(20) DEFAULT NULL,
  `traded_amount` decimal(26,16) DEFAULT '0.0000000000000000',
  `turnover` decimal(26,16) DEFAULT '0.0000000000000000',
  `type` int(11) DEFAULT NULL,
  `use_discount` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `exchange_order2` */

DROP TABLE IF EXISTS `exchange_order2`;

CREATE TABLE `exchange_order2` (
  `order_id` varchar(255) COLLATE utf8_bin NOT NULL,
  `amount` decimal(18,8) DEFAULT '0.00000000',
  `base_symbol` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `canceled_time` bigint(20) DEFAULT NULL,
  `coin_symbol` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `completed_time` bigint(20) DEFAULT NULL,
  `direction` int(11) DEFAULT NULL,
  `member_id` bigint(20) DEFAULT NULL,
  `price` decimal(18,8) DEFAULT '0.00000000',
  `status` int(11) DEFAULT NULL,
  `symbol` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `time` bigint(20) DEFAULT NULL,
  `traded_amount` decimal(26,16) DEFAULT '0.0000000000000000',
  `turnover` decimal(26,16) DEFAULT '0.0000000000000000',
  `type` int(11) DEFAULT NULL,
  `use_discount` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `feedback` */

DROP TABLE IF EXISTS `feedback`;

CREATE TABLE `feedback` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `member_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `financial_item` */

DROP TABLE IF EXISTS `financial_item`;

CREATE TABLE `financial_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `coin_minnum` decimal(19,2) DEFAULT NULL,
  `coin_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `deadline` int(11) NOT NULL,
  `item_desc` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `item_id` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `item_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `item_state` int(11) NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `yield` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `financial_order` */

DROP TABLE IF EXISTS `financial_order`;

CREATE TABLE `financial_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `coin_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `coin_num` decimal(19,2) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `frozen_days` int(11) NOT NULL,
  `item_id` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `member_id` bigint(20) DEFAULT NULL,
  `order_no` bigint(20) DEFAULT NULL,
  `order_state` int(11) NOT NULL,
  `order_usdt_rate` double DEFAULT NULL,
  `plan_revenue_time` datetime DEFAULT NULL,
  `real_income` decimal(19,2) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `hot_transfer_record` */

DROP TABLE IF EXISTS `hot_transfer_record`;

CREATE TABLE `hot_transfer_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `admin_id` bigint(20) DEFAULT NULL,
  `amount` decimal(18,8) DEFAULT '0.00000000' COMMENT '转账金额',
  `balance` decimal(18,8) DEFAULT '0.00000000' COMMENT '热钱包余额',
  `cold_address` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `miner_fee` decimal(18,8) DEFAULT '0.00000000' COMMENT '矿工费',
  `transaction_number` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `transfer_time` datetime DEFAULT NULL,
  `unit` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `init_plate` */

DROP TABLE IF EXISTS `init_plate`;

CREATE TABLE `init_plate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `final_price` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `init_price` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `interference_factor` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `relative_time` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `symbol` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `legal_wallet_recharge` */

DROP TABLE IF EXISTS `legal_wallet_recharge`;

CREATE TABLE `legal_wallet_recharge` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(18,2) NOT NULL COMMENT '充值金额',
  `creation_time` datetime DEFAULT NULL,
  `deal_time` datetime DEFAULT NULL,
  `pay_mode` int(11) NOT NULL,
  `payment_instrument` varchar(255) COLLATE utf8_bin NOT NULL,
  `remark` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `state` int(11) NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `admin_id` bigint(20) DEFAULT NULL,
  `coin_name` varchar(255) COLLATE utf8_bin NOT NULL,
  `member_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `legal_wallet_withdraw` */

DROP TABLE IF EXISTS `legal_wallet_withdraw`;

CREATE TABLE `legal_wallet_withdraw` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(18,8) DEFAULT NULL COMMENT '申请总数量',
  `create_time` datetime DEFAULT NULL,
  `deal_time` datetime DEFAULT NULL,
  `pay_mode` int(11) NOT NULL,
  `payment_instrument` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `remit_time` datetime DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `admin_id` bigint(20) DEFAULT NULL,
  `coin_name` varchar(255) COLLATE utf8_bin NOT NULL,
  `member_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `member` */

DROP TABLE IF EXISTS `member`;

CREATE TABLE `member` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ali_no` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `qr_code_url` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `appeal_success_times` int(11) DEFAULT NULL,
  `appeal_times` int(11) DEFAULT NULL,
  `application_time` datetime DEFAULT NULL,
  `avatar` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `bank` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `branch` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `card_no` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `certified_business_apply_time` datetime DEFAULT NULL,
  `certified_business_check_time` datetime DEFAULT NULL,
  `certified_business_status` int(11) DEFAULT NULL,
  `email` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `first_level` int(11) NOT NULL,
  `google_date` datetime DEFAULT NULL,
  `google_key` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `google_state` int(11) DEFAULT NULL,
  `id_number` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `inviter_id` bigint(20) DEFAULT NULL,
  `jy_password` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `last_login_time` datetime DEFAULT NULL,
  `city` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `country` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `district` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `province` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `login_count` int(11) DEFAULT NULL,
  `margin` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `member_level` int(11) DEFAULT NULL,
  `mobile_phone` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `password` varchar(255) COLLATE utf8_bin NOT NULL,
  `promotion_code` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `publish_advertise` int(11) DEFAULT NULL,
  `real_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `real_name_status` int(11) DEFAULT NULL,
  `registration_time` datetime DEFAULT NULL,
  `salt` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `second_level` int(11) NOT NULL,
  `sign_in_ability` bit(1) NOT NULL DEFAULT b'1',
  `status` int(11) DEFAULT NULL,
  `super_partner` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `third_level` int(11) NOT NULL,
  `token` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `token_expire_time` datetime DEFAULT NULL,
  `transaction_status` int(11) DEFAULT NULL,
  `transactions` int(11) NOT NULL,
  `username` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `qr_we_code_url` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `wechat` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `local` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `origin` tinyint(1) DEFAULT NULL COMMENT '1代表IPcon手机端',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_gc3jmn7c2abyo3wf6syln5t2i` (`username`),
  UNIQUE KEY `UK_mbmcqelty0fbrvxp1q58dn57t` (`email`),
  UNIQUE KEY `UK_10ixebfiyeqolglpuye0qb49u` (`mobile_phone`)
) ENGINE=InnoDB AUTO_INCREMENT=110 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `member_address` */

DROP TABLE IF EXISTS `member_address`;

CREATE TABLE `member_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `delete_time` datetime DEFAULT NULL,
  `member_id` bigint(20) DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `coin_id` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `member_application` */

DROP TABLE IF EXISTS `member_application`;

CREATE TABLE `member_application` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `audit_status` int(11) NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `id_card` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `identity_card_img_front` varchar(255) COLLATE utf8_bin NOT NULL,
  `identity_card_img_in_hand` varchar(255) COLLATE utf8_bin NOT NULL,
  `identity_card_img_reverse` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `real_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `reject_reason` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `member_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `member_application_config` */

DROP TABLE IF EXISTS `member_application_config`;

CREATE TABLE `member_application_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `promotion_on` int(11) DEFAULT NULL,
  `recharge_coin_on` int(11) DEFAULT NULL,
  `transaction_on` int(11) DEFAULT NULL,
  `withdraw_coin_on` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `member_bonus` */

DROP TABLE IF EXISTS `member_bonus`;

CREATE TABLE `member_bonus` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `arrive_time` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `coin_id` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `have_time` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `mem_bouns` decimal(18,8) DEFAULT NULL COMMENT '分红数量',
  `member_id` bigint(20) DEFAULT NULL,
  `total` decimal(18,8) DEFAULT NULL COMMENT '当天总手续费',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `member_deposit` */

DROP TABLE IF EXISTS `member_deposit`;

CREATE TABLE `member_deposit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `amount` decimal(18,8) DEFAULT '0.00000000',
  `create_time` datetime DEFAULT NULL,
  `member_id` bigint(20) DEFAULT NULL,
  `txid` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `unit` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKl2ibi99fuxplt8qt3rrpb0q4w` (`txid`,`address`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `member_exclusive_fee` */

DROP TABLE IF EXISTS `member_exclusive_fee`;

CREATE TABLE `member_exclusive_fee` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `member_id` bigint(20) NOT NULL COMMENT '会员ID',
  `symbol` varchar(32) NOT NULL COMMENT '币种',
  `fee` decimal(18,0) NOT NULL COMMENT '费率',
  `type` int(1) NOT NULL COMMENT '交易类型',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `INDEX_EXCLUSIVE_FEE` (`id`) USING HASH,
  UNIQUE KEY `INDEX_member_id` (`member_id`,`symbol`) USING HASH
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `member_legal_currency_wallet` */

DROP TABLE IF EXISTS `member_legal_currency_wallet`;

CREATE TABLE `member_legal_currency_wallet` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `balance` decimal(26,16) DEFAULT NULL COMMENT '可用余额',
  `frozen_balance` decimal(26,16) DEFAULT NULL COMMENT '冻结余额',
  `is_lock` int(11) DEFAULT '0' COMMENT '钱包不是锁定',
  `member_id` bigint(20) DEFAULT NULL,
  `to_released` decimal(18,8) DEFAULT NULL COMMENT '待释放总量',
  `version` int(11) DEFAULT NULL,
  `coin_id` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKm68bscpof0bpnxocxl4qdnvbe` (`member_id`,`coin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `member_level` */

DROP TABLE IF EXISTS `member_level`;

CREATE TABLE `member_level` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `is_default` bit(1) NOT NULL,
  `name` varchar(255) COLLATE utf8_bin NOT NULL,
  `remark` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `member_level_fee` */

DROP TABLE IF EXISTS `member_level_fee`;

CREATE TABLE `member_level_fee` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `symbol` varchar(32) NOT NULL COMMENT '币种',
  `member_level_id` bigint(20) NOT NULL COMMENT '会员等级表ID',
  `fee` decimal(18,0) NOT NULL COMMENT '费率',
  `type` int(1) NOT NULL COMMENT '交易类型',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `INDEX_LEVEL_FEE_ID` (`id`) USING HASH,
  UNIQUE KEY `INDEX_LEVEL_FEE_member_level_id` (`symbol`,`member_level_id`) USING HASH
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `member_promotion` */

DROP TABLE IF EXISTS `member_promotion`;

CREATE TABLE `member_promotion` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `invitees_id` bigint(20) DEFAULT NULL,
  `inviter_id` bigint(20) DEFAULT NULL,
  `level` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `member_sign_record` */

DROP TABLE IF EXISTS `member_sign_record`;

CREATE TABLE `member_sign_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(19,2) DEFAULT NULL,
  `creation_time` datetime DEFAULT NULL,
  `coin_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `member_id` bigint(20) DEFAULT NULL,
  `sign_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `member_transaction` */

DROP TABLE IF EXISTS `member_transaction`;

CREATE TABLE `member_transaction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `amount` decimal(26,16) DEFAULT NULL COMMENT '充币金额',
  `create_time` datetime DEFAULT NULL,
  `discount_fee` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `fee` decimal(26,16) DEFAULT NULL,
  `flag` int(11) NOT NULL DEFAULT '0',
  `member_id` bigint(20) DEFAULT NULL,
  `real_fee` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `symbol` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=759 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `member_wallet` */

DROP TABLE IF EXISTS `member_wallet`;

CREATE TABLE `member_wallet` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `balance` decimal(26,16) DEFAULT NULL COMMENT '可用余额',
  `frozen_balance` decimal(26,16) DEFAULT NULL COMMENT '冻结余额',
  `is_lock` int(11) DEFAULT '0' COMMENT '钱包不是锁定',
  `member_id` bigint(20) DEFAULT NULL,
  `to_released` decimal(18,8) DEFAULT NULL COMMENT '待释放总量',
  `version` int(11) NOT NULL,
  `coin_id` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKm68bscpof0bpnxocxl4qdnvbe` (`member_id`,`coin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4284 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `otc_coin` */

DROP TABLE IF EXISTS `otc_coin`;

CREATE TABLE `otc_coin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `buy_min_amount` decimal(18,8) DEFAULT NULL COMMENT '买入广告最低发布数量',
  `is_platform_coin` int(11) DEFAULT NULL,
  `jy_rate` decimal(18,6) DEFAULT NULL COMMENT '交易手续费率',
  `name` varchar(255) NOT NULL,
  `name_cn` varchar(255) NOT NULL,
  `sell_min_amount` decimal(18,8) DEFAULT NULL COMMENT '卖出广告最低发布数量',
  `sort` int(11) NOT NULL,
  `status` int(11) DEFAULT NULL,
  `unit` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

/*Table structure for table `otc_order` */

DROP TABLE IF EXISTS `otc_order`;

CREATE TABLE `otc_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `advertise_id` bigint(20) NOT NULL,
  `advertise_type` int(11) NOT NULL,
  `ali_no` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `qr_code_url` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `bank` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `branch` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `card_no` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `cancel_time` datetime DEFAULT NULL,
  `commission` decimal(18,8) DEFAULT NULL COMMENT '手续费',
  `country` varchar(255) COLLATE utf8_bin NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `customer_id` bigint(20) NOT NULL,
  `customer_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `customer_real_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `max_limit` decimal(18,2) DEFAULT NULL COMMENT '最高交易额',
  `member_id` bigint(20) NOT NULL,
  `member_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `member_real_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `min_limit` decimal(18,2) DEFAULT NULL COMMENT '最低交易额',
  `money` decimal(18,2) DEFAULT NULL COMMENT '交易金额',
  `number` decimal(18,8) DEFAULT NULL COMMENT '交易数量',
  `order_sn` varchar(255) COLLATE utf8_bin NOT NULL,
  `pay_mode` varchar(255) COLLATE utf8_bin NOT NULL,
  `pay_time` datetime DEFAULT NULL,
  `price` decimal(18,2) DEFAULT NULL COMMENT '价格',
  `release_time` datetime DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `time_limit` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `qr_we_code_url` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `wechat` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `coin_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_qmfpakgu6mowmslv4m5iy43t9` (`order_sn`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `reward_activity_setting` */

DROP TABLE IF EXISTS `reward_activity_setting`;

CREATE TABLE `reward_activity_setting` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `info` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `admin_id` bigint(20) DEFAULT NULL,
  `coin_id` varchar(255) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `reward_promotion_setting` */

DROP TABLE IF EXISTS `reward_promotion_setting`;

CREATE TABLE `reward_promotion_setting` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `effective_time` int(11) NOT NULL,
  `info` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `admin_id` bigint(20) DEFAULT NULL,
  `coin_id` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `reward_record` */

DROP TABLE IF EXISTS `reward_record`;

CREATE TABLE `reward_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(18,8) DEFAULT NULL COMMENT '数目',
  `create_time` datetime DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `coin_id` varchar(255) COLLATE utf8_bin NOT NULL,
  `member_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `sign` */

DROP TABLE IF EXISTS `sign`;

CREATE TABLE `sign` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(19,2) DEFAULT NULL,
  `creation_time` datetime DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `coin_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `sys_advertise` */

DROP TABLE IF EXISTS `sys_advertise`;

CREATE TABLE `sys_advertise` (
  `serial_number` varchar(255) COLLATE utf8_bin NOT NULL,
  `author` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `content` text COLLATE utf8_bin,
  `create_time` datetime DEFAULT NULL,
  `end_time` varchar(255) COLLATE utf8_bin NOT NULL,
  `link_url` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `name` varchar(255) COLLATE utf8_bin NOT NULL,
  `remark` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `sort` int(11) NOT NULL,
  `start_time` varchar(255) COLLATE utf8_bin NOT NULL,
  `status` int(11) NOT NULL,
  `sys_advertise_location` int(11) NOT NULL,
  `url` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`serial_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `sys_help` */

DROP TABLE IF EXISTS `sys_help`;

CREATE TABLE `sys_help` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `author` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `content` text COLLATE utf8_bin,
  `create_time` datetime DEFAULT NULL,
  `img_url` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `is_top` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `sort` int(11) NOT NULL,
  `status` int(11) NOT NULL,
  `sys_help_classification` int(11) NOT NULL,
  `title` varchar(255) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `tb_sms` */

DROP TABLE IF EXISTS `tb_sms`;

CREATE TABLE `tb_sms` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `key_id` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `key_secret` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `sign_id` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `sms_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `sms_status` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `template_id` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `transfer_address` */

DROP TABLE IF EXISTS `transfer_address`;

CREATE TABLE `transfer_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `min_amount` decimal(18,2) DEFAULT NULL COMMENT '最低转账数目',
  `status` int(11) DEFAULT NULL,
  `transfer_fee` decimal(18,6) DEFAULT NULL COMMENT '转账手续费率',
  `coin_id` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `transfer_other_record` */

DROP TABLE IF EXISTS `transfer_other_record`;

CREATE TABLE `transfer_other_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `member_id_from` bigint(20) DEFAULT NULL COMMENT '交易人员ID',
  `member_id_to` bigint(20) DEFAULT NULL COMMENT '交易人员ID',
  `wallet_id_from` bigint(20) NOT NULL COMMENT '币币账户id',
  `wallet_id_to` bigint(20) NOT NULL COMMENT '法币账户ID',
  `coin_id` varchar(255) COLLATE utf8_bin NOT NULL COMMENT '币种ID',
  `total_amount` decimal(18,8) DEFAULT NULL COMMENT '数量',
  `fee` decimal(18,8) DEFAULT NULL COMMENT '手续费',
  `arrived_amount` decimal(18,8) DEFAULT NULL COMMENT '预计到账数量',
  `create_time` datetime DEFAULT NULL,
  `deal_time` datetime DEFAULT NULL,
  `status` int(11) DEFAULT NULL COMMENT 'type=0 失败 type=1 成功',
  `type` int(11) DEFAULT NULL COMMENT 'type=IpCOM手机端转账',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `transfer_record` */

DROP TABLE IF EXISTS `transfer_record`;

CREATE TABLE `transfer_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `amount` decimal(19,2) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `fee` decimal(18,8) DEFAULT NULL COMMENT '手续费',
  `member_id` bigint(20) DEFAULT NULL,
  `order_sn` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `coin_id` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `transfer_self_record` */

DROP TABLE IF EXISTS `transfer_self_record`;

CREATE TABLE `transfer_self_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `legalcurrency_id` bigint(20) NOT NULL COMMENT '法币账户ID',
  `wallet_id` bigint(20) NOT NULL COMMENT '币币账户id',
  `coin_id` varchar(255) COLLATE utf8_bin NOT NULL COMMENT '币种ID',
  `total_amount` decimal(18,8) DEFAULT NULL COMMENT '数量',
  `fee` decimal(18,8) DEFAULT NULL COMMENT '手续费',
  `arrived_amount` decimal(18,8) DEFAULT NULL COMMENT '预计到账数量',
  `member_id` bigint(20) DEFAULT NULL COMMENT '交易人员ID',
  `create_time` datetime DEFAULT NULL,
  `deal_time` datetime DEFAULT NULL,
  `type` int(11) DEFAULT NULL COMMENT 'type=0 转入法币账户 type=1 转出法币账户',
  `status` int(11) DEFAULT NULL COMMENT 'type=0 失败 type=1 成功',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `website_information` */

DROP TABLE IF EXISTS `website_information`;

CREATE TABLE `website_information` (
  `id` bigint(20) NOT NULL,
  `address_icon` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `contact` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `copyright` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `description` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `keywords` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `logo` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `other_information` text COLLATE utf8_bin,
  `postcode` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `url` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `withdraw_record` */

DROP TABLE IF EXISTS `withdraw_record`;

CREATE TABLE `withdraw_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `arrived_amount` decimal(18,8) DEFAULT NULL COMMENT '预计到账数量',
  `can_auto_withdraw` int(11) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `deal_time` datetime DEFAULT NULL,
  `fee` decimal(18,8) DEFAULT NULL COMMENT '手续费',
  `is_auto` int(11) DEFAULT NULL,
  `member_id` bigint(20) DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `total_amount` decimal(18,8) DEFAULT NULL COMMENT '申请总数量',
  `transaction_number` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `admin_id` bigint(20) DEFAULT NULL,
  `coin_id` varchar(255) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
