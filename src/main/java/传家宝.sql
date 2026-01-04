/**
 * 回滚所有人最新变更的1478数据
 */

BEGIN;

-- 1) 先找到每个 user_code 最新、且还未回滚(is_rollback IS NULL) 的一条记录
WITH latest AS (
    SELECT *
    FROM (
        SELECT b.*,
               ROW_NUMBER() OVER (
                   PARTITION BY b.user_code
                   ORDER BY b.create_time DESC
               ) AS rn
        FROM per_core.category_record b
        WHERE b.is_rollback IS NULL
    ) t
    WHERE t.rn = 1     -- 只保留每个 user_code 的最新一条
)
UPDATE per_core.hr_emp_basic a
SET category1 = l.old_category1,
    category4 = l.old_category4,
    category7 = l.old_category7,
    category8 = l.old_category8
FROM latest l
WHERE a.user_code = l.user_code;

WITH latest AS (
    SELECT *
    FROM (
        SELECT b.*,
               ROW_NUMBER() OVER (
                   PARTITION BY b.user_code
                   ORDER BY b.create_time DESC
               ) AS rn
        FROM per_core.category_record b
        WHERE b.is_rollback IS NULL
    ) t
    WHERE t.rn = 1
)
UPDATE per_core.category_record b
SET is_rollback = 1
FROM latest l
WHERE b.id = l.id;   -- 这里用主键字段名替换 id

COMMIT;

/**
 * 同步部门
 */
BEGIN;

MERGE INTO HR_EMP_BASIC b
    USING (
        SELECT user_code, dept
        FROM (
                 SELECT
                     user_code,
                     dept,
                     ROW_NUMBER() OVER(
        PARTITION BY user_code
        ORDER BY start_date DESC NULLS LAST, end_date DESC NULLS first, id DESC
      ) rn
                 FROM HR_EMP_APPOINTMENT
                 WHERE start_date IS NOT NULL
             ) t
        WHERE t.rn = 1
    ) a
    ON (b.user_code = a.user_code)
    WHEN MATCHED THEN
        UPDATE SET b.dept = a.dept
    -- 只更新“确实不同”的，避免无意义写入
    WHERE (b.dept <> a.dept)
     OR (b.dept IS NULL AND a.dept IS NOT NULL)
     OR (b.dept IS NOT NULL AND a.dept IS NULL);

COMMIT;