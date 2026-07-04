INSERT INTO sys_user (username, password_hash, real_name, role, status) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '系统管理员', 'admin', 1),
('annotator1', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '标注员张三', 'annotator', 1),
('algorithm1', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '算法工程师李四', 'algorithm', 1),
('guest1', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '访客王五', 'guest', 1);

INSERT INTO taxonomy_label (label_name, parent_id, taxon_rank, description, label_path) VALUES
('动物界', 0, 'kingdom', '动物界', '0/1'),
('脊索动物门', 1, 'phylum', '脊索动物门', '0/1/2'),
('哺乳纲', 2, 'class', '哺乳纲', '0/1/2/3'),
('鸟纲', 2, 'class', '鸟纲', '0/1/2/4'),
('食肉目', 3, 'order', '食肉目', '0/1/2/3/5'),
('灵长目', 3, 'order', '灵长目', '0/1/2/3/6'),
('雀形目', 4, 'order', '雀形目', '0/1/2/4/7'),
('猫科', 5, 'family', '猫科', '0/1/2/3/5/8'),
('犬科', 5, 'family', '犬科', '0/1/2/3/5/9'),
('人科', 6, 'family', '人科', '0/1/2/3/6/10'),
('猫属', 8, 'genus', '猫属', '0/1/2/3/5/8/11'),
('豹属', 8, 'genus', '豹属', '0/1/2/3/5/8/12'),
('犬属', 9, 'genus', '犬属', '0/1/2/3/5/9/13'),
('家猫', 11, 'species', '家猫', '0/1/2/3/5/8/11/14'),
('老虎', 12, 'species', '老虎', '0/1/2/3/5/8/12/15'),
('狮子', 12, 'species', '狮子', '0/1/2/3/5/8/12/16'),
('灰狼', 13, 'species', '灰狼', '0/1/2/3/5/9/13/17'),
('家犬', 13, 'species', '家犬', '0/1/2/3/5/9/13/18');

INSERT INTO dataset (dataset_name, description, create_user_id, audio_count) VALUES
('测试数据集1', '用于模型测试的动物声纹数据集', 1, 0),
('猫科动物数据集', '猫科动物声纹数据', 1, 0),
('犬科动物数据集', '犬科动物声纹数据', 1, 0);