-- --------------------------------------------------------
-- Host:                         localhost
-- Server version:               8.0.19 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             10.3.0.5771
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


-- Dumping database structure for ece366restaurant
CREATE DATABASE IF NOT EXISTS `ece366restaurant` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `ece366restaurant`;

-- Dumping structure for table ece366restaurant.address
CREATE TABLE IF NOT EXISTS `address` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `latitude` decimal(10,8) NOT NULL,
  `longitude` decimal(10,8) NOT NULL,
  `address_line1` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `address_line2` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `city` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `state` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `zip_code` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `country` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table ece366restaurant.category
CREATE TABLE IF NOT EXISTS `category` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table ece366restaurant.contact
CREATE TABLE IF NOT EXISTS `contact` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `phone` int unsigned DEFAULT NULL,
  `email` varchar(320) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table ece366restaurant.reservation
CREATE TABLE IF NOT EXISTS `reservation` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `num_people` int unsigned NOT NULL,
  `num_points` int unsigned DEFAULT NULL,
  `restaurant_id` int unsigned NOT NULL,
  `status_id` int unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `reservation_restaurant_fk_idx` (`restaurant_id`),
  KEY `reservation_status_fk_idx` (`status_id`),
  CONSTRAINT `reservation_restaurant_fk` FOREIGN KEY (`restaurant_id`) REFERENCES `restaurant` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `reservation_status_fk` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table ece366restaurant.reservation_table
CREATE TABLE IF NOT EXISTS `reservation_table` (
  `table_id` int unsigned NOT NULL,
  `reservation_id` int unsigned NOT NULL,
  UNIQUE KEY `reservationtable_unique` (`table_id`,`reservation_id`),
  KEY `reservationtable_table_fk_idx` (`table_id`),
  KEY `reservationtable_reservation_fk_idx` (`reservation_id`),
  CONSTRAINT `reservationtable_reservation_fk` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `reservationtable_table_fk` FOREIGN KEY (`table_id`) REFERENCES `table` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table ece366restaurant.reservation_user
CREATE TABLE IF NOT EXISTS `reservation_user` (
  `reservation_id` int unsigned NOT NULL,
  `user_id` int unsigned NOT NULL,
  UNIQUE KEY `reservationuser_unique` (`reservation_id`,`user_id`),
  KEY `reservationuser_user_idx` (`user_id`),
  CONSTRAINT `reservationuser_reservation` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `reservationuser_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table ece366restaurant.restaurant
CREATE TABLE IF NOT EXISTS `restaurant` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `address_id` int unsigned NOT NULL,
  `contact_id` int unsigned NOT NULL,
  `category_id` int unsigned NOT NULL,
  `capacity_factor` int unsigned NOT NULL,
  `reservation_time` int unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `restaurant_contact_fk_idx` (`contact_id`),
  KEY `restaurant_category_fk_idx` (`category_id`),
  KEY `restaurant_address_fk_idx` (`address_id`),
  CONSTRAINT `restaurant_address_fk` FOREIGN KEY (`address_id`) REFERENCES `address` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `restaurant_category_fk` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `restaurant_contact_fk` FOREIGN KEY (`contact_id`) REFERENCES `contact` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table ece366restaurant.restaurant_user
CREATE TABLE IF NOT EXISTS `restaurant_user` (
  `restaurant_id` int unsigned NOT NULL,
  `user_id` int unsigned NOT NULL,
  `role_id` int unsigned NOT NULL,
  UNIQUE KEY `restaurantuser_unique` (`restaurant_id`,`user_id`),
  KEY `restaurantuser_restaurant_fk_idx` (`restaurant_id`),
  KEY `restaurantuser_user_fk_idx` (`user_id`),
  KEY `restaurantuser_role_fk_idx` (`role_id`),
  CONSTRAINT `restaurantuser_restaurant_fk` FOREIGN KEY (`restaurant_id`) REFERENCES `restaurant` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `restaurantuser_role_fk` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `restaurantuser_user_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table ece366restaurant.role
CREATE TABLE IF NOT EXISTS `role` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table ece366restaurant.status
CREATE TABLE IF NOT EXISTS `status` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table ece366restaurant.table
CREATE TABLE IF NOT EXISTS `table` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `label` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `capacity` tinyint unsigned NOT NULL,
  `restaurant_id` int unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `table_restaurant_fk_idx` (`restaurant_id`),
  CONSTRAINT `table_restaurant_fk` FOREIGN KEY (`restaurant_id`) REFERENCES `restaurant` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table ece366restaurant.user
CREATE TABLE IF NOT EXISTS `user` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(320) COLLATE utf8_unicode_ci NOT NULL,
  `password` varchar(60) COLLATE utf8_unicode_ci NOT NULL,
  `fname` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `lname` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `contact_id` int unsigned NOT NULL,
  `role_id` int unsigned NOT NULL,
  `rewards_points` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  UNIQUE KEY `contact_id_UNIQUE` (`contact_id`),
  KEY `user_role_fk_idx` (`role_id`),
  CONSTRAINT `user_contact_fk` FOREIGN KEY (`contact_id`) REFERENCES `contact` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `user_role_fk` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table ece366restaurant.user_login
CREATE TABLE IF NOT EXISTS `user_login` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int unsigned NOT NULL,
  `refresh_token` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `user_agent` varchar(1000) COLLATE utf8_unicode_ci DEFAULT NULL,
  `expiration_date` datetime NOT NULL,
  `revoked` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `user_login_fk_idx` (`user_id`),
  CONSTRAINT `user_login_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Data exporting was unselected.

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
