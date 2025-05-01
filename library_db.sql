-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: May 01, 2025 at 04:17 PM
-- Server version: 10.4.28-MariaDB
-- PHP Version: 8.0.28

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `library_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `Admin_Accounts`
--

CREATE TABLE `Admin_Accounts` (
  `Admin_ID` int(11) NOT NULL,
  `Password` varchar(255) NOT NULL,
  `Last_Name` varchar(100) DEFAULT NULL,
  `First_Name` varchar(100) DEFAULT NULL,
  `Username` varchar(50) DEFAULT NULL,
  `Email` varchar(100) DEFAULT NULL,
  `Role` enum('Librarian','Assistant') DEFAULT NULL,
  `Status` enum('Active','Inactive') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `Admin_Accounts`
--

INSERT INTO `Admin_Accounts` (`Admin_ID`, `Password`, `Last_Name`, `First_Name`, `Username`, `Email`, `Role`, `Status`) VALUES
(212345, '123', 'Johnson', 'Emily', 'ejohnson', 'ejohnson@library.edu', 'Librarian', 'Active'),
(212348, '123', 'Jones', 'Michael', 'mjones', 'mjones@library.edu', 'Assistant', 'Active'),
(212349, '123', 'Miller', 'Jennifer', 'jmiller', 'jmiller@library.edu', 'Librarian', 'Active'),
(232145, '123', 'Williams', 'James', 'jwilliams', 'jwilliams@library.edu', 'Assistant', 'Active'),
(245671, '123', 'Brown', 'Patricia', 'pbrown', 'pbrown@library.edu', 'Librarian', 'Inactive'),
(365724, '123', 'mariano', 'jhyra', 'jhyra', 'jhyra@gmail.com', 'Librarian', 'Active');

-- --------------------------------------------------------

--
-- Table structure for table `Books`
--

CREATE TABLE `Books` (
  `Book_ID` int(11) NOT NULL,
  `ISBN` varchar(20) DEFAULT NULL,
  `Title` varchar(255) DEFAULT NULL,
  `Author` varchar(100) DEFAULT NULL,
  `Category` varchar(100) DEFAULT NULL,
  `Total_Copies` int(11) DEFAULT NULL,
  `Available_Copies` int(11) DEFAULT NULL,
  `Reserved_Copies` int(11) DEFAULT NULL,
  `Lost_Damaged_Copies` int(11) DEFAULT NULL,
  `Added_Date` date DEFAULT NULL,
  `Last_Borrowed_Date` date DEFAULT NULL,
  `Times_Borrowed` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `Books`
--

INSERT INTO `Books` (`Book_ID`, `ISBN`, `Title`, `Author`, `Category`, `Total_Copies`, `Available_Copies`, `Reserved_Copies`, `Lost_Damaged_Copies`, `Added_Date`, `Last_Borrowed_Date`, `Times_Borrowed`) VALUES
(1, '9780134685992', 'Effective Java Book', 'Joshua Bloch', 'Computer Science', 5, 4, 0, 1, '2023-01-15', '2024-03-10', 13),
(2, '978-0321356680', 'Clean Code', 'Robert C. Martin', 'Computer Science', 4, 2, 0, 2, '2023-02-20', '2024-04-05', 9),
(3, '978-0262033848', 'Introduction to Algorithms', 'Cormen, Leiserson, Rivest', 'Computer Science', 3, 1, 1, 1, '2023-03-10', '2024-03-28', 5),
(4, '978-0470464725', 'Calculus: Early Transcendentals', 'James Stewart', 'Mathematics', 6, 4, 0, 2, '2023-01-25', '2024-04-01', 9),
(5, '978-0321973610', 'University Physics', 'Young and Freedman', 'Physics', 4, 6, 1, 1, '2023-02-15', '2024-03-15', 8),
(6, '978-1429218157', 'Principles of Chemistry', 'Atkins and Jones', 'Chemistry', 3, 1, 0, 2, '2023-03-05', '2024-02-20', 4),
(7, '978-1319017637', 'Campbell Biology', 'Urry et al.', 'Biology', 5, 1, 3, 1, '2023-01-30', '2024-03-25', 6),
(8, '978-0143105428', 'To Kill a Mockingbird', 'Harper Lee', 'Literature', 7, 2, 2, 2, '2023-02-10', '2024-04-02', 17),
(9, '978-0062315007', 'The Alchemist', 'Paulo Coelho', 'Literature', 4, 3, 1, 1, '2023-03-15', '2024-03-20', 10),
(10, '978-0393059748', 'Guns, Germs, and Steel', 'Jared Diamond', 'History', 3, 2, 0, 2, '2023-01-20', '2024-02-28', 5),
(11, '2312421', 'Book1', 'Jc', 'Art', 5, 4, 0, 0, '2025-04-25', NULL, 1);

-- --------------------------------------------------------

--
-- Table structure for table `Book_Categories`
--

CREATE TABLE `Book_Categories` (
  `Category_ID` int(11) NOT NULL,
  `Category_Name` varchar(100) DEFAULT NULL,
  `Total_Books` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `Book_Categories`
--

INSERT INTO `Book_Categories` (`Category_ID`, `Category_Name`, `Total_Books`) VALUES
(1, 'Computer Science', 15),
(2, 'Mathematics', 12),
(3, 'Physics', 10),
(4, 'Chemistry', 8),
(5, 'Biology', 9),
(6, 'Literature', 20),
(7, 'History', 14),
(8, 'Art', 8),
(9, 'Economics', 11),
(10, 'Psychology', 6);

-- --------------------------------------------------------

--
-- Table structure for table `Borrowed_Books`
--

CREATE TABLE `Borrowed_Books` (
  `Borrow_ID` int(11) NOT NULL,
  `Student_ID` varchar(100) DEFAULT NULL,
  `Student_Name` varchar(100) DEFAULT NULL,
  `Book_ID` int(11) DEFAULT NULL,
  `Book_Title` varchar(255) DEFAULT NULL,
  `Borrow_Date` date DEFAULT NULL,
  `Due_Date` date DEFAULT NULL,
  `Return_Date` date DEFAULT NULL,
  `Status` enum('Borrowed','Returned','Overdue') DEFAULT NULL,
  `Penalty_Fee` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `Borrowed_Books`
--

INSERT INTO `Borrowed_Books` (`Borrow_ID`, `Student_ID`, `Student_Name`, `Book_ID`, `Book_Title`, `Borrow_Date`, `Due_Date`, `Return_Date`, `Status`, `Penalty_Fee`) VALUES
(1, '02185365777', 'Jc David', 1, 'Effective Java', '2024-03-01', '2024-03-15', '2024-03-14', 'Returned', 0.00),
(2, '02185365123', 'Maria Garcia', 2, 'Clean Code', '2024-03-05', '2024-03-19', '2024-03-18', 'Returned', 0.00),
(3, '02185365234', 'John Smith', 3, 'Introduction to Algorithms', '2024-03-10', '2024-03-24', NULL, 'Overdue', 5.50),
(4, '02185365345', 'Emma Johnson', 4, 'Calculus: Early Transcendentals', '2024-03-15', '2024-03-29', NULL, 'Overdue', 193.50),
(5, '02185365456', 'Michael Brown', 5, 'University Physics', '2024-03-20', '2024-04-03', '2024-04-02', 'Returned', 0.00),
(6, '02185365567', 'Sarah Wilson', 6, 'Principles of Chemistry', '2024-03-25', '2024-04-08', NULL, 'Overdue', 188.50),
(7, '02185365678', 'David Lee', 7, 'Campbell Biology', '2024-03-28', '2024-04-11', NULL, 'Overdue', 187.00),
(8, '02185365789', 'Jennifer Davis', 8, 'To Kill a Mockingbird', '2024-04-01', '2024-04-15', NULL, 'Overdue', 185.00),
(9, '02185365890', 'Robert Taylor', 9, 'The Alchemist', '2024-04-05', '2024-04-19', '2025-04-20', 'Returned', 183.00),
(10, '02185365901', 'Lisa Martinez', 10, 'Guns, Germs, and Steel', '2024-04-10', '2024-04-24', '2025-04-20', 'Returned', 180.50),
(13, '02185365777', 'Jc David', 2, 'Clean Code', '2025-04-19', '2025-05-03', '2025-04-19', 'Returned', NULL),
(14, '02185365777', 'Jc David', 5, 'University Physics', '2025-04-19', '2025-05-03', '2025-04-19', 'Borrowed', NULL),
(15, '02185365777', 'Jc David', 8, 'To Kill a Mockingbird', '2025-04-19', '2025-05-03', '2025-04-19', 'Returned', NULL),
(16, '02185365777', 'Jc David', 1, 'Effective Java', '2025-04-19', '2025-05-03', '2025-04-19', 'Returned', NULL),
(17, '02185365777', 'Juan Carlo David', 8, 'To Kill a Mockingbird', '2025-04-25', '2025-05-09', NULL, 'Borrowed', NULL),
(18, '02185365777', 'Juan Carlo David', 11, 'Book1', '2025-04-25', '2025-05-09', NULL, 'Borrowed', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `Borrowing_History`
--

CREATE TABLE `Borrowing_History` (
  `Borrow_history_id` int(11) NOT NULL,
  `Student_ID` varchar(100) DEFAULT NULL,
  `Student_Name` varchar(100) DEFAULT NULL,
  `Total_Borrowed_Books` int(11) DEFAULT NULL,
  `Total_Reserved_Books` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `Borrowing_History`
--

INSERT INTO `Borrowing_History` (`Borrow_history_id`, `Student_ID`, `Student_Name`, `Total_Borrowed_Books`, `Total_Reserved_Books`) VALUES
(1, '02185365777', 'Jc David', 7, 3),
(2, '02185365123', 'Maria Garcia', 3, 1),
(3, '02185365234', 'John Smith', 7, 1),
(4, '02185365345', 'Emma Johnson', 4, 1),
(5, '02185365456', 'Michael Brown', 6, 1),
(6, '02185365567', 'Sarah Wilson', 2, 0),
(7, '02185365678', 'David Lee', 3, 0),
(8, '02185365789', 'Jennifer Davis', 1, 0),
(9, '02185365890', 'Robert Taylor', 4, 1),
(10, '02185365901', 'Lisa Martinez', 2, 0);

-- --------------------------------------------------------

--
-- Table structure for table `Due_Books_Notifications`
--

CREATE TABLE `Due_Books_Notifications` (
  `Book_ID` int(11) DEFAULT NULL,
  `Book_Title` varchar(255) DEFAULT NULL,
  `Student_ID` varchar(100) DEFAULT NULL,
  `Student_Name` varchar(100) DEFAULT NULL,
  `Due_Date` date DEFAULT NULL,
  `Days_Overdue` int(11) DEFAULT NULL,
  `Fine` decimal(10,2) DEFAULT NULL,
  `Status` enum('On time','Overdue') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `Due_Books_Notifications`
--

INSERT INTO `Due_Books_Notifications` (`Book_ID`, `Book_Title`, `Student_ID`, `Student_Name`, `Due_Date`, `Days_Overdue`, `Fine`, `Status`) VALUES
(3, 'Introduction to Algorithms', '02185365234', 'John Smith', '2024-03-24', 23, 11.50, 'Overdue'),
(4, 'Calculus: Early Transcendentals', '02185365345', 'Emma Johnson', '2024-03-29', 18, 9.00, 'Overdue'),
(6, 'Principles of Chemistry', '02185365567', 'Sarah Wilson', '2024-04-08', 8, 4.00, 'Overdue'),
(7, 'Campbell Biology', '02185365678', 'David Lee', '2024-04-11', 5, 2.50, 'Overdue'),
(8, 'To Kill a Mockingbird', '02185365789', 'Jennifer Davis', '2024-04-15', 1, 0.50, 'Overdue');

-- --------------------------------------------------------

--
-- Table structure for table `Lost_Damaged_Reports`
--

CREATE TABLE `Lost_Damaged_Reports` (
  `Report_ID` int(11) NOT NULL,
  `Book_ID` int(11) DEFAULT NULL,
  `Book_Title` varchar(255) DEFAULT NULL,
  `Student_ID` varchar(100) DEFAULT NULL,
  `Student_Name` varchar(100) DEFAULT NULL,
  `Report_Date` date DEFAULT NULL,
  `Issue_Type` varchar(100) DEFAULT NULL,
  `Penalty_Fee` decimal(10,2) DEFAULT NULL,
  `Status` enum('Pending','Resolved') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `Lost_Damaged_Reports`
--

INSERT INTO `Lost_Damaged_Reports` (`Report_ID`, `Book_ID`, `Book_Title`, `Student_ID`, `Student_Name`, `Report_Date`, `Issue_Type`, `Penalty_Fee`, `Status`) VALUES
(1, 1, 'Effective Java', '02185365777', 'Jc David', '2024-02-15', 'Damaged cover', 15.00, 'Resolved'),
(2, 2, 'Clean Code', '02185365123', 'Maria Garcia', '2024-03-01', 'Lost book', 25.00, 'Pending'),
(3, 4, 'Calculus: Early Transcendentals', '02185365345', 'Emma Johnson', '2024-03-10', 'Water damage', 20.00, 'Resolved'),
(4, 6, 'Principles of Chemistry', '02185365567', 'Sarah Wilson', '2024-03-20', 'Missing pages', 10.00, 'Pending'),
(5, 10, 'Guns, Germs, and Steel', '02185365901', 'Lisa Martinez', '2024-04-05', 'Lost book', 30.00, 'Pending'),
(7, 5, 'University Physics', '02185365777', 'Jc David', '2025-04-19', 'Missing pages: Nonne', 10.00, 'Pending'),
(8, 5, 'University Physics', '02185365777', 'Jc David', '2025-04-19', 'Water damage: NABASAAA', 20.00, 'Pending');

-- --------------------------------------------------------

--
-- Table structure for table `Most_Borrowed_Books`
--

CREATE TABLE `Most_Borrowed_Books` (
  `Book_ID` int(11) DEFAULT NULL,
  `ISBN` varchar(20) DEFAULT NULL,
  `Book_Title` varchar(255) DEFAULT NULL,
  `Total_Borrowed_Times` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `Most_Borrowed_Books`
--

INSERT INTO `Most_Borrowed_Books` (`Book_ID`, `ISBN`, `Book_Title`, `Total_Borrowed_Times`) VALUES
(1, '978-0134685991', 'Effective Java', 12),
(8, '978-0143105428', 'To Kill a Mockingbird', 15),
(2, '978-0321356680', 'Clean Code', 8),
(9, '978-0062315007', 'The Alchemist', 10),
(4, '978-0470464725', 'Calculus: Early Transcendentals', 9);

-- --------------------------------------------------------

--
-- Table structure for table `Penalties_Fines`
--

CREATE TABLE `Penalties_Fines` (
  `Penalty_ID` int(11) NOT NULL,
  `Student_ID` varchar(100) DEFAULT NULL,
  `Student_Name` varchar(100) DEFAULT NULL,
  `Book_Title` varchar(255) DEFAULT NULL,
  `Issue_Type` varchar(100) DEFAULT NULL,
  `Fine_Amount` decimal(10,2) DEFAULT NULL,
  `Due_Date` date DEFAULT NULL,
  `Return_Date` date DEFAULT NULL,
  `Status` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `Penalties_Fines`
--

INSERT INTO `Penalties_Fines` (`Penalty_ID`, `Student_ID`, `Student_Name`, `Book_Title`, `Issue_Type`, `Fine_Amount`, `Due_Date`, `Return_Date`, `Status`) VALUES
(1, '02185365777', 'Jc David', 'Effective Java', 'Damaged cover', 15.00, '2024-02-28', '2024-02-20', 'Paid'),
(2, '02185365123', 'Maria Garcia', 'Clean Code', 'Lost book', 25.00, '2024-03-15', NULL, 'Unpaid'),
(3, '02185365234', 'John Smith', 'Introduction to Algorithms', 'Overdue', 11.50, '2024-04-24', NULL, 'Unpaid'),
(4, '02185365345', 'Emma Johnson', 'Calculus: Early Transcendentals', 'Water damage', 20.00, '2024-03-25', '2024-03-22', 'Paid'),
(5, '02185365567', 'Sarah Wilson', 'Principles of Chemistry', 'Missing pages', 10.00, '2024-04-15', NULL, 'Unpaid'),
(6, '02185365777', 'Jc David', 'University Physics', 'Missing pages', 10.00, '2025-05-03', NULL, 'Unpaid'),
(7, '02185365777', 'Jc David', 'University Physics', 'Missing pages', 10.00, '2025-05-03', NULL, 'Unpaid'),
(8, '02185365777', 'Jc David', 'University Physics', 'Water damage', 20.00, '2025-05-03', NULL, 'Unpaid'),
(9, '02185365234', 'John Smith', 'Introduction to Algorithms', 'Overdue', 11.50, '2025-04-20', NULL, 'Paid');

-- --------------------------------------------------------

--
-- Table structure for table `Reservation_Approvals`
--

CREATE TABLE `Reservation_Approvals` (
  `Reservation_ID` int(11) NOT NULL,
  `Student_ID` varchar(100) DEFAULT NULL,
  `Book_Title` varchar(255) DEFAULT NULL,
  `Reservation_Date` date DEFAULT NULL,
  `Expiration_Date` date DEFAULT NULL,
  `Status` enum('Pending','Approved','Rejected') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `Reservation_Approvals`
--

INSERT INTO `Reservation_Approvals` (`Reservation_ID`, `Student_ID`, `Book_Title`, `Reservation_Date`, `Expiration_Date`, `Status`) VALUES
(1, '02185365777', 'Effective Java', '2024-04-01', '2024-04-08', 'Pending'),
(2, '02185365123', 'Introduction to Algorithms', '2024-04-05', '2024-04-12', 'Approved'),
(3, '02185365234', 'University Physics', '2024-04-10', '2024-04-17', 'Pending'),
(4, '02185365345', 'Campbell Biology', '2024-03-28', '2024-04-04', 'Approved'),
(5, '02185365456', 'The Alchemist', '2024-04-02', '2024-04-09', 'Approved'),
(7, '02185365777', 'Campbell Biology', '2025-04-19', '2025-04-26', 'Pending');

-- --------------------------------------------------------

--
-- Table structure for table `Reserved_Books`
--

CREATE TABLE `Reserved_Books` (
  `Reservation_ID` int(11) NOT NULL,
  `Student_ID` varchar(100) DEFAULT NULL,
  `Student_Name` varchar(100) DEFAULT NULL,
  `Book_ID` int(11) DEFAULT NULL,
  `Book_Title` varchar(255) DEFAULT NULL,
  `Reservation_Date` date DEFAULT NULL,
  `Expiration_Date` date DEFAULT NULL,
  `Status` enum('Pending','Reserved','Cancelled','Expired','Completed') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `Reserved_Books`
--

INSERT INTO `Reserved_Books` (`Reservation_ID`, `Student_ID`, `Student_Name`, `Book_ID`, `Book_Title`, `Reservation_Date`, `Expiration_Date`, `Status`) VALUES
(1, '02185365777', 'Jc David', 1, 'Effective Java', '2024-04-01', '2024-04-08', 'Pending'),
(2, '02185365123', 'Maria Garcia', 3, 'Introduction to Algorithms', '2024-04-05', '2024-04-12', 'Reserved'),
(3, '02185365234', 'John Smith', 5, 'University Physics', '2024-04-10', '2024-04-17', 'Reserved'),
(4, '02185365345', 'Emma Johnson', 7, 'Campbell Biology', '2024-03-28', '2024-04-04', 'Expired'),
(5, '02185365456', 'Michael Brown', 9, 'The Alchemist', '2024-04-02', '2024-04-09', 'Completed'),
(6, '02185365777', 'Jc David', 8, 'To Kill a Mockingbird', '2025-04-19', '2025-04-26', 'Reserved'),
(7, '02185365777', 'Jc David', 7, 'Campbell Biology', '2025-04-19', '2025-04-26', 'Pending');

-- --------------------------------------------------------

--
-- Table structure for table `Students`
--

CREATE TABLE `Students` (
  `Student_ID` varchar(100) NOT NULL,
  `Student_Name` varchar(100) DEFAULT NULL,
  `password` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `Students`
--

INSERT INTO `Students` (`Student_ID`, `Student_Name`, `password`) VALUES
('02185365123', 'Maria Garcia', 'password123'),
('02185365234', 'John Smith', 'securepass'),
('02185365345', 'Emma Johnson', 'emma2024'),
('02185365456', 'Michael Brown', 'mikebrown'),
('02185365567', 'Sarah Wilson', 'sarahw'),
('02185365678', 'David Lee', 'dlee123'),
('02185365777', 'Juan Carlo David', '123'),
('02185365789', 'Jennifer Davis', 'jenn2024'),
('02185365890', 'Robert Taylor', 'rtaylor'),
('02185365901', 'Lisa Martinez', 'lisa123');

-- --------------------------------------------------------

--
-- Table structure for table `System_Logs`
--

CREATE TABLE `System_Logs` (
  `Log_ID` int(11) NOT NULL,
  `Timestamp` datetime DEFAULT NULL,
  `User_Type` varchar(50) DEFAULT NULL,
  `User_ID` varchar(100) DEFAULT NULL,
  `Action_Performed` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `System_Logs`
--

INSERT INTO `System_Logs` (`Log_ID`, `Timestamp`, `User_Type`, `User_ID`, `Action_Performed`) VALUES
(1, '2024-04-01 09:15:23', 'Student', '02185365777', 'Borrowed book: Effective Java'),
(2, '2024-04-01 10:30:45', 'Admin', '1', 'Approved reservation for student 02185365777'),
(3, '2024-04-02 11:45:12', 'Student', '02185365123', 'Borrowed book: Clean Code'),
(4, '2024-04-03 13:20:34', 'Admin', '2', 'Processed book return for student 02185365777'),
(5, '2024-04-04 14:55:01', 'Student', '02185365234', 'Reserved book: University Physics'),
(6, '2024-04-05 08:10:22', 'Admin', '3', 'Reported damaged book for student 02185365777'),
(7, '2024-04-06 15:40:33', 'Student', '02185365345', 'Borrowed book: Calculus: Early Transcendentals'),
(8, '2024-04-07 16:25:44', 'Admin', '1', 'Updated book inventory'),
(9, '2024-04-08 09:50:55', 'Student', '02185365456', 'Returned book: University Physics'),
(10, '2024-04-09 10:35:06', 'Admin', '2', 'Processed fine payment for student 02185365777'),
(14, '2025-04-19 14:59:28', 'Student', '02185365777', 'Returned book ID: 5'),
(15, '2025-04-19 15:16:46', 'Student', '02185365777', 'Returned book ID: 5'),
(16, '2025-04-19 15:23:53', 'Student', '02185365777', 'Returned book ID: 5'),
(17, '2025-04-19 15:29:24', 'Student', '02185365777', 'Returned book ID: 5'),
(18, '2025-04-19 15:42:19', 'Student', '02185365777', 'Returned book ID: 2'),
(19, '2025-04-19 15:53:52', 'Student', '02185365777', 'Returned book ID: 5'),
(20, '2025-04-19 16:01:00', 'Student', '02185365777', 'Returned book ID: 1'),
(21, '2025-04-19 16:01:31', 'Student', '02185365777', 'Returned book ID: 8'),
(22, '2025-04-20 00:20:51', 'Admin', '212348', 'Updated book category ID 8 from \'Art\' to \'Art\''),
(23, '2025-04-20 00:20:54', 'Admin', '212348', 'Updated book category ID 8 from \'Art\' to \'Art\''),
(24, '2025-04-20 00:23:16', 'Admin', '212348', 'Updated book category ID 8 from \'Art\' to \'Tite\''),
(25, '2025-04-20 00:23:35', 'Admin', '212348', 'Updated book category ID 8 from \'Tite\' to \'Art\''),
(26, '2025-04-20 00:23:48', 'Admin', '212348', 'Added new book category: tite'),
(27, '2025-04-20 00:23:52', 'Admin', '212348', 'Deleted book category: tite'),
(28, '2025-04-20 00:38:37', 'Admin', '212348', 'Added new book category: Tite'),
(29, '2025-04-20 00:42:52', 'Admin', '212348', 'Added new book: asd'),
(30, '2025-04-20 00:43:25', 'Admin', '212348', 'Added new book: asda'),
(31, '2025-04-20 01:08:29', 'Admin', '212348', 'Admin removed book ID: 11'),
(32, '2025-04-20 01:08:32', 'Admin', '212348', 'Admin removed book ID: 12'),
(33, '2025-04-20 01:08:45', 'Admin', '212348', 'Deleted book category: Tite'),
(34, '2025-04-20 10:28:55', 'Admin', '212348', 'Processed book return: Book ID 10 for student 02185365901'),
(35, '2025-04-20 11:14:21', 'Admin', '212348', 'Updated reservation status for ID: 4 to Reserved'),
(36, '2025-04-20 11:14:26', 'Admin', '212348', 'Updated reservation status for ID: 4 to Expired'),
(37, '2025-04-20 12:21:07', 'Admin', '212348', 'Processed book return: Book ID 9 for student 02185365890'),
(38, '2025-04-20 12:23:50', 'Admin', '212348', 'Updated student details for ID: 02185365777'),
(39, '2025-04-20 14:14:49', 'Admin', '212348', 'Admin added new penalty for student 02185365234 for Overdue'),
(40, '2025-04-20 14:14:59', 'Admin', '212348', 'Admin marked penalty ID 9 as paid for student 02185365234'),
(41, '2025-04-20 14:31:34', 'Admin', '212348', 'Admin logged out'),
(42, '2025-04-20 14:44:38', 'Admin', '212348', 'Updated admin account: pbrown'),
(43, '2025-04-20 14:44:44', 'Admin', '212348', 'Updated admin account: mjones'),
(44, '2025-04-20 14:48:27', 'Admin', '212348', 'Created new admin account: jhyra'),
(45, '2025-04-20 14:49:19', 'Admin', '212348', 'Created new admin account: asd'),
(46, '2025-04-20 14:49:38', 'Admin', '212348', 'Deleted admin account: asd'),
(47, '2025-04-20 15:10:39', 'Admin', '212348', 'Viewed system logs'),
(48, '2025-04-20 15:11:16', 'Admin', '212348', 'Viewed system logs'),
(49, '2025-04-20 15:11:24', 'Admin', '212348', 'Admin logged out'),
(50, '2025-04-25 21:37:14', 'Admin', '212345', 'Added new book: Book1'),
(51, '2025-04-27 12:32:05', 'Admin', '212348', 'Viewed system logs'),
(52, '2025-04-27 14:44:26', 'Student', '02185365777', 'Library Visit: Study/Research'),
(53, '2025-04-27 14:45:56', 'Employee', '0212348', 'Library Visit: Research');

-- --------------------------------------------------------

--
-- Table structure for table `Visit_Logs`
--

CREATE TABLE `Visit_Logs` (
  `Visit_ID` int(11) NOT NULL,
  `Student_ID` varchar(100) DEFAULT NULL,
  `Student_Name` varchar(100) DEFAULT NULL,
  `Employee_ID` varchar(100) DEFAULT NULL,
  `Employee_Name` varchar(100) DEFAULT NULL,
  `Visit_Date` datetime DEFAULT current_timestamp(),
  `Purpose` varchar(255) DEFAULT NULL,
  `Visitor_Type` enum('Student','Employee') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `Visit_Logs`
--

INSERT INTO `Visit_Logs` (`Visit_ID`, `Student_ID`, `Student_Name`, `Employee_ID`, `Employee_Name`, `Visit_Date`, `Purpose`, `Visitor_Type`) VALUES
(1, '02185365777', 'Juan Carlo David', NULL, NULL, '2025-04-27 14:44:26', 'Study/Research', 'Student'),
(2, NULL, NULL, '0212348', 'Michael Jones', '2025-04-27 14:45:56', 'Research', 'Employee');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `Admin_Accounts`
--
ALTER TABLE `Admin_Accounts`
  ADD PRIMARY KEY (`Admin_ID`);

--
-- Indexes for table `Books`
--
ALTER TABLE `Books`
  ADD PRIMARY KEY (`Book_ID`);

--
-- Indexes for table `Book_Categories`
--
ALTER TABLE `Book_Categories`
  ADD PRIMARY KEY (`Category_ID`);

--
-- Indexes for table `Borrowed_Books`
--
ALTER TABLE `Borrowed_Books`
  ADD PRIMARY KEY (`Borrow_ID`),
  ADD KEY `Book_ID` (`Book_ID`),
  ADD KEY `Student_ID` (`Student_ID`);

--
-- Indexes for table `Borrowing_History`
--
ALTER TABLE `Borrowing_History`
  ADD PRIMARY KEY (`Borrow_history_id`),
  ADD KEY `Student_ID` (`Student_ID`);

--
-- Indexes for table `Due_Books_Notifications`
--
ALTER TABLE `Due_Books_Notifications`
  ADD KEY `Book_ID` (`Book_ID`),
  ADD KEY `Student_ID` (`Student_ID`);

--
-- Indexes for table `Lost_Damaged_Reports`
--
ALTER TABLE `Lost_Damaged_Reports`
  ADD PRIMARY KEY (`Report_ID`),
  ADD KEY `Book_ID` (`Book_ID`),
  ADD KEY `Student_ID` (`Student_ID`);

--
-- Indexes for table `Most_Borrowed_Books`
--
ALTER TABLE `Most_Borrowed_Books`
  ADD KEY `Book_ID` (`Book_ID`);

--
-- Indexes for table `Penalties_Fines`
--
ALTER TABLE `Penalties_Fines`
  ADD PRIMARY KEY (`Penalty_ID`),
  ADD KEY `Student_ID` (`Student_ID`);

--
-- Indexes for table `Reservation_Approvals`
--
ALTER TABLE `Reservation_Approvals`
  ADD PRIMARY KEY (`Reservation_ID`),
  ADD KEY `Student_ID` (`Student_ID`);

--
-- Indexes for table `Reserved_Books`
--
ALTER TABLE `Reserved_Books`
  ADD PRIMARY KEY (`Reservation_ID`),
  ADD KEY `Book_ID` (`Book_ID`),
  ADD KEY `Student_ID` (`Student_ID`);

--
-- Indexes for table `Students`
--
ALTER TABLE `Students`
  ADD PRIMARY KEY (`Student_ID`);

--
-- Indexes for table `System_Logs`
--
ALTER TABLE `System_Logs`
  ADD PRIMARY KEY (`Log_ID`);

--
-- Indexes for table `Visit_Logs`
--
ALTER TABLE `Visit_Logs`
  ADD PRIMARY KEY (`Visit_ID`),
  ADD KEY `Student_ID` (`Student_ID`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `Book_Categories`
--
ALTER TABLE `Book_Categories`
  MODIFY `Category_ID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `Borrowed_Books`
--
ALTER TABLE `Borrowed_Books`
  MODIFY `Borrow_ID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `Borrowing_History`
--
ALTER TABLE `Borrowing_History`
  MODIFY `Borrow_history_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `Lost_Damaged_Reports`
--
ALTER TABLE `Lost_Damaged_Reports`
  MODIFY `Report_ID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `Penalties_Fines`
--
ALTER TABLE `Penalties_Fines`
  MODIFY `Penalty_ID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `System_Logs`
--
ALTER TABLE `System_Logs`
  MODIFY `Log_ID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=54;

--
-- AUTO_INCREMENT for table `Visit_Logs`
--
ALTER TABLE `Visit_Logs`
  MODIFY `Visit_ID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `Borrowed_Books`
--
ALTER TABLE `Borrowed_Books`
  ADD CONSTRAINT `borrowed_books_ibfk_1` FOREIGN KEY (`Book_ID`) REFERENCES `Books` (`Book_ID`),
  ADD CONSTRAINT `borrowed_books_ibfk_2` FOREIGN KEY (`Student_ID`) REFERENCES `Students` (`Student_ID`);

--
-- Constraints for table `Borrowing_History`
--
ALTER TABLE `Borrowing_History`
  ADD CONSTRAINT `borrowing_history_ibfk_1` FOREIGN KEY (`Student_ID`) REFERENCES `Students` (`Student_ID`);

--
-- Constraints for table `Due_Books_Notifications`
--
ALTER TABLE `Due_Books_Notifications`
  ADD CONSTRAINT `due_books_notifications_ibfk_1` FOREIGN KEY (`Book_ID`) REFERENCES `Books` (`Book_ID`),
  ADD CONSTRAINT `due_books_notifications_ibfk_2` FOREIGN KEY (`Student_ID`) REFERENCES `Students` (`Student_ID`);

--
-- Constraints for table `Lost_Damaged_Reports`
--
ALTER TABLE `Lost_Damaged_Reports`
  ADD CONSTRAINT `lost_damaged_reports_ibfk_1` FOREIGN KEY (`Book_ID`) REFERENCES `Books` (`Book_ID`),
  ADD CONSTRAINT `lost_damaged_reports_ibfk_2` FOREIGN KEY (`Student_ID`) REFERENCES `Students` (`Student_ID`);

--
-- Constraints for table `Most_Borrowed_Books`
--
ALTER TABLE `Most_Borrowed_Books`
  ADD CONSTRAINT `most_borrowed_books_ibfk_1` FOREIGN KEY (`Book_ID`) REFERENCES `Books` (`Book_ID`);

--
-- Constraints for table `Penalties_Fines`
--
ALTER TABLE `Penalties_Fines`
  ADD CONSTRAINT `penalties_fines_ibfk_1` FOREIGN KEY (`Student_ID`) REFERENCES `Students` (`Student_ID`);

--
-- Constraints for table `Reservation_Approvals`
--
ALTER TABLE `Reservation_Approvals`
  ADD CONSTRAINT `reservation_approvals_ibfk_1` FOREIGN KEY (`Reservation_ID`) REFERENCES `Reserved_Books` (`Reservation_ID`),
  ADD CONSTRAINT `reservation_approvals_ibfk_2` FOREIGN KEY (`Student_ID`) REFERENCES `Students` (`Student_ID`);

--
-- Constraints for table `Reserved_Books`
--
ALTER TABLE `Reserved_Books`
  ADD CONSTRAINT `reserved_books_ibfk_1` FOREIGN KEY (`Book_ID`) REFERENCES `Books` (`Book_ID`),
  ADD CONSTRAINT `reserved_books_ibfk_2` FOREIGN KEY (`Student_ID`) REFERENCES `Students` (`Student_ID`);

--
-- Constraints for table `Visit_Logs`
--
ALTER TABLE `Visit_Logs`
  ADD CONSTRAINT `visit_logs_student_fk` FOREIGN KEY (`Student_ID`) REFERENCES `Students` (`Student_ID`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
