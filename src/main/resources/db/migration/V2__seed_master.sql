-- ============================================================
-- V2__seed_master.sql — マスタ（参照）データの投入
-- ============================================================
-- 部署・カテゴリは「本番にも存在しうる正規の参照データ」なので、
-- デモデータ（data.sql）ではなく Flyway マイグレーションで管理する（1回だけ適用・履歴に記録）。
-- ※ patients.department_id / tasks.category_id 等がこの id を参照するため、id は固定値で入れる。
-- ============================================================

-- 部署
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

-- カテゴリ（フロントの色分け：緊急=赤 / 連絡=青 / シフト・その他=グレー に対応）
INSERT INTO categories (id, category_name, created_at, updated_at, deleted_at, updated_by) VALUES
 (1, '与薬',    NOW(), NOW(), NULL, NULL),
 (2, '処置',    NOW(), NOW(), NULL, NULL),
 (3, '検査',    NOW(), NOW(), NULL, NULL),
 (4, 'リハビリ', NOW(), NOW(), NULL, NULL),
 (5, '記録',    NOW(), NOW(), NULL, NULL),
 (6, '連絡',    NOW(), NOW(), NULL, NULL),
 (7, '緊急',    NOW(), NOW(), NULL, NULL),
 (8, 'シフト',  NOW(), NOW(), NULL, NULL);
