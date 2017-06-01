-- MySQL dump 10.13  Distrib 5.7.12, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: crawler
-- ------------------------------------------------------
-- Server version	5.7.17-log

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
-- Table structure for table `keywords`
--

DROP TABLE IF EXISTS `keywords`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `keywords` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `searchKeyword` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=82 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `keywords`
--

LOCK TABLES `keywords` WRITE;
/*!40000 ALTER TABLE `keywords` DISABLE KEYS */;
INSERT INTO `keywords` VALUES (1,'sparql'),(2,'\"sparql endpoint\"'),(3,'\"sparql endpoint\" site:co.uk'),(4,'\"sparql endpoint\" site:com'),(5,'\"sparql endpoint\" site:edu'),(6,'\"sparql endpoint\" site:gov'),(7,'\"sparql endpoint\" site:org'),(8,'\"Virtuoso SPARQL Query Editor\"'),(9,'allintext: sparql query'),(10,'allintitle: sparql data'),(11,'allintitle: sparql query'),(12,'allinurl: sparql data'),(13,'intitle:sparql'),(14,'inurl:PoolParty inurl:sparql'),(15,'inurl:PoolParty sparql'),(16,'inurl:sparql'),(17,'sparql -language'),(18,'sparql query'),(19,'allinurl:  sparql data'),(20,'sparql -w3'),(21,'sparql -blog -wiki -w3 -pdf -news'),(22,'sparql -blog'),(23,'sparql -wiki'),(24,'sparql -blog -wiki -w3 -pdf'),(25,'\"sparql endpoint\" -w3'),(26,'\"sparql endpoint\" -blog -wiki -w3 -pdf -news'),(27,'\"sparql endpoint\" -blog'),(28,'\"sparql endpoint\" -wiki'),(29,'\"sparql endpoint\" -blog -wiki -w3 -pdf'),(30,'\"sparql endpoint\" site:.ac'),(31,'\"sparql endpoint\" site:.ad'),(32,'\"sparql endpoint\" site:.ae'),(33,'\"sparql endpoint\" site:.af'),(34,'\"sparql endpoint\" site:.ag'),(35,'\"sparql endpoint\" site:.ai'),(36,'\"sparql endpoint\" site:.al'),(37,'\"sparql endpoint\" site:.am'),(38,'\"sparql endpoint\" site:.an'),(39,'\"sparql endpoint\" site:.ao'),(40,'\"sparql endpoint\" site:.aq'),(41,'\"sparql endpoint\" site:.ar'),(42,'\"sparql endpoint\" site:.as'),(43,'\"sparql endpoint\" site:.at'),(44,'\"sparql endpoint\" site:.au'),(45,'\"sparql endpoint\" site:.aw'),(46,'\"sparql endpoint\" site:.ax'),(47,'\"sparql endpoint\" site:.az'),(48,'\"sparql endpoint\" site:.ba'),(49,'\"sparql endpoint\" site:.bb'),(50,'\"sparql endpoint\" site:.bd'),(51,'\"sparql endpoint\" site:.be'),(52,'\"sparql endpoint\" site:.bf'),(53,'\"sparql endpoint\" site:.bg'),(54,'\"sparql endpoint\" site:.bh'),(55,'\"sparql endpoint\" site:.bi'),(56,'\"sparql endpoint\" site:.bj'),(57,'\"sparql endpoint\" site:.bm'),(58,'\"sparql endpoint\" site:.bn'),(59,'\"sparql endpoint\" site:.bo'),(60,'\"sparql endpoint\" site:.bq'),(61,'\"sparql endpoint\" site:.br'),(62,'\"sparql endpoint\" site:.bs'),(63,'\"sparql endpoint\" site:.bt'),(64,'\"sparql endpoint\" site:.bv'),(65,'\"sparql endpoint\" site:.bw'),(66,'\"sparql endpoint\" site:.by'),(67,'\"sparql endpoint\" site:.bz'),(68,'\"sparql endpoint\" site:.ca'),(69,'\"sparql endpoint\" site:.cc'),(70,'\"sparql endpoint\" site:.cd'),(71,'\"sparql endpoint\" site:.cf'),(72,'\"sparql endpoint\" site:.cg'),(73,'\"sparql endpoint\" site:.ch'),(74,'\"sparql endpoint\" site:.ci'),(75,'\"sparql endpoint\" site:.ck'),(76,'\"sparql endpoint\" site:.cl'),(77,'\"sparql endpoint\" site:.cm'),(78,'\"sparql endpoint\" site:.cn'),(79,'\"sparql endpoint\" site:.co'),(80,'\"sparql endpoint\" site:.cr'),(81,'\"sparql endpoint\" site:.cs');
/*!40000 ALTER TABLE `keywords` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-06-01 11:19:07
