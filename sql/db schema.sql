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
  `line1` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `line2` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `city` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `state` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `zip` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `country` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Dumping data for table ece366restaurant.address: ~0 rows (approximately)
DELETE FROM `address`;
/*!40000 ALTER TABLE `address` DISABLE KEYS */;
/*!40000 ALTER TABLE `address` ENABLE KEYS */;

-- Dumping structure for table ece366restaurant.category
CREATE TABLE IF NOT EXISTS `category` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=123 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Dumping data for table ece366restaurant.category: ~122 rows (approximately)
DELETE FROM `category`;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` (`id`, `name`) VALUES
	(1, 'Afghan'),
	(2, 'African'),
	(3, 'American (New)'),
	(4, 'American (Traditional)'),
	(5, 'Arabian'),
	(6, 'Argentine'),
	(7, 'Armenian'),
	(8, 'Asian Fusion'),
	(9, 'Australian'),
	(10, 'Austrian'),
	(11, 'Bangladeshi'),
	(12, 'Barbeque'),
	(13, 'Basque'),
	(14, 'Belgian'),
	(15, 'Brasseries'),
	(16, 'Brazilian'),
	(17, 'Breakfast & Brunch'),
	(18, 'British'),
	(19, 'Buffets'),
	(20, 'Burgers'),
	(21, 'Burmese'),
	(22, 'Cafes'),
	(23, 'Cafeteria'),
	(24, 'Cajun/Creole'),
	(25, 'Cambodian'),
	(26, 'Caribbean'),
	(27, 'Catalan'),
	(28, 'Cheesesteaks'),
	(29, 'Chicken Shop'),
	(30, 'Chicken Wings'),
	(31, 'Chinese'),
	(32, 'Comfort Food'),
	(33, 'Creperies'),
	(34, 'Cuban'),
	(35, 'Czech'),
	(36, 'Delis'),
	(37, 'Diners'),
	(38, 'Dinner Theater'),
	(39, 'Ethiopian'),
	(40, 'Fast Food'),
	(41, 'Filipino'),
	(42, 'Fish & Chips'),
	(43, 'Fondue'),
	(44, 'Food Court'),
	(45, 'Food Stands'),
	(46, 'French'),
	(47, 'Game Meat'),
	(48, 'Gastropubs'),
	(49, 'German'),
	(50, 'Gluten-Free'),
	(51, 'Greek'),
	(52, 'Guamanian'),
	(53, 'Halal'),
	(54, 'Hawaiian'),
	(55, 'Himalayan/Nepalese'),
	(56, 'Honduran'),
	(57, 'Hong Kong Style Cafe'),
	(58, 'Hot Dogs'),
	(59, 'Hot Pot'),
	(60, 'Hungarian'),
	(61, 'Iberian'),
	(62, 'Indian'),
	(63, 'Indonesian'),
	(64, 'Irish'),
	(65, 'Italian'),
	(66, 'Japanese'),
	(67, 'Kebab'),
	(68, 'Korean'),
	(69, 'Kosher'),
	(70, 'Laotian'),
	(71, 'Latin American'),
	(72, 'Live/Raw Food'),
	(73, 'Malaysian'),
	(74, 'Mediterranean'),
	(75, 'Mexican'),
	(76, 'Middle Eastern'),
	(77, 'Modern European'),
	(78, 'Mongolian'),
	(79, 'Moroccan'),
	(80, 'New Mexican Cuisine'),
	(81, 'Nicaraguan'),
	(82, 'Noodles'),
	(83, 'Pakistani'),
	(84, 'Pan Asia'),
	(85, 'Persian/Iranian'),
	(86, 'Peruvian'),
	(87, 'Pizza'),
	(88, 'Polish'),
	(89, 'Polynesian'),
	(90, 'Pop-Up Restaurants'),
	(91, 'Portuguese'),
	(92, 'Poutineries'),
	(93, 'Russian'),
	(94, 'Salad'),
	(95, 'Sandwiches'),
	(96, 'Scandinavian'),
	(97, 'Scottish'),
	(98, 'Seafood'),
	(99, 'Singaporean'),
	(100, 'Slovakian'),
	(101, 'Soul Food'),
	(102, 'Soup'),
	(103, 'Southern'),
	(104, 'Spanish'),
	(105, 'Sri Lankan'),
	(106, 'Steakhouses'),
	(107, 'Supper Clubs'),
	(108, 'Sushi Bars'),
	(109, 'Syrian'),
	(110, 'Taiwanese'),
	(111, 'Tapas Bars'),
	(112, 'Tapas/Small Plates'),
	(113, 'Tex-Mex'),
	(114, 'Thai'),
	(115, 'Turkish'),
	(116, 'Ukrainian'),
	(117, 'Uzbek'),
	(118, 'Vegan'),
	(119, 'Vegetarian'),
	(120, 'Vietnamese'),
	(121, 'Waffles'),
	(122, 'Wraps');
/*!40000 ALTER TABLE `category` ENABLE KEYS */;

-- Dumping structure for table ece366restaurant.contact
CREATE TABLE IF NOT EXISTS `contact` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `phone` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `email` varchar(320) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Dumping data for table ece366restaurant.contact: ~0 rows (approximately)
DELETE FROM `contact`;
/*!40000 ALTER TABLE `contact` DISABLE KEYS */;
/*!40000 ALTER TABLE `contact` ENABLE KEYS */;

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

-- Dumping data for table ece366restaurant.reservation: ~0 rows (approximately)
DELETE FROM `reservation`;
/*!40000 ALTER TABLE `reservation` DISABLE KEYS */;
/*!40000 ALTER TABLE `reservation` ENABLE KEYS */;

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

-- Dumping data for table ece366restaurant.reservation_table: ~0 rows (approximately)
DELETE FROM `reservation_table`;
/*!40000 ALTER TABLE `reservation_table` DISABLE KEYS */;
/*!40000 ALTER TABLE `reservation_table` ENABLE KEYS */;

-- Dumping structure for table ece366restaurant.reservation_user
CREATE TABLE IF NOT EXISTS `reservation_user` (
  `reservation_id` int unsigned NOT NULL,
  `user_id` int unsigned NOT NULL,
  UNIQUE KEY `reservationuser_unique` (`reservation_id`,`user_id`),
  KEY `reservationuser_user_idx` (`user_id`),
  CONSTRAINT `reservationuser_reservation` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `reservationuser_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Dumping data for table ece366restaurant.reservation_user: ~0 rows (approximately)
DELETE FROM `reservation_user`;
/*!40000 ALTER TABLE `reservation_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `reservation_user` ENABLE KEYS */;

-- Dumping structure for table ece366restaurant.restaurant
CREATE TABLE IF NOT EXISTS `restaurant` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `address_id` int unsigned NOT NULL,
  `contact_id` int unsigned NOT NULL,
  `category_id` int unsigned NOT NULL,
  `capacity_factor` int unsigned NOT NULL DEFAULT(0),
  `reservation_time` int unsigned NOT NULL DEFAULT(2),
  PRIMARY KEY (`id`),
  KEY `restaurant_contact_fk_idx` (`contact_id`),
  KEY `restaurant_category_fk_idx` (`category_id`),
  KEY `restaurant_address_fk_idx` (`address_id`),
  CONSTRAINT `restaurant_address_fk` FOREIGN KEY (`address_id`) REFERENCES `address` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `restaurant_category_fk` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `restaurant_contact_fk` FOREIGN KEY (`contact_id`) REFERENCES `contact` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Dumping data for table ece366restaurant.restaurant: ~0 rows (approximately)
DELETE FROM `restaurant`;
/*!40000 ALTER TABLE `restaurant` DISABLE KEYS */;
/*!40000 ALTER TABLE `restaurant` ENABLE KEYS */;

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

-- Dumping data for table ece366restaurant.restaurant_user: ~0 rows (approximately)
DELETE FROM `restaurant_user`;
/*!40000 ALTER TABLE `restaurant_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `restaurant_user` ENABLE KEYS */;

-- Dumping structure for table ece366restaurant.role
CREATE TABLE IF NOT EXISTS `role` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Dumping data for table ece366restaurant.role: ~3 rows (approximately)
DELETE FROM `role`;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` (`id`, `name`) VALUES
	(2, 'Admin'),
	(1, 'Customer'),
	(3, 'Manager');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;

-- Dumping structure for table ece366restaurant.status
CREATE TABLE IF NOT EXISTS `status` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Dumping data for table ece366restaurant.status: ~2 rows (approximately)
DELETE FROM `status`;
/*!40000 ALTER TABLE `status` DISABLE KEYS */;
INSERT INTO `status` (`id`, `name`) VALUES
	(2, 'Cancelled'),
	(1, 'Opened');
/*!40000 ALTER TABLE `status` ENABLE KEYS */;

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

-- Dumping data for table ece366restaurant.table: ~0 rows (approximately)
DELETE FROM `table`;
/*!40000 ALTER TABLE `table` DISABLE KEYS */;
/*!40000 ALTER TABLE `table` ENABLE KEYS */;

-- Dumping structure for table ece366restaurant.user
CREATE TABLE IF NOT EXISTS `user` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(320) COLLATE utf8_unicode_ci NOT NULL,
  `password` varchar(200) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `fname` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `lname` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `contact_id` int unsigned NOT NULL,
  `role_id` int unsigned NOT NULL,
  `rewards_points` int NOT NULL DEFAULT(0),
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  UNIQUE KEY `contact_id_UNIQUE` (`contact_id`),
  KEY `user_role_fk_idx` (`role_id`),
  CONSTRAINT `user_contact_fk` FOREIGN KEY (`contact_id`) REFERENCES `contact` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `user_role_fk` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Dumping data for table ece366restaurant.user: ~0 rows (approximately)
DELETE FROM `user`;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
/*!40000 ALTER TABLE `user` ENABLE KEYS */;

-- Dumping structure for table ece366restaurant.user_login
CREATE TABLE IF NOT EXISTS `user_login` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int unsigned NOT NULL,
  `refresh_token` varchar(1000) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `user_agent` varchar(1000) COLLATE utf8_unicode_ci DEFAULT NULL,
  `expiration_date` datetime NOT NULL,
  `revoked` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `user_login_fk_idx` (`user_id`),
  CONSTRAINT `user_login_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Dumping data for table ece366restaurant.user_login: ~0 rows (approximately)
DELETE FROM `user_login`;
/*!40000 ALTER TABLE `user_login` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_login` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
