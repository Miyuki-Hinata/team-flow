-- ============================================================
-- data.sql — TeamFlow 初期データ（さくら総合病院・多職種シナリオ）
-- ============================================================
-- 方針：
--  ・起動のたびに「リセット→再投入」する（Option A）。常に既知の綺麗な状態＝ポートフォリオのデモに最適。
--    タスクの期限は CURDATE() 相対で生成するので、いつ起動しても「本日のタイムライン」が埋まる。
--  ・本来はマスタ（部署/職員/カテゴリ）とデモ（患者/タスク/お知らせ）を分けて、
--    デモは dev プロファイルだけに隔離するのが本番の定石。
--    ただし本プロジェクトは「公開デモ＝データが入っている方が良い」ポートフォリオなので、
--    概念は下記コメントで区分しつつ、物理的には常時投入する。
--  ・全ユーザーのパスワードは共通で「admin1234」（採用担当が各職種でログインして見比べられるように）。
-- ============================================================

-- ---------- リセット（FK を一時無効化して全消去）----------
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM related_tasks;
DELETE FROM task_assignees;
DELETE FROM user_patient_assignments;
DELETE FROM announcement_reads;
DELETE FROM announcement_histories;
DELETE FROM task_histories;
DELETE FROM task_summaries;
DELETE FROM password_change_logs;
DELETE FROM refresh_tokens;
DELETE FROM tasks;
DELETE FROM announcements;
DELETE FROM patients;
DELETE FROM projects;
DELETE FROM categories;
DELETE FROM users;
DELETE FROM departments;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 【マスタ】部署
-- ============================================================
INSERT INTO departments (id, department_name, created_at, updated_at, deleted_at, updated_by) VALUES
 (1,  '内科病棟',          NOW(), NOW(), NULL, NULL),
 (2,  '外科病棟',          NOW(), NOW(), NULL, NULL),
 (3,  '整形外科病棟',      NOW(), NOW(), NULL, NULL),
 (4,  'ICU',              NOW(), NOW(), NULL, NULL),
 (5,  '外来',              NOW(), NOW(), NULL, NULL),
 (6,  '薬剤部',            NOW(), NOW(), NULL, NULL),
 (7,  'リハビリテーション科', NOW(), NOW(), NULL, NULL),
 (8,  '放射線科',          NOW(), NOW(), NULL, NULL),
 (9,  '臨床検査科',        NOW(), NOW(), NULL, NULL),
 (10, '栄養科',            NOW(), NOW(), NULL, NULL),
 (11, '地域連携室',        NOW(), NOW(), NULL, NULL);

-- ============================================================
-- 【マスタ】職員ユーザー（多職種チーム）／全員パスワード = admin1234
-- password 列は "admin1234" の bcrypt ハッシュ（htpasswd -bnBC 10 で生成・検証済み）。全員共通。
-- ============================================================
INSERT INTO users (id, login_id, last_name, first_name, last_name_kana, first_name_kana, email, password, level, role, department_id, created_at, updated_at, deleted_at, updated_by) VALUES
 (1,  'admin',       '管理者',   'ユーザー', 'かんりしゃ', 'ゆーざー', 'admin@sakura-hp.jp',       '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 2, 'DOCTOR',        1,  NOW(), NOW(), NULL, NULL),
 (2,  'doctor',      '佐藤',     '健一',     'さとう',     'けんいち', 'doctor@sakura-hp.jp',      '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 2, 'DOCTOR',        2,  NOW(), NOW(), NULL, NULL),
 (3,  'doctor2',     '山田',     '大輔',     'やまだ',     'だいすけ', 'doctor2@sakura-hp.jp',     '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 2, 'DOCTOR',        1,  NOW(), NOW(), NULL, NULL),
 (4,  'doctor3',     '田中',     '誠',       'たなか',     'まこと',   'doctor3@sakura-hp.jp',     '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 2, 'DOCTOR',        3,  NOW(), NOW(), NULL, NULL),
 (5,  'doctor4',     '伊藤',     '洋子',     'いとう',     'ようこ',   'doctor4@sakura-hp.jp',     '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 2, 'DOCTOR',        4,  NOW(), NOW(), NULL, NULL),
 (6,  'doctor5',     '中村',     '隆',       'なかむら',   'たかし',   'doctor5@sakura-hp.jp',     '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 2, 'DOCTOR',        5,  NOW(), NOW(), NULL, NULL),
 (7,  'nurse',       '鈴木',     '美咲',     'すずき',     'みさき',   'nurse@sakura-hp.jp',       '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 1, 'NURSE',         1,  NOW(), NOW(), NULL, NULL),
 (8,  'nurse2',      '高橋',     '由美',     'たかはし',   'ゆみ',     'nurse2@sakura-hp.jp',      '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 1, 'NURSE',         1,  NOW(), NOW(), NULL, NULL),
 (9,  'nurse3',      '渡辺',     '彩',       'わたなべ',   'あや',     'nurse3@sakura-hp.jp',      '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 1, 'NURSE',         2,  NOW(), NOW(), NULL, NULL),
 (10, 'nurse4',      '小林',     '直子',     'こばやし',   'なおこ',   'nurse4@sakura-hp.jp',      '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 1, 'NURSE',         2,  NOW(), NOW(), NULL, NULL),
 (11, 'nurse5',      '加藤',     '恵',       'かとう',     'めぐみ',   'nurse5@sakura-hp.jp',      '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 1, 'NURSE',         3,  NOW(), NOW(), NULL, NULL),
 (12, 'nurse6',      '吉田',     '麻衣',     'よしだ',     'まい',     'nurse6@sakura-hp.jp',      '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 1, 'NURSE',         4,  NOW(), NOW(), NULL, NULL),
 (13, 'nurse7',      '山本',     '千夏',     'やまもと',   'ちなつ',   'nurse7@sakura-hp.jp',      '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 1, 'NURSE',         4,  NOW(), NOW(), NULL, NULL),
 (14, 'nurse8',      '松本',     '香織',     'まつもと',   'かおり',   'nurse8@sakura-hp.jp',      '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 1, 'NURSE',         5,  NOW(), NOW(), NULL, NULL),
 (15, 'pharmacist',  '井上',     '徹',       'いのうえ',   'とおる',   'pharmacist@sakura-hp.jp',  '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 1, 'PHARMACIST',    6,  NOW(), NOW(), NULL, NULL),
 (16, 'pharmacist2', '木村',     '花',       'きむら',     'はな',     'pharmacist2@sakura-hp.jp', '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 1, 'PHARMACIST',    6,  NOW(), NOW(), NULL, NULL),
 (17, 'pt',          '林',       '拓也',     'はやし',     'たくや',   'pt@sakura-hp.jp',          '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 1, 'PT',            7,  NOW(), NOW(), NULL, NULL),
 (18, 'pt2',         '清水',     '亮',       'しみず',     'りょう',   'pt2@sakura-hp.jp',         '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 1, 'PT',            7,  NOW(), NOW(), NULL, NULL),
 (19, 'ot',          '森田',     'さくら',   'もりた',     'さくら',   'ot@sakura-hp.jp',          '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 1, 'OT',            7,  NOW(), NOW(), NULL, NULL),
 (20, 'mt',          '池田',     '剛',       'いけだ',     'つよし',   'mt@sakura-hp.jp',          '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 1, 'MT',            9,  NOW(), NOW(), NULL, NULL),
 (21, 'radiologist', '橋本',     '学',       'はしもと',   'まなぶ',   'radiologist@sakura-hp.jp', '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 1, 'RADIOLOGIST',   8,  NOW(), NOW(), NULL, NULL),
 (22, 'dietitian',   '阿部',     'みどり',   'あべ',       'みどり',   'dietitian@sakura-hp.jp',   '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 1, 'DIETITIAN',     10, NOW(), NOW(), NULL, NULL),
 (23, 'msw',         '岡田',     '直樹',     'おかだ',     'なおき',   'msw@sakura-hp.jp',         '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 1, 'SOCIAL_WORKER', 11, NOW(), NOW(), NULL, NULL),
 (24, 'caremanager', '石川',     '京子',     'いしかわ',   'きょうこ', 'caremanager@sakura-hp.jp', '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 1, 'CARE_MANAGER',  11, NOW(), NOW(), NULL, NULL),
 (25, 'general',     '一般',     'ユーザー', 'いっぱん',   'ゆーざー', 'general@sakura-hp.jp',     '$2y$10$zMX/0U6HwOW6500ozkdpje3IiUN2TZq5cioBblHTeBn0R.4YdZ1Z.', 1, 'NURSE',         1,  NOW(), NOW(), NULL, NULL);

-- ============================================================
-- 【マスタ】カテゴリ（フロントの色分け：緊急=赤 / 連絡=青 / シフト・その他=グレー に対応）
-- ============================================================
INSERT INTO categories (id, category_name, created_at, updated_at, deleted_at, updated_by) VALUES
 (1, '与薬',    NOW(), NOW(), NULL, NULL),
 (2, '処置',    NOW(), NOW(), NULL, NULL),
 (3, '検査',    NOW(), NOW(), NULL, NULL),
 (4, 'リハビリ', NOW(), NOW(), NULL, NULL),
 (5, '記録',    NOW(), NOW(), NULL, NULL),
 (6, '連絡',    NOW(), NOW(), NULL, NULL),
 (7, '緊急',    NOW(), NOW(), NULL, NULL),
 (8, 'シフト',  NOW(), NOW(), NULL, NULL);

-- ============================================================
-- 【マスタ】プロジェクト（任意機能。少数）
-- ============================================================
INSERT INTO projects (id, project_name, department_id, created_at, updated_at, deleted_at, updated_by) VALUES
 (1, '内科病棟 業務改善プロジェクト', 1, NOW(), NOW(), NULL, NULL),
 (2, '外科病棟 感染対策プロジェクト', 2, NOW(), NOW(), NULL, NULL),
 (3, 'リハビリ 連携強化プロジェクト', 7, NOW(), NOW(), NULL, NULL);

-- ============================================================
-- 【サンプル】患者（年齢層・性別・病棟・担当医を分散）
-- ============================================================
INSERT INTO patients (id, last_name, first_name, last_name_kana, first_name_kana, birth, sex, address, tel, emergency_contact_name, emergency_contact_tel, doctor_id, department_id, created_at, updated_at, deleted_at, updated_by) VALUES
 (1,  '田村',   '花子', 'たむら',   'はなこ',   '1948-03-12', 'FEMALE', '東京都世田谷区桜1-2-3',  '090-1111-0001', '田村 一夫', '090-1111-1001', 3, 1, NOW(), NOW(), NULL, NULL),
 (2,  '佐々木', '太郎', 'ささき',   'たろう',   '1955-07-25', 'MALE',   '東京都杉並区和田4-5-6',  '090-1111-0002', '佐々木 花', '090-1111-1002', 3, 1, NOW(), NOW(), NULL, NULL),
 (3,  '中島',   '一郎', 'なかじま', 'いちろう', '1970-11-02', 'MALE',   '東京都中野区中央7-8-9',  '090-1111-0003', '中島 京子', '090-1111-1003', 2, 2, NOW(), NOW(), NULL, NULL),
 (4,  '藤田',   '美穂', 'ふじた',   'みほ',     '1985-05-18', 'FEMALE', '東京都練馬区豊玉1-1-1',  '090-1111-0004', '藤田 健',   '090-1111-1004', 2, 2, NOW(), NOW(), NULL, NULL),
 (5,  '岡本',   '大和', 'おかもと', 'やまと',   '2015-09-30', 'MALE',   '東京都板橋区常盤台2-2-2', '090-1111-0005', '岡本 里美', '090-1111-1005', 4, 3, NOW(), NOW(), NULL, NULL),
 (6,  '前田',   'さゆり', 'まえだ', 'さゆり',   '1938-01-08', 'FEMALE', '東京都北区赤羽3-3-3',    '090-1111-0006', '前田 淳',   '090-1111-1006', 4, 3, NOW(), NOW(), NULL, NULL),
 (7,  '石井',   '健',   'いしい',   'けん',     '1962-04-14', 'MALE',   '東京都足立区千住4-4-4',  '090-1111-0007', '石井 明子', '090-1111-1007', 5, 4, NOW(), NOW(), NULL, NULL),
 (8,  '村上',   '良子', 'むらかみ', 'りょうこ', '1950-12-20', 'FEMALE', '東京都葛飾区青戸5-5-5',  '090-1111-0008', '村上 修',   '090-1111-1008', 5, 4, NOW(), NOW(), NULL, NULL),
 (9,  '近藤',   '翔',   'こんどう', 'しょう',   '2010-02-11', 'MALE',   '東京都江戸川区船堀6-6-6', '090-1111-0009', '近藤 由紀', '090-1111-1009', 6, 1, NOW(), NOW(), NULL, NULL),
 (10, '坂本',   '千代', 'さかもと', 'ちよ',     '1945-06-06', 'FEMALE', '東京都目黒区自由が丘7-7', '090-1111-0010', '坂本 実',   '090-1111-1010', 3, 1, NOW(), NOW(), NULL, NULL),
 (11, '遠藤',   '学',   'えんどう', 'まなぶ',   '1978-08-22', 'MALE',   '東京都品川区大崎8-8-8',  '090-1111-0011', '遠藤 香',   '090-1111-1011', 2, 2, NOW(), NOW(), NULL, NULL),
 (12, '原田',   '明美', 'はらだ',   'あけみ',   '1990-10-15', 'FEMALE', '東京都大田区蒲田9-9-9',  '090-1111-0012', '原田 隆',   '090-1111-1012', 4, 3, NOW(), NOW(), NULL, NULL);

-- ============================================================
-- 【サンプル】タスク（期限は CURDATE() 相対＝いつ起動しても「今日」に並ぶ）
--   受け持ちデモ用に内科病棟の患者(1,2,9,10)へ今日タスクを厚めに配置。
--   10:00 に2件（患者1・患者2）→ タイムラインの「同時刻」グループ確認用。
--   期限超過(昨日)・完了(DONE)・未来(明日)も混在。DATE_ADD(CURDATE(), INTERVAL 分 MINUTE)。
-- ============================================================
INSERT INTO tasks (id, title, description, project_id, category_id, patient_id, assigned_to_all, priority, task_status, due_date, created_at, updated_at, deleted_at, updated_by) VALUES
 (1,  '朝の内服薬 与薬',        '降圧剤・血糖降下薬の与薬と確認',   NULL, 1, 1,  0, 'HIGH',   'PROGRESS', DATE_ADD(CURDATE(), INTERVAL 480 MINUTE),  NOW(), NOW(), NULL, NULL),
 (2,  '採血（血糖・電解質）',    '定時採血。スピッツ2本',           NULL, 3, 1,  0, 'MEDIUM', 'CREATED',  DATE_ADD(CURDATE(), INTERVAL 600 MINUTE),  NOW(), NOW(), NULL, NULL),
 (3,  '点滴ルート交換',         '末梢ルートの入れ替え',            NULL, 2, 1,  0, 'MEDIUM', 'CREATED',  DATE_ADD(CURDATE(), INTERVAL 900 MINUTE),  NOW(), NOW(), NULL, NULL),
 (4,  '看護記録の記入',         '前日夜勤帯の記録が未入力',        NULL, 5, 1,  0, 'HIGH',   'CREATED',  DATE_ADD(CURDATE(), INTERVAL -480 MINUTE), NOW(), NOW(), NULL, NULL),
 (5,  '食後薬 与薬',            '昼食後薬の配薬',                 NULL, 1, 2,  0, 'MEDIUM', 'CREATED',  DATE_ADD(CURDATE(), INTERVAL 540 MINUTE),  NOW(), NOW(), NULL, NULL),
 (6,  '歩行リハビリ付き添い',    '病棟内歩行訓練の付き添い',        NULL, 4, 2,  0, 'MEDIUM', 'CREATED',  DATE_ADD(CURDATE(), INTERVAL 600 MINUTE),  NOW(), NOW(), NULL, NULL),
 (7,  '胸部X線',               'ポータブル撮影',                 NULL, 3, 2,  0, 'LOW',    'DONE',     DATE_ADD(CURDATE(), INTERVAL 690 MINUTE),  NOW(), NOW(), NULL, NULL),
 (8,  '家族面談の調整',         '退院に向けた家族面談の日程調整',   NULL, 6, 9,  0, 'MEDIUM', 'CREATED',  DATE_ADD(CURDATE(), INTERVAL 810 MINUTE),  NOW(), NOW(), NULL, NULL),
 (9,  '夕食後薬 与薬',          '夕食後の内服確認',               NULL, 1, 9,  0, 'LOW',    'CREATED',  DATE_ADD(CURDATE(), INTERVAL 1080 MINUTE), NOW(), NOW(), NULL, NULL),
 (10, '栄養評価の記録',         '摂取量と栄養状態の記録',          NULL, 5, 10, 0, 'MEDIUM', 'CREATED',  DATE_ADD(CURDATE(), INTERVAL 1260 MINUTE), NOW(), NOW(), NULL, NULL),
 (11, '褥瘡ケア',              '仙骨部の処置とポジショニング',     NULL, 2, 10, 0, 'HIGH',   'PROGRESS', DATE_ADD(CURDATE(), INTERVAL -120 MINUTE), NOW(), NOW(), NULL, NULL),
 (12, '術後創部処置',          '創部の消毒とドレーン確認',         NULL, 2, 3,  0, 'HIGH',   'CREATED',  DATE_ADD(CURDATE(), INTERVAL 465 MINUTE),  NOW(), NOW(), NULL, NULL),
 (13, '抗生剤 点滴',           '術後抗生剤の投与',               NULL, 1, 3,  0, 'MEDIUM', 'CREATED',  DATE_ADD(CURDATE(), INTERVAL 960 MINUTE),  NOW(), NOW(), NULL, NULL),
 (14, '術前検査',              '術前の血液・凝固検査',            NULL, 3, 4,  0, 'MEDIUM', 'CREATED',  DATE_ADD(CURDATE(), INTERVAL 720 MINUTE),  NOW(), NOW(), NULL, NULL),
 (15, '退院支援の相談',         'MSWと退院後サービスの調整',       NULL, 6, 4,  0, 'LOW',    'CREATED',  DATE_ADD(CURDATE(), INTERVAL 1980 MINUTE), NOW(), NOW(), NULL, NULL),
 (16, '作業療法',              '上肢の作業療法',                 NULL, 4, 5,  0, 'MEDIUM', 'CREATED',  DATE_ADD(CURDATE(), INTERVAL 630 MINUTE),  NOW(), NOW(), NULL, NULL),
 (17, 'バイタル記録',          '検温・血圧・SpO2の記録',          NULL, 5, 5,  0, 'LOW',    'DONE',     DATE_ADD(CURDATE(), INTERVAL 420 MINUTE),  NOW(), NOW(), NULL, NULL),
 (18, 'ギプス固定の確認',       'ギプスのゆるみ・循環障害の確認',   NULL, 2, 6,  0, 'HIGH',   'CREATED',  DATE_ADD(CURDATE(), INTERVAL 570 MINUTE),  NOW(), NOW(), NULL, NULL),
 (19, 'リハビリ計画の作成',     '退院に向けた訓練計画',            3,    4, 6,  0, 'MEDIUM', 'CREATED',  DATE_ADD(CURDATE(), INTERVAL 2040 MINUTE), NOW(), NOW(), NULL, NULL),
 (20, '人工呼吸器設定確認',     '設定値と回路のチェック',          NULL, 7, 7,  0, 'HIGH',   'PROGRESS', DATE_ADD(CURDATE(), INTERVAL 495 MINUTE),  NOW(), NOW(), NULL, NULL),
 (21, '動脈血ガス分析',         'ABGの採取と提出',                NULL, 3, 7,  0, 'HIGH',   'CREATED',  DATE_ADD(CURDATE(), INTERVAL 750 MINUTE),  NOW(), NOW(), NULL, NULL),
 (22, '昇圧剤の調整',          '血圧に応じた投与量調整',          NULL, 1, 8,  0, 'HIGH',   'PROGRESS', DATE_ADD(CURDATE(), INTERVAL 600 MINUTE),  NOW(), NOW(), NULL, NULL),
 (23, 'ICU看護記録',          '夜勤帯の経過記録',               NULL, 5, 8,  0, 'MEDIUM', 'CREATED',  DATE_ADD(CURDATE(), INTERVAL 1140 MINUTE), NOW(), NOW(), NULL, NULL),
 (24, '内服確認',              '持参薬の内服確認',               NULL, 1, 11, 0, 'MEDIUM', 'DONE',     DATE_ADD(CURDATE(), INTERVAL 510 MINUTE),  NOW(), NOW(), NULL, NULL),
 (25, 'CT撮影',               '腹部造影CT',                    NULL, 3, 11, 0, 'MEDIUM', 'CREATED',  DATE_ADD(CURDATE(), INTERVAL 1920 MINUTE), NOW(), NOW(), NULL, NULL),
 (26, '退院前ADL評価',         '自宅復帰に向けたADL評価',         NULL, 4, 12, 0, 'LOW',    'CREATED',  DATE_ADD(CURDATE(), INTERVAL 870 MINUTE),  NOW(), NOW(), NULL, NULL),
 (27, 'ケアプラン確認',        'ケアマネとのプラン擦り合わせ',     NULL, 6, 12, 0, 'MEDIUM', 'CREATED',  DATE_ADD(CURDATE(), INTERVAL 990 MINUTE),  NOW(), NOW(), NULL, NULL),
 (28, '勤務シフトの確認',       '来週の勤務シフトを確認',          NULL, 8, NULL, 1, 'LOW',   'CREATED',  DATE_ADD(CURDATE(), INTERVAL 1440 MINUTE), NOW(), NOW(), NULL, NULL),
 (29, '主治医回診',           '内科病棟の定時回診',             NULL, 6, 1,  0, 'MEDIUM', 'CREATED',  DATE_ADD(CURDATE(), INTERVAL 660 MINUTE),  NOW(), NOW(), NULL, NULL),
 (30, '清拭',                 '全身清拭と更衣',                 NULL, 2, 7,  0, 'MEDIUM', 'DONE',     DATE_ADD(CURDATE(), INTERVAL 450 MINUTE),  NOW(), NOW(), NULL, NULL);

-- 担当者（task_assignees：task_id, user_id）
INSERT INTO task_assignees (task_id, user_id) VALUES
 (1,7),(1,15), (2,20), (3,7), (4,8),
 (5,7), (6,17), (7,21), (8,7),(8,23), (9,8),
 (10,22), (11,7), (12,2),(12,9), (13,9),
 (14,20), (15,23), (16,19), (17,11), (18,11),(18,4),
 (19,18), (20,5),(20,12), (21,20), (22,13),(22,16),
 (23,13), (24,10), (25,21), (26,19), (27,24),
 (29,3), (30,12);

-- ============================================================
-- 【サンプル】受け持ち患者（デモ用）
--   nurse(id7=鈴木) が内科病棟の患者1,2,9,10 を受け持ち → ログイン直後にタイムラインが充実。
--   他ログインでも受け持ちが見えるよう doctor(2)・admin(1) にも数件。
-- ============================================================
INSERT INTO user_patient_assignments (id, user_id, patient_id, created_at, updated_at, deleted_at, updated_by) VALUES
 (1, 7, 1,  NOW(), NOW(), NULL, NULL),
 (2, 7, 2,  NOW(), NOW(), NULL, NULL),
 (3, 7, 9,  NOW(), NOW(), NULL, NULL),
 (4, 7, 10, NOW(), NOW(), NULL, NULL),
 (5, 2, 3,  NOW(), NOW(), NULL, NULL),
 (6, 2, 4,  NOW(), NOW(), NULL, NULL),
 (7, 1, 7,  NOW(), NOW(), NULL, NULL);

-- ============================================================
-- 【サンプル】お知らせ（緊急/連絡/シフトを混在。有効・期限切れを分散）
--   expired_at：未来=有効、過去=期限切れ。created_at は少しずつずらして並び順を作る。
-- ============================================================
INSERT INTO announcements (id, title, description, project_id, category_id, department_id, priority, expired_at, created_by, created_at, updated_at, deleted_at, updated_by) VALUES
 (1,  'インフルエンザ院内感染対策の徹底',   '面会制限とマスク着用を徹底してください。',   NULL, 7, NULL, 'HIGH',   DATE_ADD(CURDATE(), INTERVAL 30 DAY), 1,  DATE_ADD(NOW(), INTERVAL -1 HOUR),  NOW(), NULL, NULL),
 (2,  '電子カルテ メンテナンスのお知らせ',   '今週末に定期メンテを実施します。',         NULL, 6, NULL, 'MEDIUM', DATE_ADD(CURDATE(), INTERVAL 14 DAY), 1,  DATE_ADD(NOW(), INTERVAL -2 HOUR),  NOW(), NULL, NULL),
 (3,  '来月の勤務表を掲示しました',          '内科病棟の勤務表を更新しました。',         NULL, 8, 1,    'LOW',    DATE_ADD(CURDATE(), INTERVAL 20 DAY), 8,  DATE_ADD(NOW(), INTERVAL -3 HOUR),  NOW(), NULL, NULL),
 (4,  '感染性廃棄物の分別徹底について',      '分別ルールの再確認をお願いします。',       NULL, 6, 2,    'MEDIUM', DATE_ADD(CURDATE(), INTERVAL 25 DAY), 2,  DATE_ADD(NOW(), INTERVAL -5 HOUR),  NOW(), NULL, NULL),
 (5,  '災害時対応訓練の実施（終了）',        '先月の訓練は無事終了しました。',           NULL, 7, NULL, 'HIGH',   DATE_ADD(CURDATE(), INTERVAL -5 DAY), 1,  DATE_ADD(NOW(), INTERVAL -10 DAY),  NOW(), NULL, NULL),
 (6,  '職員健康診断の日程について',          '受診日を各自で予約してください。',         NULL, 6, NULL, 'LOW',    DATE_ADD(CURDATE(), INTERVAL 40 DAY), 15, DATE_ADD(NOW(), INTERVAL -8 HOUR),  NOW(), NULL, NULL),
 (7,  '年末年始の勤務体制について',          '勤務体制と当直表を確認してください。',     NULL, 8, NULL, 'MEDIUM', DATE_ADD(CURDATE(), INTERVAL 50 DAY), 1,  DATE_ADD(NOW(), INTERVAL -12 HOUR), NOW(), NULL, NULL),
 (8,  '新人研修プログラム開始のお知らせ',    '今月より新人研修を開始します。',           NULL, 6, NULL, 'MEDIUM', DATE_ADD(CURDATE(), INTERVAL 30 DAY), 6,  DATE_ADD(NOW(), INTERVAL -1 DAY),   NOW(), NULL, NULL),
 (9,  '医療機器リコール情報',                '対象輸液ポンプの使用を停止してください。',  NULL, 7, 4,    'HIGH',   DATE_ADD(CURDATE(), INTERVAL 10 DAY), 5,  DATE_ADD(NOW(), INTERVAL -30 MINUTE), NOW(), NULL, NULL),
 (10, '駐車場工事のお知らせ（終了）',        '工事は完了しました。',                     NULL, 6, NULL, 'LOW',    DATE_ADD(CURDATE(), INTERVAL -3 DAY), 1,  DATE_ADD(NOW(), INTERVAL -7 DAY),   NOW(), NULL, NULL),
 (11, '褥瘡対策委員会の開催',                '今月の委員会を開催します。',               NULL, 6, 1,    'MEDIUM', DATE_ADD(CURDATE(), INTERVAL 7 DAY),  7,  DATE_ADD(NOW(), INTERVAL -2 DAY),   NOW(), NULL, NULL),
 (12, '夏季休暇の申請受付を開始します',      '希望日を早めに申請してください。',         NULL, 8, NULL, 'LOW',    DATE_ADD(CURDATE(), INTERVAL 45 DAY), 1,  DATE_ADD(NOW(), INTERVAL -4 DAY),   NOW(), NULL, NULL);
