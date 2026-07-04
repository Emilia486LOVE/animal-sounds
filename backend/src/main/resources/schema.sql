CREATE DATABASE IF NOT EXISTS animal_voiceprint DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE animal_voiceprint;

CREATE TABLE IF NOT EXISTS sys_user (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    role VARCHAR(20) NOT NULL DEFAULT 'annotator',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_role (role),
    INDEX idx_user_status (status),
    INDEX idx_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS taxonomy_label (
    label_id INT PRIMARY KEY AUTO_INCREMENT,
    label_name VARCHAR(100) NOT NULL,
    parent_id INT DEFAULT 0,
    taxon_rank VARCHAR(20) NOT NULL,
    description VARCHAR(255),
    label_path VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_taxon_rank (taxon_rank),
    INDEX idx_label_name (label_name),
    INDEX idx_label_path (label_path(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS dataset (
    dataset_id INT PRIMARY KEY AUTO_INCREMENT,
    dataset_name VARCHAR(100) NOT NULL,
    description TEXT,
    create_user_id INT,
    audio_count INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (create_user_id) REFERENCES sys_user(user_id),
    INDEX idx_create_user_id (create_user_id),
    INDEX idx_dataset_name (dataset_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS audio_file (
    audio_id INT PRIMARY KEY AUTO_INCREMENT,
    dataset_id INT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    duration DECIMAL(8,3),
    sample_rate INT,
    channels TINYINT,
    file_size BIGINT,
    noise_level VARCHAR(10) DEFAULT 'unknown',
    location VARCHAR(100),
    upload_user_id INT,
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(255),
    FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id),
    FOREIGN KEY (upload_user_id) REFERENCES sys_user(user_id),
    INDEX idx_dataset_id (dataset_id),
    INDEX idx_noise_level (noise_level),
    INDEX idx_location (location),
    INDEX idx_upload_user_id (upload_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS annotation_record (
    annotation_id INT PRIMARY KEY AUTO_INCREMENT,
    audio_id INT,
    annotator_id INT,
    start_time DECIMAL(8,3) NOT NULL,
    end_time DECIMAL(8,3) NOT NULL,
    label_id INT,
    sound_type VARCHAR(20),
    confidence TINYINT,
    remark VARCHAR(255),
    status VARCHAR(20) DEFAULT 'submitted',
    reviewer_id INT,
    review_remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (audio_id) REFERENCES audio_file(audio_id),
    FOREIGN KEY (annotator_id) REFERENCES sys_user(user_id),
    FOREIGN KEY (label_id) REFERENCES taxonomy_label(label_id),
    FOREIGN KEY (reviewer_id) REFERENCES sys_user(user_id),
    INDEX idx_audio_id (audio_id),
    INDEX idx_label_id (label_id),
    INDEX idx_status (status),
    INDEX idx_annotator_id (annotator_id),
    INDEX idx_reviewer_id (reviewer_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS train_task (
    task_id INT PRIMARY KEY AUTO_INCREMENT,
    task_name VARCHAR(100) NOT NULL,
    dataset_id INT,
    model_type VARCHAR(50) NOT NULL,
    train_params JSON,
    enable_hierarchical_loss TINYINT DEFAULT 1,
    status VARCHAR(20) DEFAULT 'pending',
    create_user_id INT,
    model_save_path VARCHAR(500),
    error_msg TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    start_time DATETIME,
    end_time DATETIME,
    current_epoch INT DEFAULT 0,
    best_model_path VARCHAR(500),
    best_val_metric DECIMAL(5,4),
    checkpoint_path VARCHAR(500),
    FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id),
    FOREIGN KEY (create_user_id) REFERENCES sys_user(user_id),
    INDEX idx_dataset_id (dataset_id),
    INDEX idx_status (status),
    INDEX idx_create_user_id (create_user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS evaluation_result (
    eval_id INT PRIMARY KEY AUTO_INCREMENT,
    task_id INT,
    taxon_rank VARCHAR(20) NOT NULL,
    accuracy DECIMAL(5,4),
    precision DECIMAL(5,4),
    recall DECIMAL(5,4),
    f1_score DECIMAL(5,4),
    confusion_matrix_path VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES train_task(task_id),
    INDEX idx_task_id (task_id),
    INDEX idx_taxon_rank (taxon_rank),
    UNIQUE KEY uk_task_rank (task_id, taxon_rank)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS train_sample (
    sample_id INT PRIMARY KEY AUTO_INCREMENT,
    task_id INT,
    annotation_id INT,
    split VARCHAR(10) DEFAULT 'train',
    FOREIGN KEY (task_id) REFERENCES train_task(task_id),
    FOREIGN KEY (annotation_id) REFERENCES annotation_record(annotation_id),
    INDEX idx_task_id (task_id),
    INDEX idx_annotation_id (annotation_id),
    INDEX idx_split (split),
    UNIQUE KEY uk_task_annotation (task_id, annotation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;