/*
 Navicat Premium Data Transfer

 Source Server         : lty
 Source Server Type    : MySQL
 Source Server Version : 50723
 Source Host           : localhost:3306
 Source Schema         : ocean

 Target Server Type    : MySQL
 Target Server Version : 50723
 File Encoding         : 65001

 Date: 31/03/2026 13:35:27
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ocean_grid_data
-- ----------------------------
DROP TABLE IF EXISTS `ocean_grid_data`;
CREATE TABLE `ocean_grid_data`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `longitude` double NOT NULL,
  `latitude` double NOT NULL,
  `data_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `data_value` double NOT NULL,
  `unit` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `data_time` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_data_type_time`(`data_type`, `data_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20001 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
