# Attention! This will truncate the class, attribute, attributepath and schema tables!
# A schema and data model for bibo:Document and bibrm:ContractItem will be created

SET FOREIGN_KEY_CHECKS=0;
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";

--
-- Database: `dmp`
--

--
-- Truncate table before insert `ATTRIBUTE`
--

TRUNCATE TABLE `ATTRIBUTE`;
--
-- Dumping data for table `ATTRIBUTE`
--

INSERT INTO `ATTRIBUTE` (`ID`, `NAME`, `URI`) VALUES
(1, 'type', 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type'),
(2, 'EISSN', 'http://vocab.ub.uni-leipzig.de/bibrm/EISSN'),
(3, 'title', 'http://purl.org/dc/elements/1.1/title'),
(4, 'price', 'http://vocab.ub.uni-leipzig.de/bibrm/price'),
(5, 'otherTitleInformation', 'http://rdvocab.info/Elements/otherTitleInformation'),
(6, 'alternative', 'http://purl.org/dc/terms/alternative'),
(7, 'shortTitle', 'http://purl.org/ontology/bibo/shortTitle'),
(8, 'creator', 'http://purl.org/dc/terms/creator'),
(9, 'creator', 'http://purl.org/dc/elements/1.1/creator'),
(10, 'contributor', 'http://purl.org/dc/terms/contributor'),
(11, 'contributor', 'http://purl.org/dc/elements/1.1/contributor'),
(12, 'publicationStatement', 'http://rdvocab.info/Elements/publicationStatement'),
(13, 'placeOfPublication', 'http://rdvocab.info/Elements/placeOfPublication'),
(14, 'publisher', 'http://purl.org/dc/elements/1.1/publisher'),
(15, 'issued', 'http://purl.org/dc/terms/issued'),
(16, 'sameAs', 'http://www.w3.org/2002/07/owl#sameAs'),
(17, 'isLike', 'http://umbel.org/umbel#isLike'),
(18, 'issn', 'http://purl.org/ontology/bibo/issn'),
(19, 'eissn', 'http://purl.org/ontology/bibo/eissn'),
(20, 'lccn', 'http://purl.org/ontology/bibo/lccn'),
(21, 'oclcnum', 'http://purl.org/ontology/bibo/oclcnum'),
(22, 'isbn', 'http://purl.org/ontology/bibo/isbn'),
(23, 'medium', 'http://purl.org/dc/terms/medium'),
(24, 'hasPart', 'http://purl.org/dc/terms/hasPart'),
(25, 'isPartOf', 'http://purl.org/dc/terms/isPartOf'),
(26, 'hasVersion', 'http://purl.org/dc/terms/hasVersion'),
(27, 'isFormatOf', 'http://purl.org/dc/terms/isFormatOf'),
(28, 'precededBy', 'http://rdvocab.info/Elements/precededBy'),
(29, 'succeededBy', 'http://rdvocab.info/Elements/succeededBy'),
(30, 'language', 'http://purl.org/dc/terms/language'),
(31, '1053', 'http://iflastandards.info/ns/isbd/elements/1053'),
(32, 'edition', 'http://purl.org/ontology/bibo/edition'),
(33, 'bibliographicCitation', 'http://purl.org/dc/terms/bibliographicCitation'),
(34, 'familyName', 'http://xmlns.com/foaf/0.1/familyName'),
(35, 'givenName', 'http://xmlns.com/foaf/0.1/givenName');

--
-- Truncate table before insert `ATTRIBUTES_ATTRIBUTE_PATHS`
--

TRUNCATE TABLE `ATTRIBUTE_PATHS_ATTRIBUTES`;
--
-- Dumping data for table `ATTRIBUTES_ATTRIBUTE_PATHS`
--

INSERT INTO `ATTRIBUTE_PATHS_ATTRIBUTES` (`ATTRIBUTE_PATH_ID`, `ATTRIBUTE_ID`) VALUES
(1, 1),
(34, 1),
(37, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5),
(6, 6),
(7, 7),
(8, 8),
(34, 8),
(35, 8),
(36, 8),
(9, 9),
(10, 10),
(37, 10),
(38, 10),
(39, 10),
(11, 11),
(12, 12),
(13, 13),
(14, 14),
(15, 15),
(16, 16),
(17, 17),
(18, 18),
(19, 19),
(20, 20),
(21, 21),
(22, 22),
(23, 23),
(24, 24),
(25, 25),
(26, 26),
(27, 27),
(28, 28),
(29, 29),
(30, 30),
(31, 31),
(32, 32),
(33, 33),
(35, 34),
(38, 34),
(36, 35),
(39, 35);

--
-- Truncate table before insert `ATTRIBUTE_PATH`
--

TRUNCATE TABLE `ATTRIBUTE_PATH`;
--
-- Dumping data for table `ATTRIBUTE_PATH`
--

INSERT INTO `ATTRIBUTE_PATH` (`ID`, `ATTRIBUTE_PATH`) VALUES
(1, '[1]'),
(2, '[2]'),
(3, '[3]'),
(4, '[4]'),
(5, '[5]'),
(6, '[6]'),
(7, '[7]'),
(8, '[8]'),
(9, '[9]'),
(10, '[10]'),
(11, '[11]'),
(12, '[12]'),
(13, '[13]'),
(14, '[14]'),
(15, '[15]'),
(16, '[16]'),
(17, '[17]'),
(18, '[18]'),
(19, '[19]'),
(20, '[20]'),
(21, '[21]'),
(22, '[22]'),
(23, '[23]'),
(24, '[24]'),
(25, '[25]'),
(26, '[26]'),
(27, '[27]'),
(28, '[28]'),
(29, '[29]'),
(30, '[30]'),
(31, '[31]'),
(32, '[32]'),
(33, '[33]'),
(34, '[8,1]'),
(35, '[8,34]'),
(36, '[8,35]'),
(37, '[10,1]'),
(38, '[10,34]'),
(39, '[10,35]');

--
-- Truncate table before insert `ATTRIBUTE_PATHS_SCHEMAS`
--

TRUNCATE TABLE `SCHEMAS_ATTRIBUTE_PATHS`;
--
-- Dumping data for table `ATTRIBUTE_PATHS_SCHEMAS`
--

INSERT INTO `SCHEMAS_ATTRIBUTE_PATHS` (`SCHEMA_ID`, `ATTRIBUTE_PATH_ID`) VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(2, 5),
(2, 6),
(2, 7),
(2, 8),
(2, 9),
(2, 10),
(2, 11),
(2, 12),
(2, 13),
(2, 14),
(2, 15),
(2, 16),
(2, 17),
(2, 18),
(2, 19),
(2, 20),
(2, 21),
(2, 22),
(2, 23),
(2, 24),
(2, 25),
(2, 26),
(2, 27),
(2, 28),
(2, 29),
(2, 30),
(2, 31),
(2, 32),
(2, 33),
(2, 34),
(2, 35),
(2, 36),
(2, 37),
(2, 38),
(2, 39);

--
-- Truncate table before insert `CLASS`
--

TRUNCATE TABLE `CLASS`;
--
-- Dumping data for table `CLASS`
--

INSERT INTO `CLASS` (`ID`, `NAME`, `URI`) VALUES
(1, 'ContractItem', 'http://vocab.ub.uni-leipzig.de/bibrm/ContractItem'),
(2, 'Document', 'http://purl.org/ontology/bibo/Document');

TRUNCATE TABLE `DATA_SCHEMA`;
--
-- Dumping data for table `DATA_SCHEMA`
--

INSERT INTO `DATA_SCHEMA` (`ID`, `NAME`, `RECORD_CLASS`) VALUES
(1,'bibrm:ContractItem-Schema (ERM-Scenario)',1),
(2,'bibo:Document-Schema (KIM-Titeldaten)',2);

--
-- Truncate table before insert `DATA_MODEL`
--

TRUNCATE TABLE `DATA_MODEL`;
INSERT INTO `DATA_MODEL` (ID, NAME, DESCRIPTION, CONFIGURATION, DATA_RESOURCE, DATA_SCHEMA) VALUES
(1,'Internal Data Model ContractItem', 'Internal Data Model ContractItem', null, null, 1),
(2,'Internal Data Model BiboDocument', 'Internal Data Model BiboDocument', null, null, 2);

SET FOREIGN_KEY_CHECKS=1;