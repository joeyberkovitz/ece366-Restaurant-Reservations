CREATE DATABASE  IF NOT EXISTS `ece366restaurant` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `ece366restaurant`;
-- MySQL dump 10.13  Distrib 8.0.19, for Win64 (x86_64)
--
-- Host: localhost    Database: ece366restaurant
-- ------------------------------------------------------
-- Server version	8.0.19

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `address`
--

DROP TABLE IF EXISTS `address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `address` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `latitude` decimal(10,8) NOT NULL,
  `longitude` decimal(10,8) NOT NULL,
  `line1` varchar(50) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `line2` varchar(50) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `city` varchar(50) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `state` varchar(50) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `zip` varchar(50) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `country` varchar(50) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address`
--

LOCK TABLES `address` WRITE;
/*!40000 ALTER TABLE `address` DISABLE KEYS */;
/*!40000 ALTER TABLE `address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=123 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'Afghan'),(2,'African'),(3,'American (New)'),(4,'American (Traditional)'),(5,'Arabian'),(6,'Argentine'),(7,'Armenian'),(8,'Asian Fusion'),(9,'Australian'),(10,'Austrian'),(11,'Bangladeshi'),(12,'Barbeque'),(13,'Basque'),(14,'Belgian'),(15,'Brasseries'),(16,'Brazilian'),(17,'Breakfast & Brunch'),(18,'British'),(19,'Buffets'),(20,'Burgers'),(21,'Burmese'),(22,'Cafes'),(23,'Cafeteria'),(24,'Cajun/Creole'),(25,'Cambodian'),(26,'Caribbean'),(27,'Catalan'),(28,'Cheesesteaks'),(29,'Chicken Shop'),(30,'Chicken Wings'),(31,'Chinese'),(32,'Comfort Food'),(33,'Creperies'),(34,'Cuban'),(35,'Czech'),(36,'Delis'),(37,'Diners'),(38,'Dinner Theater'),(39,'Ethiopian'),(40,'Fast Food'),(41,'Filipino'),(42,'Fish & Chips'),(43,'Fondue'),(44,'Food Court'),(45,'Food Stands'),(46,'French'),(47,'Game Meat'),(48,'Gastropubs'),(49,'German'),(50,'Gluten-Free'),(51,'Greek'),(52,'Guamanian'),(53,'Halal'),(54,'Hawaiian'),(55,'Himalayan/Nepalese'),(56,'Honduran'),(57,'Hong Kong Style Cafe'),(58,'Hot Dogs'),(59,'Hot Pot'),(60,'Hungarian'),(61,'Iberian'),(62,'Indian'),(63,'Indonesian'),(64,'Irish'),(65,'Italian'),(66,'Japanese'),(67,'Kebab'),(68,'Korean'),(69,'Kosher'),(70,'Laotian'),(71,'Latin American'),(72,'Live/Raw Food'),(73,'Malaysian'),(74,'Mediterranean'),(75,'Mexican'),(76,'Middle Eastern'),(77,'Modern European'),(78,'Mongolian'),(79,'Moroccan'),(80,'New Mexican Cuisine'),(81,'Nicaraguan'),(82,'Noodles'),(83,'Pakistani'),(84,'Pan Asia'),(85,'Persian/Iranian'),(86,'Peruvian'),(87,'Pizza'),(88,'Polish'),(89,'Polynesian'),(90,'Pop-Up Restaurants'),(91,'Portuguese'),(92,'Poutineries'),(93,'Russian'),(94,'Salad'),(95,'Sandwiches'),(96,'Scandinavian'),(97,'Scottish'),(98,'Seafood'),(99,'Singaporean'),(100,'Slovakian'),(101,'Soul Food'),(102,'Soup'),(103,'Southern'),(104,'Spanish'),(105,'Sri Lankan'),(106,'Steakhouses'),(107,'Supper Clubs'),(108,'Sushi Bars'),(109,'Syrian'),(110,'Taiwanese'),(111,'Tapas Bars'),(112,'Tapas/Small Plates'),(113,'Tex-Mex'),(114,'Thai'),(115,'Turkish'),(116,'Ukrainian'),(117,'Uzbek'),(118,'Vegan'),(119,'Vegetarian'),(120,'Vietnamese'),(121,'Waffles'),(122,'Wraps');
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contact`
--

DROP TABLE IF EXISTS `contact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contact` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `email` varchar(320) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contact`
--

LOCK TABLES `contact` WRITE;
/*!40000 ALTER TABLE `contact` DISABLE KEYS */;
/*!40000 ALTER TABLE `contact` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservation`
--

DROP TABLE IF EXISTS `reservation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservation` (
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservation`
--

LOCK TABLES `reservation` WRITE;
/*!40000 ALTER TABLE `reservation` DISABLE KEYS */;
/*!40000 ALTER TABLE `reservation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservation_table`
--

DROP TABLE IF EXISTS `reservation_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservation_table` (
  `table_id` int unsigned NOT NULL,
  `reservation_id` int unsigned NOT NULL,
  UNIQUE KEY `reservationtable_unique` (`table_id`,`reservation_id`),
  KEY `reservationtable_table_fk_idx` (`table_id`),
  KEY `reservationtable_reservation_fk_idx` (`reservation_id`),
  CONSTRAINT `reservationtable_reservation_fk` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `reservationtable_table_fk` FOREIGN KEY (`table_id`) REFERENCES `table` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservation_table`
--

LOCK TABLES `reservation_table` WRITE;
/*!40000 ALTER TABLE `reservation_table` DISABLE KEYS */;
/*!40000 ALTER TABLE `reservation_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservation_user`
--

DROP TABLE IF EXISTS `reservation_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservation_user` (
  `reservation_id` int unsigned NOT NULL,
  `user_id` int unsigned NOT NULL,
  UNIQUE KEY `reservationuser_unique` (`reservation_id`,`user_id`),
  KEY `reservationuser_user_idx` (`user_id`),
  CONSTRAINT `reservationuser_reservation` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `reservationuser_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservation_user`
--

LOCK TABLES `reservation_user` WRITE;
/*!40000 ALTER TABLE `reservation_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `reservation_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `restaurant`
--

DROP TABLE IF EXISTS `restaurant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `restaurant` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `address_id` int unsigned NOT NULL,
  `contact_id` int unsigned NOT NULL,
  `category_id` int unsigned NOT NULL,
  `capacity_factor` int unsigned NOT NULL DEFAULT (0),
  `reservation_time` int unsigned NOT NULL DEFAULT (2),
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `restaurant_contact_fk_idx` (`contact_id`),
  KEY `restaurant_category_fk_idx` (`category_id`),
  KEY `restaurant_address_fk_idx` (`address_id`),
  CONSTRAINT `restaurant_address_fk` FOREIGN KEY (`address_id`) REFERENCES `address` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `restaurant_category_fk` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `restaurant_contact_fk` FOREIGN KEY (`contact_id`) REFERENCES `contact` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `restaurant`
--

LOCK TABLES `restaurant` WRITE;
/*!40000 ALTER TABLE `restaurant` DISABLE KEYS */;
/*!40000 ALTER TABLE `restaurant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `restaurant_user`
--

DROP TABLE IF EXISTS `restaurant_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `restaurant_user` (
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `restaurant_user`
--

LOCK TABLES `restaurant_user` WRITE;
/*!40000 ALTER TABLE `restaurant_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `restaurant_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (2,'Admin'),(1,'Customer'),(3,'Manager');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `status`
--

DROP TABLE IF EXISTS `status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `status` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `status`
--

LOCK TABLES `status` WRITE;
/*!40000 ALTER TABLE `status` DISABLE KEYS */;
INSERT INTO `status` VALUES (2,'Cancelled'),(1,'Opened');
/*!40000 ALTER TABLE `status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `table`
--

DROP TABLE IF EXISTS `table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `table` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `label` varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `capacity` tinyint unsigned NOT NULL,
  `restaurant_id` int unsigned NOT NULL,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `table_restaurant_fk_idx` (`restaurant_id`),
  CONSTRAINT `table_restaurant_fk` FOREIGN KEY (`restaurant_id`) REFERENCES `restaurant` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `table`
--

LOCK TABLES `table` WRITE;
/*!40000 ALTER TABLE `table` DISABLE KEYS */;
/*!40000 ALTER TABLE `table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(320) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `password` varchar(200) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `fname` varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `lname` varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `contact_id` int unsigned NOT NULL,
  `role_id` int unsigned NOT NULL,
  `rewards_points` int NOT NULL DEFAULT (0),
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  UNIQUE KEY `contact_id_UNIQUE` (`contact_id`),
  KEY `user_role_fk_idx` (`role_id`),
  CONSTRAINT `user_contact_fk` FOREIGN KEY (`contact_id`) REFERENCES `contact` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `user_role_fk` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_login`
--

DROP TABLE IF EXISTS `user_login`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_login` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int unsigned NOT NULL,
  `refresh_token` varchar(1000) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `user_agent` varchar(1000) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `expiration_date` datetime NOT NULL,
  `revoked` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `user_login_fk_idx` (`user_id`),
  CONSTRAINT `user_login_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_login`
--

LOCK TABLES `user_login` WRITE;
/*!40000 ALTER TABLE `user_login` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_login` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-04-27 17:30:34
