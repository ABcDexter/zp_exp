-- MariaDB dump 10.17  Distrib 10.4.13-MariaDB, for Linux (x86_64)
--
-- Host: localhost    Database: zp
-- ------------------------------------------------------
-- Server version	10.4.13-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `auth_group`
--

DROP TABLE IF EXISTS `auth_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_group`
--

LOCK TABLES `auth_group` WRITE;
/*!40000 ALTER TABLE `auth_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_group_permissions`
--

DROP TABLE IF EXISTS `auth_group_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_group_permissions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `auth_group_permissions_group_id_permission_id_0cd325b0_uniq` (`group_id`,`permission_id`),
  KEY `auth_group_permissio_permission_id_84c5c92e_fk_auth_perm` (`permission_id`),
  CONSTRAINT `auth_group_permissio_permission_id_84c5c92e_fk_auth_perm` FOREIGN KEY (`permission_id`) REFERENCES `auth_permission` (`id`),
  CONSTRAINT `auth_group_permissions_group_id_b120cbf9_fk_auth_group_id` FOREIGN KEY (`group_id`) REFERENCES `auth_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_group_permissions`
--

LOCK TABLES `auth_group_permissions` WRITE;
/*!40000 ALTER TABLE `auth_group_permissions` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_group_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_permission`
--

DROP TABLE IF EXISTS `auth_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_permission` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content_type_id` int(11) NOT NULL,
  `codename` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `auth_permission_content_type_id_codename_01ab375a_uniq` (`content_type_id`,`codename`),
  CONSTRAINT `auth_permission_content_type_id_2f476e4b_fk_django_co` FOREIGN KEY (`content_type_id`) REFERENCES `django_content_type` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_permission`
--

LOCK TABLES `auth_permission` WRITE;
/*!40000 ALTER TABLE `auth_permission` DISABLE KEYS */;
INSERT INTO `auth_permission` VALUES (1,'Can add driver',1,'add_driver'),(2,'Can change driver',1,'change_driver'),(3,'Can delete driver',1,'delete_driver'),(4,'Can view driver',1,'view_driver'),(5,'Can add location',2,'add_location'),(6,'Can change location',2,'change_location'),(7,'Can delete location',2,'delete_location'),(8,'Can view location',2,'view_location'),(9,'Can add place',3,'add_place'),(10,'Can change place',3,'change_place'),(11,'Can delete place',3,'delete_place'),(12,'Can view place',3,'view_place'),(13,'Can add progress',4,'add_progress'),(14,'Can change progress',4,'change_progress'),(15,'Can delete progress',4,'delete_progress'),(16,'Can view progress',4,'view_progress'),(17,'Can add route',5,'add_route'),(18,'Can change route',5,'change_route'),(19,'Can delete route',5,'delete_route'),(20,'Can view route',5,'view_route'),(21,'Can add trip',6,'add_trip'),(22,'Can change trip',6,'change_trip'),(23,'Can delete trip',6,'delete_trip'),(24,'Can view trip',6,'view_trip'),(25,'Can add user',7,'add_user'),(26,'Can change user',7,'change_user'),(27,'Can delete user',7,'delete_user'),(28,'Can view user',7,'view_user'),(29,'Can add vehicle',8,'add_vehicle'),(30,'Can change vehicle',8,'change_vehicle'),(31,'Can delete vehicle',8,'delete_vehicle'),(32,'Can view vehicle',8,'view_vehicle'),(33,'Can add log entry',9,'add_logentry'),(34,'Can change log entry',9,'change_logentry'),(35,'Can delete log entry',9,'delete_logentry'),(36,'Can view log entry',9,'view_logentry'),(37,'Can add permission',10,'add_permission'),(38,'Can change permission',10,'change_permission'),(39,'Can delete permission',10,'delete_permission'),(40,'Can view permission',10,'view_permission'),(41,'Can add group',11,'add_group'),(42,'Can change group',11,'change_group'),(43,'Can delete group',11,'delete_group'),(44,'Can view group',11,'view_group'),(45,'Can add user',12,'add_user'),(46,'Can change user',12,'change_user'),(47,'Can delete user',12,'delete_user'),(48,'Can view user',12,'view_user'),(49,'Can add content type',13,'add_contenttype'),(50,'Can change content type',13,'change_contenttype'),(51,'Can delete content type',13,'delete_contenttype'),(52,'Can view content type',13,'view_contenttype'),(53,'Can add session',14,'add_session'),(54,'Can change session',14,'change_session'),(55,'Can delete session',14,'delete_session'),(56,'Can view session',14,'view_session');
/*!40000 ALTER TABLE `auth_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_user`
--

DROP TABLE IF EXISTS `auth_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `password` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `last_login` datetime(6) DEFAULT NULL,
  `is_superuser` tinyint(1) NOT NULL,
  `username` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `first_name` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `last_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(254) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_staff` tinyint(1) NOT NULL,
  `is_active` tinyint(1) NOT NULL,
  `date_joined` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_user`
--

LOCK TABLES `auth_user` WRITE;
/*!40000 ALTER TABLE `auth_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_user_groups`
--

DROP TABLE IF EXISTS `auth_user_groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_user_groups` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `group_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `auth_user_groups_user_id_group_id_94350c0c_uniq` (`user_id`,`group_id`),
  KEY `auth_user_groups_group_id_97559544_fk_auth_group_id` (`group_id`),
  CONSTRAINT `auth_user_groups_group_id_97559544_fk_auth_group_id` FOREIGN KEY (`group_id`) REFERENCES `auth_group` (`id`),
  CONSTRAINT `auth_user_groups_user_id_6a12ed8b_fk_auth_user_id` FOREIGN KEY (`user_id`) REFERENCES `auth_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_user_groups`
--

LOCK TABLES `auth_user_groups` WRITE;
/*!40000 ALTER TABLE `auth_user_groups` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_user_groups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_user_user_permissions`
--

DROP TABLE IF EXISTS `auth_user_user_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_user_user_permissions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `auth_user_user_permissions_user_id_permission_id_14a6b632_uniq` (`user_id`,`permission_id`),
  KEY `auth_user_user_permi_permission_id_1fbb5f2c_fk_auth_perm` (`permission_id`),
  CONSTRAINT `auth_user_user_permi_permission_id_1fbb5f2c_fk_auth_perm` FOREIGN KEY (`permission_id`) REFERENCES `auth_permission` (`id`),
  CONSTRAINT `auth_user_user_permissions_user_id_a95ead1b_fk_auth_user_id` FOREIGN KEY (`user_id`) REFERENCES `auth_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_user_user_permissions`
--

LOCK TABLES `auth_user_user_permissions` WRITE;
/*!40000 ALTER TABLE `auth_user_user_permissions` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_user_user_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `django_admin_log`
--

DROP TABLE IF EXISTS `django_admin_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `django_admin_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `action_time` datetime(6) NOT NULL,
  `object_id` longtext COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `object_repr` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `action_flag` smallint(5) unsigned NOT NULL CHECK (`action_flag` >= 0),
  `change_message` longtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `content_type_id` int(11) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `django_admin_log_content_type_id_c4bce8eb_fk_django_co` (`content_type_id`),
  KEY `django_admin_log_user_id_c564eba6_fk_auth_user_id` (`user_id`),
  CONSTRAINT `django_admin_log_content_type_id_c4bce8eb_fk_django_co` FOREIGN KEY (`content_type_id`) REFERENCES `django_content_type` (`id`),
  CONSTRAINT `django_admin_log_user_id_c564eba6_fk_auth_user_id` FOREIGN KEY (`user_id`) REFERENCES `auth_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `django_admin_log`
--

LOCK TABLES `django_admin_log` WRITE;
/*!40000 ALTER TABLE `django_admin_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `django_admin_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `django_content_type`
--

DROP TABLE IF EXISTS `django_content_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `django_content_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `app_label` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `model` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `django_content_type_app_label_model_76bd3d3b_uniq` (`app_label`,`model`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `django_content_type`
--

LOCK TABLES `django_content_type` WRITE;
/*!40000 ALTER TABLE `django_content_type` DISABLE KEYS */;
INSERT INTO `django_content_type` VALUES (9,'admin','logentry'),(11,'auth','group'),(10,'auth','permission'),(12,'auth','user'),(13,'contenttypes','contenttype'),(14,'sessions','session'),(1,'zp','driver'),(2,'zp','location'),(3,'zp','place'),(4,'zp','progress'),(5,'zp','route'),(6,'zp','trip'),(7,'zp','user'),(8,'zp','vehicle');
/*!40000 ALTER TABLE `django_content_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `django_migrations`
--

DROP TABLE IF EXISTS `django_migrations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `django_migrations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `app` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `applied` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `django_migrations`
--

LOCK TABLES `django_migrations` WRITE;
/*!40000 ALTER TABLE `django_migrations` DISABLE KEYS */;
INSERT INTO `django_migrations` VALUES (1,'contenttypes','0001_initial','2020-05-21 06:39:51.214216'),(2,'auth','0001_initial','2020-05-21 06:39:51.412886'),(3,'admin','0001_initial','2020-05-21 06:39:52.163453'),(4,'admin','0002_logentry_remove_auto_add','2020-05-21 06:39:52.639234'),(5,'admin','0003_logentry_add_action_flag_choices','2020-05-21 06:39:52.651832'),(6,'contenttypes','0002_remove_content_type_name','2020-05-21 06:39:52.727752'),(7,'auth','0002_alter_permission_name_max_length','2020-05-21 06:39:52.805563'),(8,'auth','0003_alter_user_email_max_length','2020-05-21 06:39:52.825431'),(9,'auth','0004_alter_user_username_opts','2020-05-21 06:39:52.837819'),(10,'auth','0005_alter_user_last_login_null','2020-05-21 06:39:52.921776'),(11,'auth','0006_require_contenttypes_0002','2020-05-21 06:39:52.936681'),(12,'auth','0007_alter_validators_add_error_messages','2020-05-21 06:39:52.954073'),(13,'auth','0008_alter_user_username_max_length','2020-05-21 06:39:53.003224'),(14,'auth','0009_alter_user_last_name_max_length','2020-05-21 06:39:53.030676'),(15,'auth','0010_alter_group_name_max_length','2020-05-21 06:39:53.054575'),(16,'auth','0011_update_proxy_permissions','2020-05-21 06:39:53.070904'),(17,'sessions','0001_initial','2020-05-21 06:39:53.103072'),(18,'zp','0001_initial','2020-05-21 06:39:53.438446');
/*!40000 ALTER TABLE `django_migrations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `django_session`
--

DROP TABLE IF EXISTS `django_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `django_session` (
  `session_key` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `session_data` longtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `expire_date` datetime(6) NOT NULL,
  PRIMARY KEY (`session_key`),
  KEY `django_session_expire_date_a5c62663` (`expire_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `django_session`
--

LOCK TABLES `django_session` WRITE;
/*!40000 ALTER TABLE `django_session` DISABLE KEYS */;
/*!40000 ALTER TABLE `django_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `driver`
--

DROP TABLE IF EXISTS `driver`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `driver` (
  `an` bigint(20) NOT NULL,
  `pn` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `auth` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL,
  `mode` varchar(2) COLLATE utf8mb4_unicode_ci NOT NULL,
  `pid` int(11) DEFAULT NULL,
  `tid` int(11) NOT NULL,
  `dl` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gdr` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `hs` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`an`),
  KEY `driver_pn_64343c29` (`pn`),
  KEY `driver_auth_ad685462` (`auth`),
  KEY `driver_mode_b58afc86` (`mode`),
  KEY `driver_pid_f5fd5c09` (`pid`),
  KEY `driver_tid_3a15350c` (`tid`),
  KEY `driver_name_8b634840` (`name`),
  KEY `driver_gdr_a58a4735` (`gdr`),
  KEY `driver_age_74d2c339` (`age`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `driver`
--

LOCK TABLES `driver` WRITE;
/*!40000 ALTER TABLE `driver` DISABLE KEYS */;
INSERT INTO `driver` VALUES (1000,'9800000000','dauth0','AV',2,-1,'UK0123456789124','driver000','m',30,'uk'),(1001,'9800000001','dauth1','AV',2,-1,'UK0123456789124','driver001','m',30,'uk'),(1002,'9800000002','dauth2','OF',3,-1,'UK0123456789125','driver002','f',31,'uk'),(1003,'9800000003','dauth3','AV',2,-1,'UK0123456789126','driver003','m',32,'uk'),(1004,'9800000004','dauth4','AV',5,-1,'UK0123456780128','driver004','m',33,'uk'),(1005,'9800000005','dauth5','AV',5,-1,'UK0123456709129','driver005','m',34,'uk'),(1006,'9800000006','dauth6','AV',5,-1,'UK0123456089130','driver006','f',35,'uk'),(1007,'9800000007','dauth7','AV',4,-1,'UK0123456089131','driver007','m',36,'uk');
/*!40000 ALTER TABLE `driver` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location`
--

DROP TABLE IF EXISTS `location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location` (
  `an` bigint(20) NOT NULL,
  `lat` double NOT NULL,
  `lng` double NOT NULL,
  `time` datetime(6) NOT NULL,
  `kind` int(11) NOT NULL,
  PRIMARY KEY (`an`),
  KEY `location_kind_cc47a038` (`kind`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location`
--

LOCK TABLES `location` WRITE;
/*!40000 ALTER TABLE `location` DISABLE KEYS */;
INSERT INTO `location` VALUES (0,29.354217,79.52201,'2020-05-21 12:22:52.000000',0),(1,29.220449,79.47853,'2020-05-21 12:22:52.000000',0),(2,29.239276,79.58613,'2020-05-21 12:22:52.000000',0),(3,29.354217,79.52201,'2020-05-21 12:22:52.000000',0),(4,29.348567,79.54465,'2020-05-21 12:22:52.000000',0),(5,29.348567,79.54465,'2020-05-21 12:22:52.000000',0),(6,29.348369,79.55901,'2020-05-21 13:19:38.289109',3),(7,29.239276,79.58613,'2020-05-21 12:22:52.000000',0),(8,29.359265,79.55001,'2020-05-21 12:22:52.000000',0),(9,29.348567,79.54465,'2020-05-21 12:22:52.000000',0),(10,29.354217,79.52201,'2020-05-21 12:22:52.000000',0),(1000,29.348369,79.55901,'2020-05-21 12:22:52.000000',1),(1001,29.348369,79.55901,'2020-05-21 13:19:38.284659',1),(1002,29.348567,79.54465,'2020-05-21 12:22:52.000000',1),(1003,29.348369,79.55901,'2020-05-21 13:13:01.651764',1),(1004,29.359265,79.55001,'2020-05-21 13:13:53.140942',1),(1005,29.3581754,79.55091,'2020-05-21 13:10:16.545761',1),(1006,29.359265,79.55001,'2020-05-21 12:21:20.670378',1),(1007,29.354217,79.52201,'2020-05-21 13:05:32.628248',1),(2000,29.354217,79.52201,'2020-05-21 11:06:17.057653',2),(2001,29.348369,79.55901,'2020-05-21 13:13:01.645906',2),(2002,29.348567,79.54465,'2020-05-21 13:11:47.858894',2),(2003,29.220449,79.47853,'2020-05-21 09:08:30.324559',2),(2004,29.33217335,79.550872,'2020-05-21 12:58:36.228822',2),(2005,29.2412714,79.489252,'2020-05-21 10:40:21.947329',2),(2006,29.359265,79.55001,'2020-05-21 12:32:12.410898',2),(2007,29.220449,79.47853,'2020-05-21 10:37:07.077061',2),(2008,29.220449,79.47853,'2020-05-21 10:02:01.713583',2),(2009,29.3384426,79.539288,'2020-05-21 12:00:18.966338',2),(2010,29.348369,79.55901,'2020-05-21 13:19:38.279592',2),(2011,29.359265,79.55001,'2020-05-21 11:30:22.464921',2),(2012,29.220449,79.47853,'2020-05-21 10:31:47.438765',2),(2013,29.239276,79.58613,'2020-05-21 09:12:42.617651',2),(2014,29.348369,79.55901,'2020-05-21 12:41:47.688172',2),(2015,29.220449,79.47853,'2020-05-21 13:18:50.578949',2),(2016,29.239276,79.58613,'2020-05-21 12:07:27.977735',2),(2017,29.348369,79.55901,'2020-05-21 12:22:52.000000',2),(2018,29.33217335,79.550872,'2020-05-21 12:53:35.234235',2),(2019,29.3533811,79.547062,'2020-05-21 12:49:17.639953',2),(2020,29.348567,79.54465,'2020-05-21 13:18:05.306360',2);
/*!40000 ALTER TABLE `location` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `place`
--

DROP TABLE IF EXISTS `place`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `place` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pn` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `lat` double NOT NULL,
  `lng` double NOT NULL,
  `alt` int(11) NOT NULL,
  `wt` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pn` (`pn`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `place`
--

LOCK TABLES `place` WRITE;
/*!40000 ALTER TABLE `place` DISABLE KEYS */;
INSERT INTO `place` VALUES (1,'HANUMAN TEMPLE',29.239276,79.58613,1328,100),(2,'DAATH BHIMTAL',29.348369,79.55901,1337,100),(3,'POLICE STATION',29.348567,79.54465,1338,100),(4,'BHIMTAL GOVT HOSPITAL',29.354217,79.52201,1520,100),(5,'BYPASS ROAD BHIMTAL',29.359265,79.55001,1379,100),(6,'MEHRA GAON',29.220449,79.47853,607,100);
/*!40000 ALTER TABLE `place` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `progress`
--

DROP TABLE IF EXISTS `progress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `progress` (
  `tid` int(11) NOT NULL,
  `pct` int(11) NOT NULL,
  PRIMARY KEY (`tid`),
  KEY `progress_pct_ea5c0417` (`pct`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `progress`
--

LOCK TABLES `progress` WRITE;
/*!40000 ALTER TABLE `progress` DISABLE KEYS */;
INSERT INTO `progress` VALUES (1,0),(2,0),(3,0),(4,0),(5,0),(6,0),(7,0),(8,0),(9,0),(11,0),(13,0),(14,0),(15,0),(18,0),(19,0),(20,0),(21,0),(22,0),(23,0),(24,0),(25,0),(26,0),(27,0),(28,0),(29,0),(32,0),(33,0),(34,0),(35,0),(36,0),(41,0),(44,0),(48,0),(54,0),(55,0),(58,0),(61,0),(62,0),(63,0),(69,0),(70,0),(71,0),(73,0),(74,0),(75,0),(76,0),(78,0),(80,0),(88,0),(90,0),(106,0),(113,0),(114,0),(115,0),(119,0),(121,0),(125,0),(126,0),(130,0),(131,0),(135,0),(141,0),(143,0),(144,0),(147,0),(152,0),(155,0),(159,0),(164,0),(172,0),(173,0),(186,0),(188,0),(189,0),(190,0),(192,0),(193,0),(201,0),(204,0),(208,0),(218,0),(222,0),(231,0),(234,0),(235,0),(237,0),(238,0),(252,0),(255,0),(260,0),(262,0),(267,0),(268,0),(269,0),(270,0),(271,0),(272,0),(273,0),(274,0),(275,0),(276,0),(277,0),(278,0),(279,0),(280,0),(281,0),(282,0),(283,0),(284,0),(285,0),(286,0),(287,0),(288,0),(289,0),(290,0),(291,0),(292,0),(293,0),(294,0),(295,0),(296,0),(297,0),(298,0),(299,0),(300,0),(301,0),(302,0),(303,0),(304,0),(305,0),(306,0),(307,0),(308,0),(309,0),(310,0),(311,0),(312,0),(313,0),(314,0),(31,15),(47,15),(60,15),(89,15),(100,15),(118,15),(167,15),(178,15),(179,15),(184,15),(187,15),(198,15),(206,15),(232,15),(236,15),(243,15),(30,30),(39,30),(49,30),(83,30),(139,30),(171,30),(176,30),(199,30),(227,30),(45,45),(59,45),(102,45),(103,45),(140,45),(174,45),(224,45),(226,45),(229,45),(50,60),(65,60),(66,60),(91,60),(95,60),(98,60),(109,60),(136,60),(150,60),(157,60),(165,60),(166,60),(233,60),(93,75),(97,75),(127,75),(158,75),(212,75),(217,75),(223,75),(230,75),(251,75),(43,90),(53,90),(87,90),(108,90),(169,90),(177,90),(207,90),(209,90),(225,90),(254,90),(10,100),(12,100),(16,100),(17,100),(37,100),(38,100),(40,100),(42,100),(46,100),(51,100),(52,100),(56,100),(57,100),(64,100),(67,100),(68,100),(72,100),(77,100),(79,100),(81,100),(82,100),(84,100),(85,100),(86,100),(92,100),(94,100),(96,100),(99,100),(101,100),(104,100),(105,100),(107,100),(110,100),(111,100),(112,100),(116,100),(117,100),(120,100),(122,100),(123,100),(124,100),(128,100),(129,100),(132,100),(133,100),(134,100),(137,100),(138,100),(142,100),(145,100),(146,100),(148,100),(149,100),(151,100),(153,100),(154,100),(156,100),(160,100),(161,100),(162,100),(163,100),(168,100),(170,100),(175,100),(180,100),(181,100),(182,100),(183,100),(185,100),(191,100),(194,100),(195,100),(196,100),(197,100),(200,100),(202,100),(203,100),(205,100),(210,100),(211,100),(213,100),(214,100),(215,100),(216,100),(219,100),(220,100),(221,100),(228,100),(239,100),(240,100),(241,100),(242,100),(244,100),(245,100),(246,100),(247,100),(248,100),(249,100),(250,100),(253,100),(256,100),(257,100),(258,100),(259,100),(261,100),(263,100),(264,100),(265,100),(266,100);
/*!40000 ALTER TABLE `progress` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `route`
--

DROP TABLE IF EXISTS `route`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `route` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `idx` int(11) NOT NULL,
  `idy` int(11) NOT NULL,
  `dist` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `route_idx_f49387_idx` (`idx`,`idy`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `route`
--

LOCK TABLES `route` WRITE;
/*!40000 ALTER TABLE `route` DISABLE KEYS */;
INSERT INTO `route` VALUES (1,1,2,25968),(2,1,3,26769),(3,1,4,35931),(4,1,5,26923),(5,1,6,15191),(6,2,3,2612),(7,2,4,10910),(8,2,5,1902),(9,2,6,30655),(10,3,4,12575),(11,3,5,3567),(12,3,6,31457),(13,4,5,10285),(14,4,6,40619),(15,5,6,31610);
/*!40000 ALTER TABLE `route` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trip`
--

DROP TABLE IF EXISTS `trip`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trip` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `st` varchar(2) COLLATE utf8mb4_unicode_ci NOT NULL,
  `uan` bigint(20) NOT NULL,
  `dan` bigint(20) NOT NULL,
  `van` bigint(20) NOT NULL,
  `rtime` datetime(6) NOT NULL,
  `atime` datetime(6) DEFAULT NULL,
  `stime` datetime(6) DEFAULT NULL,
  `etime` datetime(6) DEFAULT NULL,
  `srcid` int(11) NOT NULL,
  `dstid` int(11) NOT NULL,
  `npas` int(11) NOT NULL,
  `rtype` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `pmode` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `trip_st_1e7bde0f` (`st`),
  KEY `trip_uan_f53eacb0` (`uan`),
  KEY `trip_dan_72acf51a` (`dan`),
  KEY `trip_van_ad1c3d22` (`van`),
  KEY `trip_rtime_5cee8dfe` (`rtime`),
  KEY `trip_atime_4043e6da` (`atime`),
  KEY `trip_stime_e633e888` (`stime`),
  KEY `trip_etime_7ba7b52c` (`etime`),
  KEY `trip_srcid_8859e33b` (`srcid`),
  KEY `trip_dstid_c830080d` (`dstid`),
  KEY `trip_rtype_94ffb7e4` (`rtype`),
  KEY `trip_pmode_1fc71b9f` (`pmode`)
) ENGINE=InnoDB AUTO_INCREMENT=315 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trip`
--

LOCK TABLES `trip` WRITE;
/*!40000 ALTER TABLE `trip` DISABLE KEYS */;
INSERT INTO `trip` VALUES (2,'DN',1,1001,2014,'2020-05-21 08:04:09.474992','2020-05-21 08:04:10.482814',NULL,'2020-05-21 08:04:34.705757',2,5,1,'0','0'),(3,'CN',3,1001,2014,'2020-05-21 08:19:06.011642','2020-05-21 08:19:07.295060',NULL,'2020-05-21 08:19:11.062800',2,5,1,'0','0'),(4,'CN',5,1001,2003,'2020-05-21 08:29:28.912337','2020-05-21 08:29:31.142995',NULL,'2020-05-21 08:29:33.969108',2,1,1,'0','0'),(5,'PD',6,1001,2017,'2020-05-21 08:37:58.111001','2020-05-21 08:37:58.251063','2020-05-21 08:38:08.354744','2020-05-21 08:38:58.577487',2,3,1,'0','0'),(6,'TO',6,0,0,'2020-05-21 08:44:35.224026',NULL,NULL,NULL,2,5,1,'0','0'),(7,'TO',6,0,0,'2020-05-21 08:47:55.929059',NULL,NULL,NULL,2,1,1,'0','0'),(8,'PD',6,1001,2011,'2020-05-21 08:51:11.658754','2020-05-21 08:53:05.841335','2020-05-21 08:53:07.879489','2020-05-21 08:54:47.776758',2,6,1,'0','0'),(9,'PD',6,1001,2017,'2020-05-21 08:58:15.682108','2020-05-21 08:58:26.569116','2020-05-21 08:58:32.647257','2020-05-21 08:58:50.898160',2,1,1,'0','0'),(10,'PD',6,1001,2003,'2020-05-21 09:07:59.095044','2020-05-21 09:08:08.912991','2020-05-21 09:08:10.954475','2020-05-21 09:08:30.361191',2,6,1,'0','0'),(11,'DN',6,1001,2008,'2020-05-21 09:08:49.362541','2020-05-21 09:08:56.437980',NULL,'2020-05-21 09:08:58.481570',6,2,1,'0','0'),(12,'PD',6,1001,2020,'2020-05-21 09:09:24.507106','2020-05-21 09:09:25.643311','2020-05-21 09:09:29.687616','2020-05-21 09:09:51.552096',6,3,1,'0','0'),(13,'CN',6,1001,2007,'2020-05-21 09:10:54.909392','2020-05-21 09:10:57.210161',NULL,'2020-05-21 09:10:59.964093',3,5,1,'0','0'),(14,'DN',6,1001,2000,'2020-05-21 09:11:30.100490','2020-05-21 09:11:31.469483',NULL,'2020-05-21 09:11:35.653352',3,6,1,'0','0'),(15,'CN',6,1001,2000,'2020-05-21 09:11:50.229977','2020-05-21 09:11:57.782803',NULL,'2020-05-21 09:12:00.312100',3,5,1,'0','0'),(16,'PD',6,1001,2013,'2020-05-21 09:12:15.390697','2020-05-21 09:12:21.953470','2020-05-21 09:12:26.011340','2020-05-21 09:12:42.646745',3,1,1,'0','0'),(17,'PD',6,1001,2002,'2020-05-21 09:13:00.649081','2020-05-21 09:13:07.704421','2020-05-21 09:13:11.763679','2020-05-21 09:13:28.541068',1,3,1,'0','0'),(18,'TO',6,0,0,'2020-05-21 09:13:45.900813',NULL,NULL,NULL,5,6,1,'0','0'),(19,'TO',6,0,0,'2020-05-21 09:17:06.697595',NULL,NULL,NULL,5,3,1,'0','0'),(20,'TO',6,0,0,'2020-05-21 09:20:27.427302',NULL,NULL,NULL,5,3,1,'0','0'),(21,'TO',6,0,0,'2020-05-21 09:24:03.194849',NULL,NULL,NULL,5,4,1,'0','0'),(22,'TO',6,0,0,'2020-05-21 09:27:18.882077',NULL,NULL,NULL,5,3,1,'0','0'),(23,'TO',6,0,0,'2020-05-21 09:30:39.611897',NULL,NULL,NULL,5,2,1,'0','0'),(24,'TO',6,0,0,'2020-05-21 09:34:00.330358',NULL,NULL,NULL,5,6,1,'0','0'),(25,'TO',6,0,0,'2020-05-21 09:37:21.035245',NULL,NULL,NULL,5,4,1,'0','0'),(26,'TO',6,0,0,'2020-05-21 09:40:41.763222',NULL,NULL,NULL,5,6,1,'0','0'),(27,'TO',6,0,0,'2020-05-21 09:44:02.508329',NULL,NULL,NULL,5,6,1,'0','0'),(28,'TO',6,0,0,'2020-05-21 09:47:23.212522',NULL,NULL,NULL,5,1,1,'0','0'),(29,'CN',6,1007,2016,'2020-05-21 09:50:43.929682','2020-05-21 09:51:20.867306',NULL,'2020-05-21 09:51:24.116319',5,1,1,'0','0'),(30,'PD',6,1007,2016,'2020-05-21 09:51:40.648989','2020-05-21 09:51:51.267780','2020-05-21 09:51:56.324026','2020-05-21 09:52:08.357206',5,3,1,'0','0'),(31,'PD',6,1007,2004,'2020-05-21 09:52:58.532085','2020-05-21 09:53:13.664541','2020-05-21 09:53:18.693185','2020-05-21 09:53:28.665245',5,3,1,'0','0'),(32,'CN',6,1007,2004,'2020-05-21 09:53:48.725534','2020-05-21 09:54:01.808080',NULL,'2020-05-21 09:54:03.803276',5,6,1,'0','0'),(33,'PD',6,1007,2016,'2020-05-21 09:54:33.905419','2020-05-21 09:54:37.977637','2020-05-21 09:54:43.013930','2020-05-21 09:54:44.004243',5,2,1,'0','0'),(34,'CN',6,1007,2016,'2020-05-21 09:55:04.093466','2020-05-21 09:55:17.162585',NULL,'2020-05-21 09:55:19.186303',5,6,1,'0','0'),(35,'DN',6,1007,2004,'2020-05-21 09:55:49.336879','2020-05-21 09:55:53.367189',NULL,'2020-05-21 09:55:58.412061',5,1,1,'0','0'),(36,'CN',6,1007,2004,'2020-05-21 09:56:09.461419','2020-05-21 09:56:26.525344',NULL,'2020-05-21 09:56:29.557275',5,3,1,'0','0'),(37,'PD',6,1007,2004,'2020-05-21 09:56:44.647556','2020-05-21 09:56:54.656356','2020-05-21 09:57:02.388855','2020-05-21 09:57:42.936851',5,3,1,'0','0'),(38,'PD',6,1004,2007,'2020-05-21 09:58:00.328983','2020-05-21 09:58:01.254847','2020-05-21 09:58:06.293035','2020-05-21 09:58:26.604197',3,6,1,'0','0'),(39,'PD',6,1004,2008,'2020-05-21 09:58:45.555591','2020-05-21 09:58:54.719159','2020-05-21 09:58:59.755674','2020-05-21 09:59:05.705972',6,4,1,'0','0'),(40,'PD',6,1004,2008,'2020-05-21 09:59:40.831672','2020-05-21 09:59:46.982378','2020-05-21 09:59:52.020575','2020-05-21 10:00:33.530241',6,1,1,'0','0'),(41,'DN',6,1004,2015,'2020-05-21 10:00:51.191036','2020-05-21 10:01:01.708663',NULL,'2020-05-21 10:01:06.740071',1,4,1,'0','0'),(42,'PD',6,1004,2008,'2020-05-21 10:01:36.395239','2020-05-21 10:01:36.427714','2020-05-21 10:01:41.456005','2020-05-21 10:02:01.742667',1,6,1,'0','0'),(43,'PD',6,1004,2018,'2020-05-21 10:02:21.600360','2020-05-21 10:02:29.864850','2020-05-21 10:02:34.909917','2020-05-21 10:02:51.765955',6,3,1,'0','0'),(44,'DN',6,1004,2002,'2020-05-21 10:04:12.018548','2020-05-21 10:04:13.393120',NULL,'2020-05-21 10:04:18.378936',3,6,1,'0','0'),(45,'PD',6,1007,2001,'2020-05-21 10:04:32.099553','2020-05-21 10:04:33.987264','2020-05-21 10:04:39.024148','2020-05-21 10:04:57.232451',3,5,1,'0','0'),(46,'PD',6,1004,2006,'2020-05-21 10:05:17.310736','2020-05-21 10:05:18.657254','2020-05-21 10:05:23.686717','2020-05-21 10:05:44.032463',3,2,1,'0','0'),(47,'PD',6,1004,2011,'2020-05-21 10:06:32.648791','2020-05-21 10:06:33.332435','2020-05-21 10:06:38.368741','2020-05-21 10:06:42.707173',2,5,1,'0','0'),(48,'CN',6,1005,2009,'2020-05-21 10:07:02.930650','2020-05-21 10:07:07.649325',NULL,'2020-05-21 10:07:08.002924',2,1,1,'0','0'),(49,'PD',6,1004,2014,'2020-05-21 10:07:23.079819','2020-05-21 10:07:24.134185','2020-05-21 10:07:29.183980','2020-05-21 10:07:43.234304',2,5,1,'0','0'),(50,'PD',6,1006,2014,'2020-05-21 10:08:18.347065','2020-05-21 10:08:23.140075','2020-05-21 10:08:28.177973','2020-05-21 10:08:48.490133',2,5,1,'0','0'),(51,'PD',6,1006,2014,'2020-05-21 10:09:08.571777','2020-05-21 10:09:23.824404','2020-05-21 10:09:28.849617','2020-05-21 10:10:09.352768',5,3,1,'0','0'),(52,'PD',6,1001,2002,'2020-05-21 10:10:28.991613','2020-05-21 10:10:33.253001','2020-05-21 10:10:35.277254','2020-05-21 10:10:51.753081',3,2,1,'0','0'),(53,'PD',6,1005,2002,'2020-05-21 10:11:09.189589','2020-05-21 10:11:12.865022','2020-05-21 10:11:17.902346','2020-05-21 10:11:49.425816',2,5,1,'0','0'),(54,'CN',6,1005,2002,'2020-05-21 10:12:09.501128','2020-05-21 10:12:22.584273',NULL,'2020-05-21 10:12:24.595392',5,4,1,'0','0'),(55,'DN',6,1005,2002,'2020-05-21 10:12:39.669699','2020-05-21 10:12:50.699047',NULL,'2020-05-21 10:12:55.740484',5,4,1,'0','0'),(56,'PD',6,1005,2016,'2020-05-21 10:13:09.807455','2020-05-21 10:13:23.859117','2020-05-21 10:13:28.902039','2020-05-21 10:14:05.114812',5,4,1,'0','0'),(57,'PD',6,1003,2012,'2020-05-21 10:14:25.202244','2020-05-21 10:14:30.829416','2020-05-21 10:14:38.452877','2020-05-21 10:15:18.992502',4,3,1,'0','0'),(58,'PD',6,1006,2000,'2020-05-21 10:15:35.514028','2020-05-21 10:15:39.932241','2020-05-21 10:15:45.519153','2020-05-21 10:15:45.602039',3,4,1,'0','0'),(59,'PD',6,1007,2004,'2020-05-21 10:16:20.733760','2020-05-21 10:16:21.595427','2020-05-21 10:16:26.632863','2020-05-21 10:16:45.938915',3,4,1,'0','0'),(60,'PD',6,1006,2001,'2020-05-21 10:17:06.027117','2020-05-21 10:17:07.559002','2020-05-21 10:17:14.304035','2020-05-21 10:17:21.119610',3,6,1,'0','0'),(61,'PD',6,1007,2014,'2020-05-21 10:17:41.189981','2020-05-21 10:17:43.178832','2020-05-21 10:17:48.207382','2020-05-21 10:17:51.269614',3,4,1,'0','0'),(62,'CN',6,1003,2012,'2020-05-21 10:18:11.355729','2020-05-21 10:18:16.892091',NULL,'2020-05-21 10:18:21.417269',3,6,1,'0','0'),(63,'CN',6,1007,2004,'2020-05-21 10:18:36.490835','2020-05-21 10:18:40.499912',NULL,'2020-05-21 10:18:41.551858',3,4,1,'0','0'),(64,'PD',6,1006,2018,'2020-05-21 10:18:56.637507','2020-05-21 10:18:58.609716','2020-05-21 10:19:03.647559','2020-05-21 10:19:44.249038',3,4,1,'0','0'),(65,'PD',6,1005,2018,'2020-05-21 10:20:01.994709','2020-05-21 10:20:08.053610','2020-05-21 10:20:13.088666','2020-05-21 10:20:37.225395',4,2,1,'0','0'),(66,'PD',6,1007,2012,'2020-05-21 10:21:12.371679','2020-05-21 10:21:17.454843','2020-05-21 10:21:22.493977','2020-05-21 10:21:47.615998',3,1,1,'0','0'),(67,'PD',6,1007,2009,'2020-05-21 10:22:22.761761','2020-05-21 10:22:28.832266','2020-05-21 10:22:33.876426','2020-05-21 10:23:14.526029',2,1,1,'0','0'),(68,'PD',6,1007,2015,'2020-05-21 10:23:48.296273','2020-05-21 10:23:55.774640','2020-05-21 10:24:00.814843','2020-05-21 10:24:41.398040',1,5,1,'0','0'),(69,'PD',6,1007,2015,'2020-05-21 10:25:13.764948','2020-05-21 10:25:22.629326','2020-05-21 10:25:27.668346','2020-05-21 10:25:28.868750',5,6,1,'0','0'),(70,'DN',6,1007,2002,'2020-05-21 10:25:48.952800','2020-05-21 10:26:02.046110',NULL,'2020-05-21 10:26:07.095150',5,3,1,'0','0'),(71,'CN',6,1007,2002,'2020-05-21 10:26:19.121906','2020-05-21 10:26:35.227786',NULL,'2020-05-21 10:26:39.234159',5,2,1,'0','0'),(72,'PD',6,1007,2015,'2020-05-21 10:26:54.321967','2020-05-21 10:27:03.364217','2020-05-21 10:27:08.407950','2020-05-21 10:27:49.026040',5,4,1,'0','0'),(73,'CN',6,1006,2016,'2020-05-21 10:28:19.756131','2020-05-21 10:28:20.238109',NULL,'2020-05-21 10:28:24.812655',4,6,1,'0','0'),(74,'CN',6,1007,2016,'2020-05-21 10:28:39.899571','2020-05-21 10:28:46.353122',NULL,'2020-05-21 10:28:49.973242',4,6,1,'0','0'),(75,'CN',6,1006,2016,'2020-05-21 10:29:05.054106','2020-05-21 10:29:12.517727',NULL,'2020-05-21 10:29:15.138050',4,3,1,'0','0'),(76,'CN',6,1007,2015,'2020-05-21 10:29:30.436648','2020-05-21 10:29:30.575463',NULL,'2020-05-21 10:29:35.516734',4,3,1,'0','0'),(77,'PD',6,1007,2015,'2020-05-21 10:29:50.596228','2020-05-21 10:29:58.706016','2020-05-21 10:30:03.747033','2020-05-21 10:30:44.335320',4,2,1,'0','0'),(78,'CN',6,1001,2006,'2020-05-21 10:31:01.007642','2020-05-21 10:31:05.966736',NULL,'2020-05-21 10:31:06.068003',2,6,1,'0','0'),(79,'PD',6,1004,2012,'2020-05-21 10:31:21.151803','2020-05-21 10:31:22.106397','2020-05-21 10:31:27.149719','2020-05-21 10:31:47.465033',2,6,1,'0','0'),(80,'DN',6,1004,2019,'2020-05-21 10:32:06.395412','2020-05-21 10:32:15.586424',NULL,'2020-05-21 10:32:20.627921',6,5,1,'0','0'),(81,'PD',6,1004,2007,'2020-05-21 10:32:32.710718','2020-05-21 10:32:44.564553','2020-05-21 10:32:49.622284','2020-05-21 10:33:23.233814',6,4,1,'0','0'),(82,'PD',6,1006,2007,'2020-05-21 10:33:38.384408','2020-05-21 10:33:42.073702','2020-05-21 10:33:47.114687','2020-05-21 10:34:27.730632',4,2,1,'0','0'),(83,'PD',6,1001,2015,'2020-05-21 10:34:43.758035','2020-05-21 10:34:44.810469','2020-05-21 10:34:48.872500','2020-05-21 10:34:53.848570',2,6,1,'0','0'),(84,'PD',6,1003,2004,'2020-05-21 10:35:13.958424','2020-05-21 10:35:14.853096','2020-05-21 10:35:19.887908','2020-05-21 10:36:00.482537',3,2,1,'0','0'),(85,'PD',6,1006,2007,'2020-05-21 10:36:19.365041','2020-05-21 10:36:21.413577','2020-05-21 10:36:26.460229','2020-05-21 10:37:07.104636',2,6,1,'0','0'),(86,'PD',6,1006,2019,'2020-05-21 10:37:39.803261','2020-05-21 10:37:40.296542','2020-05-21 10:37:45.335916','2020-05-21 10:38:26.992127',6,4,1,'0','0'),(87,'PD',6,1004,2019,'2020-05-21 10:38:45.204238','2020-05-21 10:38:46.874656','2020-05-21 10:38:51.886069','2020-05-21 10:39:10.392331',4,6,1,'0','0'),(88,'CN',6,1004,2005,'2020-05-21 10:39:30.495220','2020-05-21 10:39:43.623851',NULL,'2020-05-21 10:39:45.604999',6,5,1,'0','0'),(89,'PD',6,1004,2005,'2020-05-21 10:40:00.687982','2020-05-21 10:40:11.800936','2020-05-21 10:40:16.858716','2020-05-21 10:40:25.849904',6,5,1,'0','0'),(90,'CN',6,1004,2010,'2020-05-21 10:40:46.776901','2020-05-21 10:40:59.909411',NULL,'2020-05-21 10:41:01.903369',1,5,1,'0','0'),(91,'PD',6,1004,2009,'2020-05-21 10:41:16.990239','2020-05-21 10:41:28.075934','2020-05-21 10:41:33.127841','2020-05-21 10:41:57.242417',1,4,1,'0','0'),(92,'PD',6,1004,2009,'2020-05-21 10:42:32.381451','2020-05-21 10:42:38.512511','2020-05-21 10:42:43.568460','2020-05-21 10:43:20.245120',5,6,1,'0','0'),(93,'PD',6,1004,2019,'2020-05-21 10:43:37.766595','2020-05-21 10:43:53.526033','2020-05-21 10:43:58.576525','2020-05-21 10:44:28.100759',6,5,1,'0','0'),(94,'PD',6,1005,2001,'2020-05-21 10:44:52.245923','2020-05-21 10:44:55.872759','2020-05-21 10:45:00.911898','2020-05-21 10:45:41.583732',3,6,1,'0','0'),(95,'PD',6,1005,2009,'2020-05-21 10:46:12.733405','2020-05-21 10:46:14.772876','2020-05-21 10:46:19.814720','2020-05-21 10:46:42.952015',6,2,1,'0','0'),(96,'PD',6,1005,2009,'2020-05-21 10:47:03.046224','2020-05-21 10:47:16.111511','2020-05-21 10:47:21.144351','2020-05-21 10:48:01.764578',4,3,1,'0','0'),(97,'PD',6,1004,2018,'2020-05-21 10:48:18.457743','2020-05-21 10:48:18.964809','2020-05-21 10:48:23.980824','2020-05-21 10:48:43.624889',3,1,1,'0','0'),(98,'PD',6,1004,2018,'2020-05-21 10:49:03.723687','2020-05-21 10:49:16.856505','2020-05-21 10:49:21.901519','2020-05-21 10:49:43.995229',1,6,1,'0','0'),(99,'PD',6,1006,2018,'2020-05-21 10:50:04.099548','2020-05-21 10:50:09.869957','2020-05-21 10:50:16.597005','2020-05-21 10:50:54.457247',4,3,1,'0','0'),(100,'PD',6,1005,2015,'2020-05-21 10:51:29.602789','2020-05-21 10:51:32.126070','2020-05-21 10:51:37.165253','2020-05-21 10:51:44.719336',3,6,1,'0','0'),(101,'PD',6,1001,2019,'2020-05-21 10:52:19.850240','2020-05-21 10:52:19.995694','2020-05-21 10:52:26.059859','2020-05-21 10:52:42.655780',3,2,1,'0','0'),(102,'PD',6,1007,2011,'2020-05-21 10:53:00.078727','2020-05-21 10:53:01.036563','2020-05-21 10:53:06.081627','2020-05-21 10:53:25.251192',2,4,1,'0','0'),(103,'PD',6,1006,2014,'2020-05-21 10:53:45.337510','2020-05-21 10:53:52.480438','2020-05-21 10:53:57.521656','2020-05-21 10:54:15.555908',3,1,1,'0','0'),(104,'PD',6,1001,2006,'2020-05-21 10:54:35.648627','2020-05-21 10:54:37.928036','2020-05-21 10:54:42.899408','2020-05-21 10:54:59.547800',2,1,1,'0','0'),(105,'PD',6,1001,2010,'2020-05-21 10:55:17.536730','2020-05-21 10:55:24.606826','2020-05-21 10:55:28.653856','2020-05-21 10:55:45.381756',1,5,1,'0','0'),(106,'DN',6,1003,2004,'2020-05-21 10:56:17.822440','2020-05-21 10:56:24.801509',NULL,'2020-05-21 10:56:29.842775',2,1,1,'0','0'),(107,'PD',6,1006,2019,'2020-05-21 10:56:42.967832','2020-05-21 10:56:49.413333','2020-05-21 10:56:54.454524','2020-05-21 10:57:35.062637',2,5,1,'0','0'),(108,'PD',6,1001,2002,'2020-05-21 10:57:53.367661','2020-05-21 10:58:00.775166','2020-05-21 10:58:04.830777','2020-05-21 10:58:18.535076',5,6,1,'0','0'),(109,'PD',6,1001,2002,'2020-05-21 10:58:53.685174','2020-05-21 10:59:00.813155','2020-05-21 10:59:04.873737','2020-05-21 10:59:13.893848',6,1,1,'0','0'),(110,'PD',6,1001,2009,'2020-05-21 11:00:34.178017','2020-05-21 11:00:36.538956','2020-05-21 11:00:40.593840','2020-05-21 11:00:57.184991',3,2,1,'0','0'),(111,'PD',6,1003,2004,'2020-05-21 11:01:14.374036','2020-05-21 11:01:15.458940','2020-05-21 11:01:20.503965','2020-05-21 11:02:01.085077',2,1,1,'0','0'),(112,'PD',6,1003,2006,'2020-05-21 11:02:49.897296','2020-05-21 11:02:50.357811','2020-05-21 11:02:55.397804','2020-05-21 11:03:35.960943',1,3,1,'0','0'),(113,'CN',6,1005,2020,'2020-05-21 11:04:10.344807','2020-05-21 11:04:14.028064',NULL,'2020-05-21 11:04:15.401640',3,6,1,'0','0'),(114,'CN',6,1003,2018,'2020-05-21 11:04:30.487076','2020-05-21 11:04:33.291947',NULL,'2020-05-21 11:04:35.543976',3,5,1,'0','0'),(115,'DN',6,1005,2011,'2020-05-21 11:04:50.634286','2020-05-21 11:04:58.276438',NULL,'2020-05-21 11:05:03.312314',3,5,1,'0','0'),(116,'PD',6,1005,2000,'2020-05-21 11:05:30.801773','2020-05-21 11:05:31.446991','2020-05-21 11:05:36.474239','2020-05-21 11:06:17.087654',3,4,1,'0','0'),(117,'PD',6,1004,2016,'2020-05-21 11:06:36.174714','2020-05-21 11:06:41.191615','2020-05-21 11:06:46.601448','2020-05-21 11:07:27.519047',4,6,1,'0','0'),(118,'PD',6,1004,2016,'2020-05-21 11:07:46.588523','2020-05-21 11:08:00.783927','2020-05-21 11:08:05.826587','2020-05-21 11:08:11.763795',6,2,1,'0','0'),(119,'CN',6,1004,2004,'2020-05-21 11:08:31.871227','2020-05-21 11:08:45.023852',NULL,'2020-05-21 11:08:47.019983',1,3,1,'0','0'),(120,'PD',6,1004,2016,'2020-05-21 11:09:17.165614','2020-05-21 11:09:21.244837','2020-05-21 11:09:26.280916','2020-05-21 11:09:46.587751',1,5,1,'0','0'),(121,'DN',6,1001,2014,'2020-05-21 11:10:02.492729','2020-05-21 11:10:11.027363',NULL,'2020-05-21 11:10:13.073975',2,4,1,'0','0'),(122,'PD',6,1001,2014,'2020-05-21 11:10:42.686391','2020-05-21 11:10:45.287871','2020-05-21 11:10:49.335811','2020-05-21 11:11:05.929488',2,4,1,'0','0'),(123,'PD',6,1005,2014,'2020-05-21 11:11:22.903045','2020-05-21 11:11:29.053118','2020-05-21 11:11:34.085685','2020-05-21 11:12:14.694945',4,6,1,'0','0'),(124,'PD',6,1005,2001,'2020-05-21 11:12:33.319345','2020-05-21 11:12:47.884032','2020-05-21 11:12:52.928016','2020-05-21 11:13:33.557862',6,2,1,'0','0'),(125,'CN',6,1005,2001,'2020-05-21 11:13:48.701724','2020-05-21 11:14:06.740847',NULL,'2020-05-21 11:14:08.808971',2,3,1,'0','0'),(126,'PD',6,1005,2001,'2020-05-21 11:14:23.882688','2020-05-21 11:14:34.878249','2020-05-21 11:14:39.911986','2020-05-21 11:14:44.009889',2,5,1,'0','0'),(127,'PD',6,1005,2001,'2020-05-21 11:15:04.098268','2020-05-21 11:15:17.178065','2020-05-21 11:15:22.252049','2020-05-21 11:15:49.373492',2,4,1,'0','0'),(128,'PD',6,1001,2001,'2020-05-21 11:16:09.468519','2020-05-21 11:16:09.873288','2020-05-21 11:16:15.949113','2020-05-21 11:16:32.548245',4,1,1,'0','0'),(129,'PD',6,1001,2004,'2020-05-21 11:16:49.687416','2020-05-21 11:16:56.750291','2020-05-21 11:17:00.801390','2020-05-21 11:17:17.389795',1,4,1,'0','0'),(130,'DN',6,1001,2004,'2020-05-21 11:17:34.935045','2020-05-21 11:17:41.994847',NULL,'2020-05-21 11:17:44.031350',4,2,1,'0','0'),(131,'PD',6,1005,2004,'2020-05-21 11:17:55.024016','2020-05-21 11:17:58.021444','2020-05-21 11:18:03.063741','2020-05-21 11:18:05.114425',4,6,1,'0','0'),(132,'PD',6,1001,2004,'2020-05-21 11:18:25.208470','2020-05-21 11:18:29.339922','2020-05-21 11:18:31.378077','2020-05-21 11:18:48.594958',4,3,1,'0','0'),(133,'PD',6,1003,2011,'2020-05-21 11:19:05.452033','2020-05-21 11:19:06.395550','2020-05-21 11:19:11.434723','2020-05-21 11:19:52.022977',3,4,1,'0','0'),(134,'PD',6,1005,2011,'2020-05-21 11:20:10.867274','2020-05-21 11:20:17.345796','2020-05-21 11:20:22.384456','2020-05-21 11:21:03.008874',4,6,1,'0','0'),(135,'DN',6,1005,2014,'2020-05-21 11:21:21.285038','2020-05-21 11:21:36.176522',NULL,'2020-05-21 11:21:41.221390',6,2,1,'0','0'),(136,'PD',6,1005,2014,'2020-05-21 11:21:51.430973','2020-05-21 11:22:09.357079','2020-05-21 11:22:14.401670','2020-05-21 11:22:36.713874',6,1,1,'0','0'),(137,'PD',6,1001,2006,'2020-05-21 11:22:56.854949','2020-05-21 11:22:57.445069','2020-05-21 11:23:03.501806','2020-05-21 11:23:20.103503',3,5,1,'0','0'),(138,'PD',6,1006,2016,'2020-05-21 11:23:37.105013','2020-05-21 11:23:38.148887','2020-05-21 11:23:43.187489','2020-05-21 11:24:22.420769',5,2,1,'0','0'),(139,'PD',6,1006,2009,'2020-05-21 11:24:42.520035','2020-05-21 11:24:55.615250','2020-05-21 11:25:00.656234','2020-05-21 11:25:12.725549',2,3,1,'0','0'),(140,'PD',6,1006,2016,'2020-05-21 11:25:32.844422','2020-05-21 11:25:45.900790','2020-05-21 11:25:50.944411','2020-05-21 11:26:08.085297',2,6,1,'0','0'),(141,'CN',6,1003,2016,'2020-05-21 11:26:28.186862','2020-05-21 11:26:36.235514',NULL,'2020-05-21 11:26:40.032030',4,5,1,'0','0'),(142,'PD',6,1003,2016,'2020-05-21 11:27:10.158607','2020-05-21 11:27:12.416097','2020-05-21 11:27:17.452980','2020-05-21 11:27:58.040133',4,6,1,'0','0'),(143,'CN',6,1003,2016,'2020-05-21 11:28:15.584812','2020-05-21 11:28:31.219849',NULL,'2020-05-21 11:28:35.701184',6,1,1,'0','0'),(144,'CN',6,1003,2011,'2020-05-21 11:28:50.775527','2020-05-21 11:28:59.385504',NULL,'2020-05-21 11:29:00.845971',6,3,1,'0','0'),(145,'PD',6,1003,2011,'2020-05-21 11:29:30.973258','2020-05-21 11:29:35.555650','2020-05-21 11:29:40.599623','2020-05-21 11:30:22.494972',6,5,1,'0','0'),(146,'PD',6,1004,2019,'2020-05-21 11:30:41.373900','2020-05-21 11:30:43.687824','2020-05-21 11:30:48.727487','2020-05-21 11:31:09.018239',5,4,1,'0','0'),(147,'CN',6,1006,2019,'2020-05-21 11:31:26.624790','2020-05-21 11:31:30.836283',NULL,'2020-05-21 11:31:31.677943',4,2,1,'0','0'),(148,'PD',6,1004,2019,'2020-05-21 11:32:01.898331','2020-05-21 11:32:03.828848','2020-05-21 11:32:08.871362','2020-05-21 11:32:29.203704',4,3,1,'0','0'),(149,'PD',6,1005,2014,'2020-05-21 11:32:47.147969','2020-05-21 11:32:49.341352','2020-05-21 11:32:54.376695','2020-05-21 11:33:36.105275',3,2,1,'0','0'),(150,'PD',6,1005,2014,'2020-05-21 11:34:07.607401','2020-05-21 11:34:09.277957','2020-05-21 11:34:14.329018','2020-05-21 11:34:37.814985',2,4,1,'0','0'),(151,'PD',6,1004,2004,'2020-05-21 11:34:57.901170','2020-05-21 11:34:57.993332','2020-05-21 11:35:03.025748','2020-05-21 11:35:23.318387',3,2,1,'0','0'),(152,'DN',6,1004,2004,'2020-05-21 11:35:58.244110','2020-05-21 11:35:59.477152',NULL,'2020-05-21 11:36:01.267355',2,5,1,'0','0'),(153,'PD',6,1004,2009,'2020-05-21 11:36:13.335790','2020-05-21 11:36:27.618652','2020-05-21 11:36:32.658977','2020-05-21 11:36:52.973346',2,5,1,'0','0'),(154,'PD',6,1004,2009,'2020-05-21 11:37:38.748574','2020-05-21 11:37:39.014755','2020-05-21 11:37:44.066219','2020-05-21 11:38:04.518612',5,6,1,'0','0'),(155,'CN',6,1004,2009,'2020-05-21 11:38:24.028311','2020-05-21 11:38:32.686183',NULL,'2020-05-21 11:38:34.090769',6,4,1,'0','0'),(156,'PD',6,1004,2016,'2020-05-21 11:38:49.161468','2020-05-21 11:39:00.825529','2020-05-21 11:39:05.866893','2020-05-21 11:39:24.385847',6,4,1,'0','0'),(157,'PD',6,1006,2016,'2020-05-21 11:39:44.475550','2020-05-21 11:39:44.654735','2020-05-21 11:39:49.703436','2020-05-21 11:40:14.761212',4,3,1,'0','0'),(158,'PD',6,1007,2020,'2020-05-21 11:40:34.863334','2020-05-21 11:40:39.107923','2020-05-21 11:40:44.148947','2020-05-21 11:41:10.114789',3,1,1,'0','0'),(159,'DN',6,1007,2020,'2020-05-21 11:41:45.278901','2020-05-21 11:41:51.341764',NULL,'2020-05-21 11:41:56.384341',1,4,1,'0','0'),(160,'PD',6,1007,2020,'2020-05-21 11:42:10.416770','2020-05-21 11:42:24.511582','2020-05-21 11:42:29.550054','2020-05-21 11:43:10.169475',1,2,1,'0','0'),(161,'PD',6,1007,2004,'2020-05-21 11:43:25.871801','2020-05-21 11:43:51.384684','2020-05-21 11:43:56.448139','2020-05-21 11:44:37.074951',2,6,1,'0','0'),(162,'PD',6,1007,2004,'2020-05-21 11:44:56.355713','2020-05-21 11:45:10.276925','2020-05-21 11:45:15.319946','2020-05-21 11:45:59.730033',6,5,1,'0','0'),(163,'PD',6,1007,2006,'2020-05-21 11:46:31.876694','2020-05-21 11:46:32.919877','2020-05-21 11:46:37.955115','2020-05-21 11:47:18.551945',5,3,1,'0','0'),(164,'CN',6,1005,2006,'2020-05-21 11:47:37.259606','2020-05-21 11:47:39.441780',NULL,'2020-05-21 11:47:42.311002',3,4,1,'0','0'),(165,'PD',6,1007,2016,'2020-05-21 11:47:57.391204','2020-05-21 11:47:59.797720','2020-05-21 11:48:04.828817','2020-05-21 11:48:27.608195',3,1,1,'0','0'),(166,'PD',6,1007,2016,'2020-05-21 11:48:47.704574','2020-05-21 11:49:00.763389','2020-05-21 11:49:05.806297','2020-05-21 11:49:27.980615',2,5,1,'0','0'),(167,'PD',6,1003,2004,'2020-05-21 11:49:48.198482','2020-05-21 11:49:48.656494','2020-05-21 11:49:53.697607','2020-05-21 11:50:03.309115',5,6,1,'0','0'),(168,'PD',6,1005,2019,'2020-05-21 11:50:23.398327','2020-05-21 11:50:24.341976','2020-05-21 11:50:29.381503','2020-05-21 11:51:09.987705',3,4,1,'0','0'),(169,'PD',6,1004,2019,'2020-05-21 11:51:28.794354','2020-05-21 11:51:28.929143','2020-05-21 11:51:33.976735','2020-05-21 11:52:09.056893',4,3,1,'0','0'),(170,'PD',6,1006,2002,'2020-05-21 11:52:29.153591','2020-05-21 11:52:36.133762','2020-05-21 11:52:41.179112','2020-05-21 11:53:21.787086',3,6,1,'0','0'),(171,'PD',6,1006,2002,'2020-05-21 11:53:39.587894','2020-05-21 11:53:54.944223','2020-05-21 11:53:59.989238','2020-05-21 11:54:14.797922',6,1,1,'0','0'),(172,'CN',6,1006,2009,'2020-05-21 11:54:34.889698','2020-05-21 11:54:47.971970',NULL,'2020-05-21 11:54:49.985990',6,1,1,'0','0'),(173,'CN',6,1006,2009,'2020-05-21 11:55:35.170839','2020-05-21 11:55:40.255583',NULL,'2020-05-21 11:55:45.250725',6,4,1,'0','0'),(174,'PD',6,1006,2009,'2020-05-21 11:56:00.334639','2020-05-21 11:56:08.397939','2020-05-21 11:56:13.440113','2020-05-21 11:56:30.546200',6,5,1,'0','0'),(175,'PD',6,1005,2009,'2020-05-21 11:56:50.637765','2020-05-21 11:56:59.935095','2020-05-21 11:57:04.969845','2020-05-21 11:57:45.570153',4,5,1,'0','0'),(176,'PD',6,1001,2016,'2020-05-21 11:58:01.068268','2020-05-21 11:58:02.329740','2020-05-21 11:58:06.378030','2020-05-21 11:58:11.142856',5,6,1,'0','0'),(177,'PD',6,1006,2016,'2020-05-21 11:58:31.240273','2020-05-21 11:58:32.245204','2020-05-21 11:58:37.284391','2020-05-21 11:59:11.537119',4,5,1,'0','0'),(178,'PD',6,1007,2016,'2020-05-21 11:59:31.622521','2020-05-21 11:59:34.054609','2020-05-21 11:59:39.102397','2020-05-21 11:59:46.758204',5,3,1,'0','0'),(179,'PD',6,1006,2009,'2020-05-21 12:00:06.855195','2020-05-21 12:00:08.864723','2020-05-21 12:00:13.901838','2020-05-21 12:00:21.979867',5,6,1,'0','0'),(180,'PD',6,1004,2018,'2020-05-21 12:00:57.148722','2020-05-21 12:01:02.275306','2020-05-21 12:01:07.319858','2020-05-21 12:01:48.176466',3,6,1,'0','0'),(181,'PD',6,1004,2018,'2020-05-21 12:02:07.628804','2020-05-21 12:02:21.415346','2020-05-21 12:02:26.441170','2020-05-21 12:03:07.309771',6,3,1,'0','0'),(182,'PD',6,1006,2019,'2020-05-21 12:03:23.074586','2020-05-21 12:03:28.151357','2020-05-21 12:03:33.182289','2020-05-21 12:04:13.785220',3,2,1,'0','0'),(183,'PD',6,1006,2020,'2020-05-21 12:04:33.504021','2020-05-21 12:04:46.971282','2020-05-21 12:04:52.014158','2020-05-21 12:05:28.887046',2,5,1,'0','0'),(184,'PD',6,1005,2010,'2020-05-21 12:05:48.980205','2020-05-21 12:05:49.605467','2020-05-21 12:05:54.639108','2020-05-21 12:06:04.099175',5,4,1,'0','0'),(185,'PD',6,1006,2016,'2020-05-21 12:06:39.285548','2020-05-21 12:06:42.337822','2020-05-21 12:06:47.387980','2020-05-21 12:07:28.003929',5,1,1,'0','0'),(186,'CN',6,1006,2016,'2020-05-21 12:07:59.726273','2020-05-21 12:08:17.291933',NULL,'2020-05-21 12:08:19.838926',1,3,1,'0','0'),(187,'PD',6,1006,2001,'2020-05-21 12:08:34.908554','2020-05-21 12:08:45.420909','2020-05-21 12:08:50.455561','2020-05-21 12:09:00.087616',1,4,1,'0','0'),(188,'PD',6,1006,2016,'2020-05-21 12:09:20.184960','2020-05-21 12:09:33.255129','2020-05-21 12:09:38.299104','2020-05-21 12:09:40.305322',1,3,1,'0','0'),(189,'DN',6,1006,2001,'2020-05-21 12:10:15.464720','2020-05-21 12:10:21.533390',NULL,'2020-05-21 12:10:26.569099',1,6,1,'0','0'),(190,'CN',6,1006,2016,'2020-05-21 12:10:40.594409','2020-05-21 12:10:54.707853',NULL,'2020-05-21 12:10:55.696159',1,3,1,'0','0'),(191,'PD',6,1006,2001,'2020-05-21 12:11:25.826890','2020-05-21 12:11:30.886646','2020-05-21 12:11:35.919155','2020-05-21 12:12:16.494935',1,4,1,'0','0'),(192,'CN',6,1001,2001,'2020-05-21 12:12:36.249321','2020-05-21 12:12:45.429862',NULL,'2020-05-21 12:12:46.319574',4,2,1,'0','0'),(193,'DN',6,1006,2001,'2020-05-21 12:13:01.398011','2020-05-21 12:13:05.757448',NULL,'2020-05-21 12:13:10.804521',4,1,1,'0','0'),(194,'PD',6,1001,2001,'2020-05-21 12:13:21.531908','2020-05-21 12:13:25.698499','2020-05-21 12:13:28.476963','2020-05-21 12:13:51.114010',4,6,1,'0','0'),(195,'PD',6,1001,2002,'2020-05-21 12:14:21.846972','2020-05-21 12:14:25.371931','2020-05-21 12:14:27.410475','2020-05-21 12:14:43.972672',6,2,1,'0','0'),(196,'PD',6,1001,2019,'2020-05-21 12:15:02.059104','2020-05-21 12:15:09.117710','2020-05-21 12:15:13.170680','2020-05-21 12:15:29.779030',2,4,1,'0','0'),(197,'PD',6,1006,2019,'2020-05-21 12:15:47.288050','2020-05-21 12:15:52.735814','2020-05-21 12:15:57.768763','2020-05-21 12:16:37.623121',4,2,1,'0','0'),(198,'PD',6,1006,2002,'2020-05-21 12:16:57.713988','2020-05-21 12:17:10.780587','2020-05-21 12:17:15.818294','2020-05-21 12:17:22.861010',2,5,1,'0','0'),(199,'PD',6,1006,2019,'2020-05-21 12:17:42.958869','2020-05-21 12:17:56.023893','2020-05-21 12:18:01.063542','2020-05-21 12:18:13.157467',2,5,1,'0','0'),(200,'PD',6,1006,2019,'2020-05-21 12:18:33.261242','2020-05-21 12:18:46.315468','2020-05-21 12:18:51.361986','2020-05-21 12:19:28.663184',2,1,1,'0','0'),(201,'DN',6,1006,2019,'2020-05-21 12:19:48.744205','2020-05-21 12:20:01.805770',NULL,'2020-05-21 12:20:06.849326',1,6,1,'0','0'),(202,'PD',6,1006,2019,'2020-05-21 12:20:33.927079','2020-05-21 12:20:34.981173','2020-05-21 12:20:40.020031','2020-05-21 12:21:20.694218',1,5,1,'0','0'),(203,'PD',6,1005,2020,'2020-05-21 12:21:39.328899','2020-05-21 12:21:40.753251','2020-05-21 12:21:45.785326','2020-05-21 12:22:26.403517',5,4,1,'0','0'),(204,'CN',6,1001,2020,'2020-05-21 12:22:59.789059','2020-05-21 12:23:04.914050',NULL,'2020-05-21 12:23:09.856310',4,3,1,'0','0'),(205,'PD',6,1001,2020,'2020-05-21 12:23:24.937281','2020-05-21 12:23:31.075846','2020-05-21 12:23:36.124228','2020-05-21 12:23:52.701777',4,3,1,'0','0'),(206,'PD',6,1004,2015,'2020-05-21 12:24:10.168410','2020-05-21 12:24:13.146902','2020-05-21 12:24:18.171255','2020-05-21 12:24:20.247097',3,5,1,'0','0'),(207,'PD',6,1003,2020,'2020-05-21 12:24:40.344362','2020-05-21 12:24:40.915390','2020-05-21 12:24:45.945747','2020-05-21 12:25:20.650241',3,6,1,'0','0'),(208,'CN',6,1003,2020,'2020-05-21 12:25:56.800106','2020-05-21 12:26:02.874882',NULL,'2020-05-21 12:26:06.890934',6,3,1,'0','0'),(209,'PD',6,1003,2020,'2020-05-21 12:26:52.072318','2020-05-21 12:26:56.266380','2020-05-21 12:27:01.311511','2020-05-21 12:27:32.366347',6,4,1,'0','0'),(210,'PD',6,1003,2020,'2020-05-21 12:27:52.468012','2020-05-21 12:28:05.525673','2020-05-21 12:28:10.569513','2020-05-21 12:28:51.174380',4,2,1,'0','0'),(211,'PD',6,1003,2002,'2020-05-21 12:29:07.907780','2020-05-21 12:29:24.362461','2020-05-21 12:29:29.408653','2020-05-21 12:30:10.004297',2,6,1,'0','0'),(212,'PD',6,1003,2002,'2020-05-21 12:30:28.370303','2020-05-21 12:30:51.234770','2020-05-21 12:30:56.284411','2020-05-21 12:31:28.726298',6,2,1,'0','0'),(213,'PD',6,1001,2006,'2020-05-21 12:31:48.810974','2020-05-21 12:31:53.770962','2020-05-21 12:31:55.841051','2020-05-21 12:32:12.439792',3,5,1,'0','0'),(214,'PD',6,1007,2019,'2020-05-21 12:32:29.072006','2020-05-21 12:32:36.169449','2020-05-21 12:32:41.210693','2020-05-21 12:33:21.856832',5,2,1,'0','0'),(215,'PD',6,1007,2019,'2020-05-21 12:33:39.451165','2020-05-21 12:34:11.132346','2020-05-21 12:34:16.172660','2020-05-21 12:34:54.929445',2,6,1,'0','0'),(216,'PD',6,1007,2001,'2020-05-21 12:35:15.025206','2020-05-21 12:35:28.106535','2020-05-21 12:35:33.168403','2020-05-21 12:36:13.823771',6,2,1,'0','0'),(217,'PD',6,1007,2001,'2020-05-21 12:36:45.518687','2020-05-21 12:36:55.048407','2020-05-21 12:37:00.094421','2020-05-21 12:37:30.820020',2,1,1,'0','0'),(218,'CN',6,1007,2001,'2020-05-21 12:37:51.052513','2020-05-21 12:38:04.143651',NULL,'2020-05-21 12:38:06.156427',1,4,1,'0','0'),(219,'PD',6,1007,2001,'2020-05-21 12:38:21.462124','2020-05-21 12:38:32.722046','2020-05-21 12:38:37.761324','2020-05-21 12:39:16.818544',1,3,1,'0','0'),(220,'PD',6,1003,2014,'2020-05-21 12:39:36.916020','2020-05-21 12:39:43.128712','2020-05-21 12:39:48.167818','2020-05-21 12:40:28.818221',3,6,1,'0','0'),(221,'PD',6,1003,2014,'2020-05-21 12:40:47.316076','2020-05-21 12:41:01.997232','2020-05-21 12:41:07.041483','2020-05-21 12:41:47.748762',6,2,1,'0','0'),(222,'CN',6,1003,2020,'2020-05-21 12:42:07.832758','2020-05-21 12:42:20.955933',NULL,'2020-05-21 12:42:22.953186',2,3,1,'0','0'),(223,'PD',6,1003,2020,'2020-05-21 12:42:38.035490','2020-05-21 12:42:49.113863','2020-05-21 12:42:54.154989','2020-05-21 12:43:23.339963',2,6,1,'0','0'),(224,'PD',6,1003,2020,'2020-05-21 12:43:43.443447','2020-05-21 12:43:56.502467','2020-05-21 12:44:01.546956','2020-05-21 12:44:18.672115',6,4,1,'0','0'),(225,'PD',6,1003,2019,'2020-05-21 12:44:53.837979','2020-05-21 12:44:59.909343','2020-05-21 12:45:04.952416','2020-05-21 12:45:39.131537',6,4,1,'0','0'),(226,'PD',6,1005,2019,'2020-05-21 12:45:59.250217','2020-05-21 12:45:59.615287','2020-05-21 12:46:10.320268','2020-05-21 12:46:30.519145',4,5,1,'0','0'),(227,'PD',6,1003,2019,'2020-05-21 12:47:05.659697','2020-05-21 12:47:08.637211','2020-05-21 12:47:13.679286','2020-05-21 12:47:25.852549',4,2,1,'0','0'),(228,'PD',6,1005,2019,'2020-05-21 12:47:45.950319','2020-05-21 12:47:54.025498','2020-05-21 12:47:59.069576','2020-05-21 12:48:39.681718',4,3,1,'0','0'),(229,'PD',6,1007,2019,'2020-05-21 12:48:56.392927','2020-05-21 12:48:57.395257','2020-05-21 12:49:02.438738','2020-05-21 12:49:21.611505',3,5,1,'0','0'),(230,'PD',6,1005,2001,'2020-05-21 12:49:41.711680','2020-05-21 12:49:45.056596','2020-05-21 12:49:50.100451','2020-05-21 12:50:16.955752',3,4,1,'0','0'),(231,'DN',6,1003,2001,'2020-05-21 12:50:37.050270','2020-05-21 12:50:44.998904',NULL,'2020-05-21 12:50:50.040409',4,6,1,'0','0'),(232,'PD',6,1005,2001,'2020-05-21 12:51:02.196299','2020-05-21 12:51:09.264770','2020-05-21 12:51:14.304515','2020-05-21 12:51:22.365856',4,5,1,'0','0'),(233,'PD',6,1003,2001,'2020-05-21 12:51:42.465926','2020-05-21 12:51:43.311328','2020-05-21 12:51:48.351566','2020-05-21 12:52:12.723929',4,2,1,'0','0'),(234,'DN',6,1007,2019,'2020-05-21 12:52:32.841859','2020-05-21 12:52:35.897640',NULL,'2020-05-21 12:52:40.932259',3,1,1,'0','0'),(235,'PD',6,1003,2001,'2020-05-21 12:52:52.958626','2020-05-21 12:52:53.954897','2020-05-21 12:52:58.993045','2020-05-21 12:53:03.051653',3,5,1,'0','0'),(236,'PD',6,1007,2018,'2020-05-21 12:53:23.155054','2020-05-21 12:53:25.145980','2020-05-21 12:53:30.179141','2020-05-21 12:53:38.277134',3,1,1,'0','0'),(237,'CN',6,1001,2006,'2020-05-21 12:54:13.427687','2020-05-21 12:54:15.385114',NULL,'2020-05-21 12:54:18.484514',5,3,1,'0','0'),(238,'PD',6,1006,2006,'2020-05-21 12:54:33.564763','2020-05-21 12:54:33.681473','2020-05-21 12:54:38.709096','2020-05-21 12:54:43.660809',5,1,1,'0','0'),(239,'PD',6,1001,2010,'2020-05-21 12:55:03.762624','2020-05-21 12:55:04.776410','2020-05-21 12:55:08.818993','2020-05-21 12:55:29.034434',5,6,1,'0','0'),(240,'PD',6,1001,2010,'2020-05-21 12:55:49.013859','2020-05-21 12:55:56.119995','2020-05-21 12:56:00.173355','2020-05-21 12:56:16.785991',6,5,1,'0','0'),(241,'PD',6,1004,2004,'2020-05-21 12:56:34.266176','2020-05-21 12:56:39.800896','2020-05-21 12:56:44.851654','2020-05-21 12:57:07.513281',3,1,1,'0','0'),(242,'PD',6,1004,2004,'2020-05-21 12:57:24.542879','2020-05-21 12:57:35.654984','2020-05-21 12:57:40.687267','2020-05-21 12:58:01.016769',1,3,1,'0','0'),(243,'PD',6,1003,2004,'2020-05-21 12:58:19.834379','2020-05-21 12:58:26.131688','2020-05-21 12:58:31.179121','2020-05-21 12:58:39.977384',3,1,1,'0','0'),(244,'PD',6,1007,2010,'2020-05-21 12:59:00.065598','2020-05-21 12:59:01.405116','2020-05-21 12:59:06.440932','2020-05-21 12:59:47.072554',5,4,1,'0','0'),(245,'PD',6,1005,2010,'2020-05-21 13:00:05.462257','2020-05-21 13:00:11.438367','2020-05-21 13:00:16.480720','2020-05-21 13:00:57.126295',4,3,1,'0','0'),(246,'PD',6,1004,2010,'2020-05-21 13:01:15.874421','2020-05-21 13:01:18.220285','2020-05-21 13:01:23.264781','2020-05-21 13:01:43.579223',3,4,1,'0','0'),(247,'PD',6,1007,2010,'2020-05-21 13:02:01.143037','2020-05-21 13:02:06.866569','2020-05-21 13:02:11.907736','2020-05-21 13:02:53.623163',4,6,1,'0','0'),(248,'PD',6,1007,2020,'2020-05-21 13:03:11.578598','2020-05-21 13:03:28.088455','2020-05-21 13:03:33.129476','2020-05-21 13:04:13.788734',6,2,1,'0','0'),(249,'PD',6,1007,2020,'2020-05-21 13:04:32.035845','2020-05-21 13:04:46.980760','2020-05-21 13:04:52.018366','2020-05-21 13:05:32.652346',2,4,1,'0','0'),(250,'PD',6,1004,2020,'2020-05-21 13:05:52.529920','2020-05-21 13:05:59.053380','2020-05-21 13:06:03.580219','2020-05-21 13:06:23.914955',4,6,1,'0','0'),(251,'PD',6,1004,2020,'2020-05-21 13:06:42.817882','2020-05-21 13:06:52.052442','2020-05-21 13:06:57.089182','2020-05-21 13:07:08.041001',6,1,1,'0','0'),(252,'DN',6,1005,2001,'2020-05-21 13:07:43.198792','2020-05-21 13:07:49.046175',NULL,'2020-05-21 13:07:54.085295',3,5,1,'0','0'),(253,'PD',6,1005,2002,'2020-05-21 13:08:08.330395','2020-05-21 13:08:22.213698','2020-05-21 13:08:27.258840','2020-05-21 13:09:07.877122',3,2,1,'0','0'),(254,'PD',6,1005,2002,'2020-05-21 13:09:38.807589','2020-05-21 13:09:41.040398','2020-05-21 13:09:46.088265','2020-05-21 13:10:19.090736',2,5,1,'0','0'),(255,'CN',6,1001,2006,'2020-05-21 13:10:39.205071','2020-05-21 13:10:43.937120',NULL,'2020-05-21 13:10:44.268660',5,2,1,'0','0'),(256,'PD',6,1003,2002,'2020-05-21 13:11:00.390903','2020-05-21 13:11:02.205640','2020-05-21 13:11:07.249952','2020-05-21 13:11:47.888946',5,3,1,'0','0'),(257,'PD',6,1003,2001,'2020-05-21 13:12:05.799960','2020-05-21 13:12:21.085575','2020-05-21 13:12:26.128999','2020-05-21 13:13:06.188184',3,2,1,'0','0'),(258,'PD',6,1004,2020,'2020-05-21 13:13:26.285088','2020-05-21 13:13:27.800190','2020-05-21 13:13:32.835047','2020-05-21 13:13:53.165142',2,5,1,'0','0'),(259,'PD',6,1001,2020,'2020-05-21 13:14:11.550920','2020-05-21 13:14:12.767156','2020-05-21 13:14:16.807613','2020-05-21 13:14:33.389023',5,6,1,'0','0'),(260,'DN',6,1001,2010,'2020-05-21 13:14:51.767566','2020-05-21 13:14:58.823993',NULL,'2020-05-21 13:15:02.876612',6,2,1,'0','0'),(261,'PD',6,1001,2010,'2020-05-21 13:15:31.944104','2020-05-21 13:15:35.086604','2020-05-21 13:15:37.120809','2020-05-21 13:15:53.724034',6,1,1,'0','0'),(262,'DN',6,1001,2016,'2020-05-21 13:16:12.156496','2020-05-21 13:16:21.171347',NULL,'2020-05-21 13:16:23.216313',1,4,1,'0','0'),(263,'PD',6,1001,2010,'2020-05-21 13:16:52.368950','2020-05-21 13:17:00.486095','2020-05-21 13:17:02.525085','2020-05-21 13:17:19.101923',1,6,1,'0','0'),(264,'PD',6,1001,2020,'2020-05-21 13:17:37.638537','2020-05-21 13:17:44.701151','2020-05-21 13:17:48.757121','2020-05-21 13:18:05.337365',6,3,1,'0','0'),(265,'PD',6,1001,2015,'2020-05-21 13:18:22.896128','2020-05-21 13:18:29.944770','2020-05-21 13:18:33.992276','2020-05-21 13:18:50.605368',3,6,1,'0','0'),(266,'PD',6,1001,2010,'2020-05-21 13:19:08.140534','2020-05-21 13:19:15.226807','2020-05-21 13:19:19.281748','2020-05-21 13:19:38.306712',6,2,1,'0','0'),(267,'TO',6,0,0,'2020-05-21 13:20:08.443059',NULL,NULL,NULL,3,6,1,'0','0'),(268,'TO',6,0,0,'2020-05-21 13:23:29.242989',NULL,NULL,NULL,3,1,1,'0','0'),(269,'TO',6,0,0,'2020-05-21 13:26:50.017965',NULL,NULL,NULL,3,2,1,'0','0'),(270,'TO',6,0,0,'2020-05-21 13:30:05.787141',NULL,NULL,NULL,3,4,1,'0','0'),(271,'TO',6,0,0,'2020-05-21 13:33:17.798920',NULL,NULL,NULL,3,2,1,'0','0'),(272,'TO',6,0,0,'2020-05-21 13:36:38.580673',NULL,NULL,NULL,3,6,1,'0','0'),(273,'TO',6,0,0,'2020-05-21 13:40:14.399638',NULL,NULL,NULL,3,4,1,'0','0'),(274,'TO',6,0,0,'2020-05-21 13:43:30.155076',NULL,NULL,NULL,3,5,1,'0','0'),(275,'TO',6,0,0,'2020-05-21 13:46:50.971427',NULL,NULL,NULL,3,1,1,'0','0'),(276,'TO',6,0,0,'2020-05-21 13:50:11.761801',NULL,NULL,NULL,3,6,1,'0','0'),(277,'TO',6,0,0,'2020-05-21 13:53:47.605409',NULL,NULL,NULL,3,1,1,'0','0'),(278,'TO',6,0,0,'2020-05-21 13:57:03.385743',NULL,NULL,NULL,3,6,1,'0','0'),(279,'TO',6,0,0,'2020-05-21 14:00:24.174846',NULL,NULL,NULL,3,2,1,'0','0'),(280,'TO',6,0,0,'2020-05-21 14:03:46.003705',NULL,NULL,NULL,3,2,1,'0','0'),(281,'TO',6,0,0,'2020-05-21 14:07:01.772593',NULL,NULL,NULL,3,4,1,'0','0'),(282,'TO',6,0,0,'2020-05-21 14:10:22.555287',NULL,NULL,NULL,3,1,1,'0','0'),(283,'TO',6,0,0,'2020-05-21 14:13:43.316176',NULL,NULL,NULL,3,5,1,'0','0'),(284,'TO',6,0,0,'2020-05-21 14:17:04.102964',NULL,NULL,NULL,3,2,1,'0','0'),(285,'TO',6,0,0,'2020-05-21 14:20:24.897577',NULL,NULL,NULL,3,1,1,'0','0'),(286,'TO',6,0,0,'2020-05-21 14:23:45.664867',NULL,NULL,NULL,3,2,1,'0','0'),(287,'TO',6,0,0,'2020-05-21 14:27:06.438214',NULL,NULL,NULL,3,4,1,'0','0'),(288,'TO',6,0,0,'2020-05-21 14:30:27.230184',NULL,NULL,NULL,3,2,1,'0','0'),(289,'TO',6,0,0,'2020-05-21 14:34:03.059121',NULL,NULL,NULL,3,2,1,'0','0'),(290,'TO',6,0,0,'2020-05-21 14:37:18.815834',NULL,NULL,NULL,3,1,1,'0','0'),(291,'TO',6,0,0,'2020-05-21 14:40:39.597986',NULL,NULL,NULL,3,6,1,'0','0'),(292,'TO',6,0,0,'2020-05-21 14:44:00.397426',NULL,NULL,NULL,3,1,1,'0','0'),(293,'TO',6,0,0,'2020-05-21 14:47:16.158030',NULL,NULL,NULL,3,5,1,'0','0'),(294,'TO',6,0,0,'2020-05-21 14:50:26.943177',NULL,NULL,NULL,3,5,1,'0','0'),(295,'TO',6,0,0,'2020-05-21 14:53:37.685377',NULL,NULL,NULL,3,5,1,'0','0'),(296,'TO',6,0,0,'2020-05-21 14:57:13.499756',NULL,NULL,NULL,3,5,1,'0','0'),(297,'TO',6,0,0,'2020-05-21 15:00:46.205613',NULL,NULL,NULL,3,4,1,'0','0'),(298,'TO',6,0,0,'2020-05-21 15:04:01.940128',NULL,NULL,NULL,3,4,1,'0','0'),(299,'TO',6,0,0,'2020-05-21 15:07:22.625957',NULL,NULL,NULL,3,4,1,'0','0'),(300,'TO',6,0,0,'2020-05-21 15:10:43.433687',NULL,NULL,NULL,3,2,1,'0','0'),(301,'TO',6,0,0,'2020-05-21 15:14:04.193166',NULL,NULL,NULL,3,6,1,'0','0'),(302,'TO',6,0,0,'2020-05-21 15:17:25.032157',NULL,NULL,NULL,3,1,1,'0','0'),(303,'TO',6,0,0,'2020-05-21 15:20:40.773875',NULL,NULL,NULL,3,2,1,'0','0'),(304,'TO',6,0,0,'2020-05-21 15:23:51.520262',NULL,NULL,NULL,3,6,1,'0','0'),(305,'TO',6,0,0,'2020-05-21 15:27:13.516318',NULL,NULL,NULL,3,2,1,'0','0'),(306,'TO',6,0,0,'2020-05-21 15:30:34.242901',NULL,NULL,NULL,3,1,1,'0','0'),(307,'TO',6,0,0,'2020-05-21 15:34:10.100537',NULL,NULL,NULL,3,6,1,'0','0'),(308,'TO',6,0,0,'2020-05-21 15:37:25.840305',NULL,NULL,NULL,3,5,1,'0','0'),(309,'TO',6,0,0,'2020-05-21 15:40:46.612472',NULL,NULL,NULL,3,5,1,'0','0'),(310,'TO',6,0,0,'2020-05-21 15:44:07.396058',NULL,NULL,NULL,3,1,1,'0','0'),(311,'TO',6,0,0,'2020-05-21 15:47:58.240852',NULL,NULL,NULL,3,2,1,'0','0'),(312,'TO',6,0,0,'2020-05-21 15:52:05.008557',NULL,NULL,NULL,3,1,1,'0','0'),(313,'TO',6,0,0,'2020-05-21 15:55:20.778308',NULL,NULL,NULL,3,1,1,'0','0'),(314,'RQ',6,0,0,'2020-05-21 15:59:26.668156',NULL,NULL,NULL,3,6,1,'0','0');
/*!40000 ALTER TABLE `trip` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `an` bigint(20) NOT NULL,
  `pn` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `auth` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL,
  `pid` int(11) DEFAULT NULL,
  `tid` int(11) NOT NULL,
  `name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gdr` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `age` smallint(6) DEFAULT NULL,
  `dl` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hs` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`an`),
  KEY `user_pn_22e36341` (`pn`),
  KEY `user_auth_89344e9d` (`auth`),
  KEY `user_pid_9485f0dc` (`pid`),
  KEY `user_tid_3d7f6abc` (`tid`),
  KEY `user_name_14c4b06b` (`name`),
  KEY `user_gdr_e9a7628f` (`gdr`),
  KEY `user_age_9380f8f8` (`age`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (0,'9700000000','auth00',4,-1,'John 00','MALE',25,'UK-123456789001','UK'),(1,'9700000001','auth01',2,2,'John 01','MALE',25,'UK-123456789001','UK'),(2,'9700000002','auth02',2,1,'John 02','MALE',25,'UK-123456789002','UK'),(3,'9700000003','auth03',2,-1,'John 03','MALE',25,'UK-123456789003','UK'),(4,'9700000004','auth04',3,-1,'John 04','MALE',25,'UK-123456789004','UK'),(5,'9700000005','auth05',2,-1,'John 05','MALE',25,'UK-123456789005','UK'),(6,'9700000006','auth06',2,314,'John 06','MALE',25,'UK-123456789006','UK'),(7,'9700000007','auth07',1,-1,'John 07','MALE',25,'UK-123456789007','UK'),(8,'9700000008','auth08',5,-1,'John 08','MALE',25,'UK-123456789008','UK'),(9,'9700000009','auth09',3,-1,'John 09','MALE',25,'UK-123456789009','UK'),(10,'9700000010','auth10',4,-1,'John 10','MALE',25,'UK-123456789010','UK');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vehicle`
--

DROP TABLE IF EXISTS `vehicle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vehicle` (
  `an` bigint(20) NOT NULL,
  `tid` bigint(20) NOT NULL,
  `regn` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL,
  `dist` int(11) DEFAULT NULL,
  `hrs` double DEFAULT NULL,
  `pid` int(11) DEFAULT NULL,
  `vtype` int(11) DEFAULT NULL,
  PRIMARY KEY (`an`),
  KEY `vehicle_tid_5401bddc` (`tid`),
  KEY `vehicle_regn_1fa406b9` (`regn`),
  KEY `vehicle_pid_ca0bd59d` (`pid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehicle`
--

LOCK TABLES `vehicle` WRITE;
/*!40000 ALTER TABLE `vehicle` DISABLE KEYS */;
INSERT INTO `vehicle` VALUES (2000,117,'reg00',0,0,4,0),(2001,-1,'reg01',0,0,2,0),(2002,-1,'reg02',0,0,3,1),(2003,40,'reg03',0,0,6,2),(2004,-1,'reg04',0,0,5,3),(2005,98,'reg05',0,0,1,0),(2006,-1,'reg06',0,0,5,1),(2007,93,'reg07',0,0,6,2),(2008,89,'reg08',0,0,6,3),(2009,180,'reg09',0,0,3,0),(2010,-1,'reg10',0,0,2,1),(2011,154,'reg11',0,0,5,2),(2012,88,'reg12',0,0,6,3),(2013,90,'reg13',0,0,1,0),(2014,-1,'reg14',0,0,2,1),(2015,-1,'reg15',0,0,6,2),(2016,-1,'reg16',0,0,1,3),(2017,49,'reg17',0,0,2,0),(2018,-1,'reg18',0,0,5,1),(2019,-1,'reg19',0,0,3,2),(2020,-1,'reg20',0,0,3,3);
/*!40000 ALTER TABLE `vehicle` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-05-21 22:00:22
