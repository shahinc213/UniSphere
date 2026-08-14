-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Mar 25, 2025 at 09:11 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `unishpere`
--

-- --------------------------------------------------------

--
-- Table structure for table `chat_history`
--

CREATE TABLE `chat_history` (
  `id` int(11) NOT NULL,
  `sender_email` varchar(255) DEFAULT NULL,
  `receiver_email` varchar(255) DEFAULT NULL,
  `message_text` text DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `chat_history`
--

INSERT INTO `chat_history` (`id`, `sender_email`, `receiver_email`, `message_text`, `timestamp`) VALUES
(1, 'arnab0574@gmail.com', 'schowdhury222152@bscse.uiu.ac.bd', 'Hey', '2025-01-24 13:25:48'),
(2, 'arnab0574@gmail.com', 'schowdhury222152@bscse.uiu.ac.bd', 'What\'s up?', '2025-01-24 13:25:55'),
(3, 'schowdhury222152@bscse.uiu.ac.bd', 'arnab0574@gmail.com', 'Great...', '2025-01-24 13:27:44'),
(4, 'arnab0574@gmail.com', 'schowdhury222152@bscse.uiu.ac.bd', 'What are you doing??', '2025-01-24 15:09:34'),
(6, 'arnab0574@gmail.com', 'schowdhury222152@bscse.uiu.ac.bd', 'Hey..', '2025-01-24 17:26:20'),
(7, 'schowdhury222152@bscse.uiu.ac.bd', 'arnab0574@gmail.com', 'Hello', '2025-01-24 17:29:40'),
(8, 'cr7@gmail.com', 'arnab0574@gmail.com', 'Hello', '2025-01-26 18:22:33'),
(9, 'arnab0574@gmail.com', 'cr7@gmail.com', 'Hey', '2025-01-26 20:22:46'),
(10, 'arnab0574@gmail.com', 'schowdhury222152@bscse.uiu.ac.bd', 'hey', '2025-01-27 04:19:28'),
(11, 'arnab0574@gmail.com', 'schowdhury222152@bscse.uiu.ac.bd', 'hello', '2025-01-27 04:19:45'),
(12, 'arnab0574@gmail.com', 'cr7@gmail.com', 'Hey', '2025-01-30 18:35:19'),
(13, 'cr7@gmail.com', 'arnab0574@gmail.com', 'Hello', '2025-01-30 18:35:26');

-- --------------------------------------------------------

--
-- Table structure for table `clothingrental`
--

CREATE TABLE `clothingrental` (
  `id` int(11) NOT NULL,
  `type` varchar(100) NOT NULL,
  `size` varchar(50) NOT NULL,
  `color` varchar(50) NOT NULL,
  `gender` varchar(50) NOT NULL,
  `rent_price` decimal(10,2) NOT NULL,
  `image_path` varchar(255) NOT NULL,
  `lessor` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `clothingrental`
--

INSERT INTO `clothingrental` (`id`, `type`, `size`, `color`, `gender`, `rent_price`, `image_path`, `lessor`, `created_at`) VALUES
(1, 'Suit', 'XL', 'Grey', 'Male', 200.00, 'img/cloths_rental/cloths_1737322740837_suit.jpg', 1, '2025-01-19 21:39:00'),
(2, 'Saree', 'N/A', 'Red', 'Female', 300.00, 'img/cloths_rental/cloths_1737323170071_saree.jpg', 2, '2025-01-19 21:46:10'),
(3, 'Shirt', 'XL', 'White', 'Male', 200.00, 'img/cloths_rental/cloths_1737951227693_cloths_1737321810160_suit.jpg', 1, '2025-01-27 04:13:47'),
(4, 'Shirt', 'X', 'REd', 'male', 100.00, 'img/cloths_rental/cloths_1737951783602_cloths_1737323170071_saree.jpg', 2, '2025-01-27 04:23:03');

-- --------------------------------------------------------

--
-- Table structure for table `courses`
--

CREATE TABLE `courses` (
  `course_id` int(11) NOT NULL,
  `course_name` varchar(255) NOT NULL,
  `department_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `courses`
--

INSERT INTO `courses` (`course_id`, `course_name`, `department_id`) VALUES
(1, 'Computer Networks', 1),
(2, 'Society, Environment and Computing Ethics', 1),
(3, 'Microprocessors and Microcontrollers', 1);

-- --------------------------------------------------------

--
-- Table structure for table `cycles`
--

CREATE TABLE `cycles` (
  `cycle_id` int(11) NOT NULL,
  `location` varchar(255) NOT NULL,
  `is_available` tinyint(4) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `cycles`
--

INSERT INTO `cycles` (`cycle_id`, `location`, `is_available`) VALUES
(1, 'Notunbazr', 1),
(2, 'Notunbazr', 1),
(3, 'Notunbazr', 1),
(4, 'UIU Campus', 1),
(5, 'NotunBazar', 1),
(6, 'NotunBazar', 1),
(7, 'NotunBazar', 1),
(8, 'Notunbazar', 0);

-- --------------------------------------------------------

--
-- Table structure for table `cycle_rentals`
--

CREATE TABLE `cycle_rentals` (
  `rental_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `cycle_id` int(11) NOT NULL,
  `rental_start_time` datetime NOT NULL,
  `rental_end_time` datetime DEFAULT NULL,
  `status` enum('rented','returned') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `cycle_rentals`
--

INSERT INTO `cycle_rentals` (`rental_id`, `user_id`, `cycle_id`, `rental_start_time`, `rental_end_time`, `status`) VALUES
(1, 1, 4, '2025-01-26 01:58:55', '2025-01-26 01:59:33', 'returned'),
(2, 1, 4, '2025-01-26 02:04:26', '2025-01-26 02:04:48', 'returned'),
(3, 1, 4, '2025-01-26 02:10:15', '2025-01-26 02:10:24', 'returned'),
(4, 1, 5, '2025-01-26 02:12:04', '2025-01-26 02:12:09', 'returned'),
(5, 2, 6, '2025-01-26 14:58:58', '2025-01-26 14:59:09', 'returned'),
(6, 2, 7, '2025-01-26 16:03:58', '2025-01-26 16:04:06', 'returned'),
(7, 2, 4, '2025-01-27 10:27:54', '2025-01-27 10:28:03', 'returned');

-- --------------------------------------------------------

--
-- Table structure for table `departments`
--

CREATE TABLE `departments` (
  `department_id` int(11) NOT NULL,
  `department_name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `departments`
--

INSERT INTO `departments` (`department_id`, `department_name`) VALUES
(1, 'Computer Science and Engineering (CSE)'),
(2, 'Bachelor of Business Administration (BBA)'),
(3, 'Electrical and Electronic Engineering (EEE)');

-- --------------------------------------------------------

--
-- Table structure for table `peertutoring`
--

CREATE TABLE `peertutoring` (
  `id` int(11) NOT NULL,
  `requester_id` int(11) NOT NULL,
  `accepter_id` int(11) DEFAULT NULL,
  `course_name` varchar(255) NOT NULL,
  `problem_topic` varchar(255) NOT NULL,
  `description` text NOT NULL,
  `created_time` timestamp NOT NULL DEFAULT current_timestamp(),
  `type` enum('pending','accepted') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `peertutoring`
--

INSERT INTO `peertutoring` (`id`, `requester_id`, `accepter_id`, `course_name`, `problem_topic`, `description`, `created_time`, `type`) VALUES
(1, 1, 3, 'Computer Networkings', 'Dynamic Routing', 'I\'m facing some difficulties in DHcP...', '2025-01-10 14:52:19', 'accepted'),
(2, 2, 1, 'AOOP', 'Socket Programming', 'Can someone teach me to add Threading on SocketProgramming.', '2025-01-10 15:15:24', 'accepted'),
(3, 1, 2, 'AOOP', 'Socket', '123wdefse', '2025-01-12 10:25:17', 'pending');

-- --------------------------------------------------------

--
-- Table structure for table `posts`
--

CREATE TABLE `posts` (
  `post_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `caption` text DEFAULT NULL,
  `photo_url` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `posts`
--

INSERT INTO `posts` (`post_id`, `user_id`, `caption`, `photo_url`, `created_at`) VALUES
(7, 1, 'To-let for male! Preferable for UIU or DIU students. A big master bedroom attached with balcony and washroom available from February. 2 person can easily share. Room rent: 7k, without utilities, proper meal system. Location: Sayed Nagar, B Block, Muktijuddha Chottor, Bhandarir Mor. Contact: 01785585936 or inbox me.', 'C:\\Users\\Arnab\\OneDrive\\Desktop\\UniShpere\\uniShpere\\src\\main\\resources\\img\\To_Let.png', '2025-01-09 15:39:30'),
(8, 2, 'One room available from January. Rent : 6000/-', 'C:\\Users\\Arnab\\OneDrive\\Pictures\\Screenshots\\Screenshot 2024-11-17 214803.png', '2025-01-09 15:42:37'),
(9, 1, 'Helloo....Everyone..', 'C:\\Users\\Arnab\\OneDrive\\Pictures\\Screenshots\\222.png', '2025-01-11 15:05:01'),
(10, 1, 'safawfdfh', 'C:\\Users\\Arnab\\OneDrive\\Desktop\\UniShpere\\uniShpere\\src\\main\\resources\\img\\defaultPhoto.png', '2025-01-12 10:23:38');

-- --------------------------------------------------------

--
-- Table structure for table `questions`
--

CREATE TABLE `questions` (
  `question_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `trimester_id` int(11) NOT NULL,
  `question_type_id` int(11) NOT NULL,
  `question_file_path` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `question_types`
--

CREATE TABLE `question_types` (
  `question_type_id` int(11) NOT NULL,
  `type_name` enum('Midterm','Final') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `question_types`
--

INSERT INTO `question_types` (`question_type_id`, `type_name`) VALUES
(1, 'Midterm'),
(2, 'Final');

-- --------------------------------------------------------

--
-- Table structure for table `seat_selections`
--

CREATE TABLE `seat_selections` (
  `seat_number` varchar(5) NOT NULL,
  `user_email` varchar(100) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `seat_selections`
--

INSERT INTO `seat_selections` (`seat_number`, `user_email`, `update_time`) VALUES
('A1', 'arnab0574@gmail.com', '2025-01-31 15:06:18');

-- --------------------------------------------------------

--
-- Table structure for table `shuttle`
--

CREATE TABLE `shuttle` (
  `total_seat` int(11) DEFAULT NULL,
  `available` int(11) DEFAULT NULL,
  `selected` int(11) DEFAULT NULL,
  `shuttleNo` int(11) DEFAULT NULL,
  `id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `shuttle`
--

INSERT INTO `shuttle` (`total_seat`, `available`, `selected`, `shuttleNo`, `id`) VALUES
(28, 28, 0, NULL, 1);

-- --------------------------------------------------------

--
-- Table structure for table `shuttlenum`
--

CREATE TABLE `shuttlenum` (
  `id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `shuttle_group_chat`
--

CREATE TABLE `shuttle_group_chat` (
  `id` int(11) NOT NULL,
  `user_email` varchar(100) DEFAULT NULL,
  `user_name` varchar(100) DEFAULT NULL,
  `message` text DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `shuttle_group_chat`
--

INSERT INTO `shuttle_group_chat` (`id`, `user_email`, `user_name`, `message`, `timestamp`) VALUES
(1, 'arnab0574@gmail.com', 'arnab0574@gmail.com', 'Hello everyone...', '2025-01-26 18:02:35'),
(2, 'schowdhury222152@bscse.uiu.ac.bd', 'schowdhury222152@bscse.uiu.ac.bd', 'Hey...', '2025-01-26 18:07:50'),
(3, 'cr7@gmail.com', 'Cristiano Ronaldo', 'Bhai...Shuttle kothay??? *_*', '2025-01-26 18:21:44'),
(4, 'arnab0574@gmail.com', 'Mubasshir Ahmed', 'Notunbazar.', '2025-01-26 18:24:15');

-- --------------------------------------------------------

--
-- Table structure for table `trimesters`
--

CREATE TABLE `trimesters` (
  `trimester_id` int(11) NOT NULL,
  `trimester_name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `trimesters`
--

INSERT INTO `trimesters` (`trimester_id`, `trimester_name`) VALUES
(1, 'Summer 22'),
(2, 'Summer 23'),
(3, 'Fall 23'),
(4, 'Spring 24');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `student_id` varchar(20) NOT NULL,
  `password` varchar(255) NOT NULL,
  `cPassword` varchar(255) NOT NULL,
  `profile_photo` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `first_name`, `last_name`, `email`, `student_id`, `password`, `cPassword`, `profile_photo`) VALUES
(1, 'Mubasshir', 'Ahmed', 'arnab0574@gmail.com', '011222263', '12345', '12345', 'file:/C:/Users/Arnab/OneDrive/Pictures/Arnab%203.jpeg'),
(2, 'Shahin', 'Chowdhury', 'schowdhury222152@bscse.uiu.ac.bd', '011222152', 'shahin123', 'shahin123', 'file:/C:/Users/Arnab/OneDrive/Pictures/Shahin.jpg'),
(3, 'Cristiano', 'Ronaldo', 'cr7@gmail.com', '011222111', '123', '123', 'file:/C:/Users/Arnab/OneDrive/Pictures/Screenshots/Ronaldo.jpg');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `chat_history`
--
ALTER TABLE `chat_history`
  ADD PRIMARY KEY (`id`),
  ADD KEY `sender_email` (`sender_email`),
  ADD KEY `receiver_email` (`receiver_email`);

--
-- Indexes for table `clothingrental`
--
ALTER TABLE `clothingrental`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_lessor` (`lessor`);

--
-- Indexes for table `courses`
--
ALTER TABLE `courses`
  ADD PRIMARY KEY (`course_id`),
  ADD KEY `department_id` (`department_id`);

--
-- Indexes for table `cycles`
--
ALTER TABLE `cycles`
  ADD PRIMARY KEY (`cycle_id`);

--
-- Indexes for table `cycle_rentals`
--
ALTER TABLE `cycle_rentals`
  ADD PRIMARY KEY (`rental_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `cycle_id` (`cycle_id`);

--
-- Indexes for table `departments`
--
ALTER TABLE `departments`
  ADD PRIMARY KEY (`department_id`);

--
-- Indexes for table `peertutoring`
--
ALTER TABLE `peertutoring`
  ADD PRIMARY KEY (`id`),
  ADD KEY `requester_id` (`requester_id`),
  ADD KEY `accepter_id` (`accepter_id`);

--
-- Indexes for table `posts`
--
ALTER TABLE `posts`
  ADD PRIMARY KEY (`post_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `questions`
--
ALTER TABLE `questions`
  ADD PRIMARY KEY (`question_id`),
  ADD KEY `course_id` (`course_id`),
  ADD KEY `trimester_id` (`trimester_id`),
  ADD KEY `question_type_id` (`question_type_id`);

--
-- Indexes for table `question_types`
--
ALTER TABLE `question_types`
  ADD PRIMARY KEY (`question_type_id`);

--
-- Indexes for table `seat_selections`
--
ALTER TABLE `seat_selections`
  ADD PRIMARY KEY (`seat_number`);

--
-- Indexes for table `shuttle`
--
ALTER TABLE `shuttle`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_shuttle_shuttleno` (`shuttleNo`);

--
-- Indexes for table `shuttlenum`
--
ALTER TABLE `shuttlenum`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `shuttle_group_chat`
--
ALTER TABLE `shuttle_group_chat`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `trimesters`
--
ALTER TABLE `trimesters`
  ADD PRIMARY KEY (`trimester_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `student_id` (`student_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `chat_history`
--
ALTER TABLE `chat_history`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `clothingrental`
--
ALTER TABLE `clothingrental`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `courses`
--
ALTER TABLE `courses`
  MODIFY `course_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `cycles`
--
ALTER TABLE `cycles`
  MODIFY `cycle_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `cycle_rentals`
--
ALTER TABLE `cycle_rentals`
  MODIFY `rental_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `departments`
--
ALTER TABLE `departments`
  MODIFY `department_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `peertutoring`
--
ALTER TABLE `peertutoring`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `posts`
--
ALTER TABLE `posts`
  MODIFY `post_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `questions`
--
ALTER TABLE `questions`
  MODIFY `question_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `question_types`
--
ALTER TABLE `question_types`
  MODIFY `question_type_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `shuttle`
--
ALTER TABLE `shuttle`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `shuttlenum`
--
ALTER TABLE `shuttlenum`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `shuttle_group_chat`
--
ALTER TABLE `shuttle_group_chat`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `trimesters`
--
ALTER TABLE `trimesters`
  MODIFY `trimester_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `chat_history`
--
ALTER TABLE `chat_history`
  ADD CONSTRAINT `chat_history_ibfk_1` FOREIGN KEY (`sender_email`) REFERENCES `users` (`email`),
  ADD CONSTRAINT `chat_history_ibfk_2` FOREIGN KEY (`receiver_email`) REFERENCES `users` (`email`);

--
-- Constraints for table `clothingrental`
--
ALTER TABLE `clothingrental`
  ADD CONSTRAINT `fk_lessor` FOREIGN KEY (`lessor`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `courses`
--
ALTER TABLE `courses`
  ADD CONSTRAINT `courses_ibfk_1` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`) ON DELETE CASCADE;

--
-- Constraints for table `cycle_rentals`
--
ALTER TABLE `cycle_rentals`
  ADD CONSTRAINT `cycle_rentals_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `cycle_rentals_ibfk_2` FOREIGN KEY (`cycle_id`) REFERENCES `cycles` (`cycle_id`);

--
-- Constraints for table `peertutoring`
--
ALTER TABLE `peertutoring`
  ADD CONSTRAINT `peertutoring_ibfk_1` FOREIGN KEY (`requester_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `peertutoring_ibfk_2` FOREIGN KEY (`accepter_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `posts`
--
ALTER TABLE `posts`
  ADD CONSTRAINT `posts_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `questions`
--
ALTER TABLE `questions`
  ADD CONSTRAINT `questions_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `questions_ibfk_2` FOREIGN KEY (`trimester_id`) REFERENCES `trimesters` (`trimester_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `questions_ibfk_3` FOREIGN KEY (`question_type_id`) REFERENCES `question_types` (`question_type_id`) ON DELETE CASCADE;

--
-- Constraints for table `shuttle`
--
ALTER TABLE `shuttle`
  ADD CONSTRAINT `fk_shuttle_shuttleno` FOREIGN KEY (`shuttleNo`) REFERENCES `shuttlenum` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
