package com.crrc.service;

import com.crrc.api.WkDataAPi;
import com.crrc.entity.*;
import com.crrc.service.impl.CodeInfoServiceImpl;
import com.crrc.service.impl.PostCatalogInfoServiceImpl;
import com.crrc.utils.MyBatisPlusTableUtil;
import com.crrc.utils.OracleJdbcUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MyBatisPlusMigrationService {

    private int batchSize = 1000;

    @Autowired
    private JdbcTemplate openGaussJdbcTemplate;

    @Autowired
    private MyBatisPlusTableUtil tableUtil;

    @Autowired
    private WkDataAPi wkDataAPi;
    @Autowired
    private PostCatalogInfoServiceImpl postCatalogInfoServiceImpl;
    @Autowired
    private CodeInfoServiceImpl codeInfoService;

    /**
     * 迁移实体表数据
     */
    public void migrateEntity(Class<?> entityClass) {
        String tableName = tableUtil.getTableName(entityClass);
        List<String> columns = tableUtil.getTableColumns(entityClass);
        String primaryKey = tableUtil.getPrimaryKey(entityClass);
        columns.add(primaryKey);
        migrateTable(tableName, columns.toArray(new String[0]), primaryKey);
    }

    /**
     * 迁移指定表数据,全量同步
     */
    public void migrateTable(String tableName, String[] columns, String primaryKey) {
        log.info("开始迁移表: " + tableName);
        log.info("主键列: " + primaryKey);
        log.info("迁移列: " + String.join(", ", columns));

        long startTime = System.currentTimeMillis();

        String columnStr = String.join(", ", columns);
        String placeholders = String.join(", ",
                java.util.Collections.nCopies(columns.length, "?"));

        // 查询总数
        long totalCount = openGaussJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName, Long.class);

        // Oracle连接
        try (Connection oracleConn = OracleJdbcUtil.getConnection()) {
            // 清空目标表
            try (Statement stmt = oracleConn.createStatement()) {
                stmt.execute("TRUNCATE TABLE " + tableName);
            }

            // 准备插入语句
            String insertSql = String.format(
                    "INSERT INTO %s (%s) VALUES (%s)",
                    tableName, columnStr, placeholders);

            Object lastUserCode = null; // 初始为null
            AtomicLong migratedCount = new AtomicLong(0);

            while (true) {
                // 分页查询源数据
                String querySql;
                List<Map<String, Object>> batchData;

                if (lastUserCode == null) {
                    // 第一次查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    // 先取create_time有值的
                                    "           ROW_NUMBER() OVER " +
                                    "(PARTITION BY user_code ORDER BY CASE WHEN create_time IS NULL THEN 1 ELSE 0 END, create_time DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND user_code IS NOT NULL" +
                                    ") " +
                                    "SELECT * FROM ranked_data " +
                                    "WHERE group_rank = 1 " +
                                    "ORDER BY user_code " +
                                    "LIMIT ?",
                            // 不从视图取值
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, batchSize);
                } else {
                    // 后续查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    // 先取create_time有值的
                                    "           ROW_NUMBER() OVER " +
                                    "(PARTITION BY user_code ORDER BY CASE WHEN create_time IS NULL THEN 1 ELSE 0 END, create_time DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND user_code IS NOT NULL AND user_code > ?" +
                                    ") " +
                                    "SELECT * FROM ranked_data " +
                                    "WHERE group_rank = 1 " +
                                    "ORDER BY user_code " +
                                    "LIMIT ?",
                            // 不从视图取值
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, lastUserCode, batchSize);
                }

                if (batchData.isEmpty()) {
                    break;
                }

                // 批量插入目标表
                try (PreparedStatement pstmt = oracleConn.prepareStatement(insertSql)) {
                    for (Map<String, Object> row : batchData) {
                        for (int i = 0; i < columns.length; i++) {
                            pstmt.setObject(i + 1, row.get(columns[i]));
                        }
                        pstmt.addBatch();

                        // 更新最后ID
                        lastUserCode = row.get("user_code");
                    }
                    pstmt.executeBatch();
                }

                long currentCount = migratedCount.addAndGet(batchData.size());
                log.info("表 {} 迁移进度: {}/{} ({}%)",
                        tableName, currentCount, totalCount,
                        String.format("%.2f", currentCount * 100.0 / totalCount));


            }

            long endTime = System.currentTimeMillis();
            log.info("表 {} 迁移完成, 共 {} 条记录, 耗时 {} 毫秒",
                    tableName, migratedCount.get(), (endTime - startTime));

        } catch (SQLException e) {
            throw new RuntimeException("Oracle数据库操作失败", e);
        }
    }
    /**
     * 教育经历只取前两条
     */
    public void migrateEduTable() {
        String tableName = tableUtil.getTableName(HrEmpEducation.class);
        List<String> columnList = tableUtil.getTableColumns(HrEmpEducation.class);
        String primaryKey = tableUtil.getPrimaryKey(HrEmpEducation.class);
        columnList.add(primaryKey);
        String[] columns = columnList.toArray(new String[0]);

        log.info("开始迁移表: " + tableName);
        log.info("主键列: " + primaryKey);
        log.info("迁移列: " + String.join(", ", columns));

        long startTime = System.currentTimeMillis();

        String columnStr = String.join(", ", columns);
        String placeholders = String.join(", ",
                java.util.Collections.nCopies(columns.length, "?"));

        // 查询总数
        long totalCount = openGaussJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName, Long.class);

        // Oracle连接
        try (Connection oracleConn = OracleJdbcUtil.getConnection()) {
            // 清空目标表
            try (Statement stmt = oracleConn.createStatement()) {
                stmt.execute("TRUNCATE TABLE " + tableName);
            }

            // 准备插入语句
            String insertSql = String.format(
                    "INSERT INTO %s (%s) VALUES (%s)",
                    tableName, columnStr, placeholders);

            Object lastUserCode = null; // 初始为null
            AtomicLong migratedCount = new AtomicLong(0);

            while (true) {
                // 分页查询源数据
                String querySql;
                List<Map<String, Object>> batchData;

                if (lastUserCode == null) {
                    // 第一次查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    "           ROW_NUMBER() OVER (PARTITION BY user_code ORDER BY CASE WHEN school_entry_date IS NULL THEN 1 ELSE 0 END ASC, school_entry_date DESC, education ASC) AS group_rank " +  // 改为按education排序
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND user_code IS NOT NULL AND education IS NOT NULL" +  // 增加education不为空条件
                                    ") " +
                                    "SELECT ra.id, user_code, school_entry_date, graduation_date, school_name, " +
                                    "case when ci1.code_value is null then major_code else ci1.code_value end as major_code, college, is_major, " +
                                    "case when ci3.code_value is null then is_full_time else ci3.code_value end as is_full_time, " +
                                    "case when ci.code_value is not null then ci.code_value else education end as education, " +
                                    "case when ci4.code_value is null then \"degree\" else ci4.code_value end as \"degree\", " +
                                    "is_highest_degree, is_highest_education, completion_status, ra.deleted, ra.create_time, ra.create_by, " +
                                    "ra.update_time, ra.update_by, ra.tenant_id, degree_major, is_updating, ra.input_school_name, ra.input_major_name, " +
                                    "case when ci2.code_value is null then school_code else ci2.code_value end as school_code, file_header_id " +
                                    "FROM ranked_data ra " +
                                    "left join code_info ci on ci.code = ra.education and ci.\"type\" = 'EDU_EDU' " +
                                    "left join code_info ci1 on ci1.code = ra.major_code and ci1.\"type\" = 'EDU_MAJOR_CODE' " +
                                    "left join code_info ci2 on ci2.code = ra.school_code and ci2.\"type\" = 'EDU_SCHOOL_CODE' " +
                                    "left join code_info ci3 on ci3.code = ra.is_full_time and ci3.\"type\" = 'EDU_FULL_TIME' " +
                                    "left join code_info ci4 on ci4.code = ra.\"degree\" and ci4.\"type\" = 'EDU_DEGREE' " +
                                    "WHERE group_rank <= 2 " +  // 取每组前两条
                                    "ORDER BY user_code " +  // 外层仍按user_code排序保证分页连续性
                                    "LIMIT ?",
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, batchSize);
                } else {
                    // 后续查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    "           ROW_NUMBER() OVER (PARTITION BY user_code ORDER BY CASE WHEN school_entry_date IS NULL THEN 1 ELSE 0 END ASC, school_entry_date DESC, education ASC) AS group_rank " +  // 改为按education排序
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND user_code IS NOT NULL AND user_code > ? AND education IS NOT NULL" +  // 增加education不为空条件
                                    ") " +
                                    "SELECT ra.id, user_code, school_entry_date, graduation_date, school_name, " +
                                    "case when ci1.code_value is null then major_code else ci1.code_value end as major_code, college, is_major, " +
                                    "case when ci3.code_value is null then is_full_time else ci3.code_value end as is_full_time, " +
                                    "case when ci.code_value is not null then ci.code_value else education end as education, " +
                                    "case when ci4.code_value is null then \"degree\" else ci4.code_value end as \"degree\", " +
                                    "is_highest_degree, is_highest_education, completion_status, ra.deleted, ra.create_time, ra.create_by, " +
                                    "ra.update_time, ra.update_by, ra.tenant_id, degree_major, is_updating, ra.input_school_name, ra.input_major_name, " +
                                    "case when ci2.code_value is null then school_code else ci2.code_value end as school_code, file_header_id " +
                                    "FROM ranked_data ra " +
                                    "left join code_info ci on ci.code = ra.education and ci.\"type\" = 'EDU_EDU' " +
                                    "left join code_info ci1 on ci1.code = ra.major_code and ci1.\"type\" = 'EDU_MAJOR_CODE' " +
                                    "left join code_info ci2 on ci2.code = ra.school_code and ci2.\"type\" = 'EDU_SCHOOL_CODE' " +
                                    "left join code_info ci3 on ci3.code = ra.is_full_time and ci3.\"type\" = 'EDU_FULL_TIME' " +
                                    "left join code_info ci4 on ci4.code = ra.\"degree\" and ci4.\"type\" = 'EDU_DEGREE' " +
                                    "WHERE group_rank <= 2 " +  // 取每组前两条
                                    "ORDER BY user_code " +  // 外层仍按user_code排序保证分页连续性
                                    "LIMIT ?",
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, lastUserCode, batchSize);
                }

                if (batchData.isEmpty()) {
                    break;
                }

                // 批量插入目标表
                try (PreparedStatement pstmt = oracleConn.prepareStatement(insertSql)) {
                    for (Map<String, Object> row : batchData) {
                        for (int i = 0; i < columns.length; i++) {
                            pstmt.setObject(i + 1, row.get(columns[i]));
                        }
                        pstmt.addBatch();

                        // 更新最后ID
                        lastUserCode = row.get("user_code");
                    }
                    pstmt.executeBatch();
                }

                long currentCount = migratedCount.addAndGet(batchData.size());
                log.info("表 {} 迁移进度: {}/{} ({}%)",
                        tableName, currentCount, totalCount,
                        String.format("%.2f", currentCount * 100.0 / totalCount));


            }

            long endTime = System.currentTimeMillis();
            log.info("表 {} 迁移完成, 共 {} 条记录, 耗时 {} 毫秒",
                    tableName, migratedCount.get(), (endTime - startTime));

        } catch (SQLException e) {
            throw new RuntimeException("Oracle数据库操作失败", e);
        }
    }

    /**
     * 基础数据表同步
     * @param entityClass
     */
    public void migrateInfoTable(Class<?> entityClass) {
        String tableName = tableUtil.getTableName(entityClass);
        List<String> columnList = tableUtil.getTableColumns(entityClass);
        String primaryKey = tableUtil.getPrimaryKey(entityClass);
        columnList.add(primaryKey);
        String[] columns = columnList.toArray(new String[0]);

        log.info("开始迁移表: " + tableName);
        log.info("主键列: " + primaryKey);
        log.info("迁移列: " + String.join(", ", columns));

        long startTime = System.currentTimeMillis();

        String columnStr = String.join(", ", columns);
        String placeholders = String.join(", ",
                java.util.Collections.nCopies(columns.length, "?"));

        // 查询总数
        long totalCount = openGaussJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " WHERE DELETED = '0'", Long.class);

        // Oracle连接
        try (Connection oracleConn = OracleJdbcUtil.getConnection()) {
            // 清空目标表
            try (Statement stmt = oracleConn.createStatement()) {
                stmt.execute("TRUNCATE TABLE " + tableName);
            }

            // 准备插入语句
            String insertSql = String.format(
                    "INSERT INTO %s (%s) VALUES (%s)",
                    tableName, columnStr, placeholders);

            // 查询所有源数据
            String querySql = "SELECT * FROM " + tableName + " WHERE DELETED = '0'";
            List<Map<String, Object>> allData = openGaussJdbcTemplate.queryForList(querySql);

            // 批量插入目标表
            try (PreparedStatement pstmt = oracleConn.prepareStatement(insertSql)) {
                for (Map<String, Object> row : allData) {
                    for (int i = 0; i < columns.length; i++) {
                        pstmt.setObject(i + 1, row.get(columns[i]));
                    }
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            long endTime = System.currentTimeMillis();
            log.info("表 {} 迁移完成, 共 {} 条记录, 耗时 {} 毫秒",
                    tableName, allData.size(), (endTime - startTime));

        } catch (SQLException e) {
            throw new RuntimeException("Oracle数据库操作失败", e);
        }
    }


    /**
     * 智能迁移表数据，增量同步
     *
     * @param tableName  表名
     * @param columns    列数组
     * @param primaryKey 主键列名
     */
    public void smartMigrateTable(String tableName, String[] columns, String primaryKey) {
        Assert.hasText(tableName, "表名不能为空");
        Assert.notEmpty(columns, "列不能为空");
        Assert.hasText(primaryKey, "主键不能为空");
        Assert.isTrue(Arrays.asList(columns).contains(primaryKey), "主键列必须包含在列列表中");

        log.info("开始智能迁移表: {}, 主键: {}, 迁移列: {}",
                tableName, primaryKey, String.join(", ", columns));

        long startTime = System.currentTimeMillis();

        // 查询总数
        long totalCount = openGaussJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName, Long.class);
        log.info("待迁移总记录数: {}", totalCount);

        // 构建MERGE SQL
        String mergeSql = buildMergeSql(tableName, columns, primaryKey);
        log.debug("生成MERGE SQL: {}", mergeSql);

        try (Connection oracleConn = OracleJdbcUtil.getConnection()) {

            Object lastId = null;
            AtomicLong migratedCount = new AtomicLong(0);

            while (true) {
                // 分页查询源数据
                List<Map<String, Object>> batchData = fetchBatchData(
                        tableName, columns, primaryKey, lastId);

                if (batchData.isEmpty()) {
                    break;
                }

                // 批量处理
                try (PreparedStatement pstmt = oracleConn.prepareStatement(mergeSql)) {
                    for (Map<String, Object> row : batchData) {
                        bindParameters(pstmt, columns, primaryKey, row);
                        pstmt.addBatch();
                        lastId = row.get(primaryKey);
                    }
                    int[] results = pstmt.executeBatch();
                    logBatchResults(results);
                }

                // 更新进度
                long currentCount = migratedCount.addAndGet(batchData.size());
                logProgress(tableName, currentCount, totalCount);
            }

            logCompletion(tableName, migratedCount.get(), startTime);
        } catch (SQLException e) {
            throw new RuntimeException("数据库操作失败: " + e.getMessage(), e);
        }
    }


    /**
     * 生成MERGE SQL
     */
    private String buildMergeSql(String tableName, String[] columns, String primaryKey) {
        // 1. USING子句
        String usingParams = Arrays.stream(columns)
                .map(col -> "? as " + col)
                .collect(Collectors.joining(", "));

        // 2. UPDATE子句
        String updateSet = Arrays.stream(columns)
                .filter(col -> !col.equals(primaryKey))
                .map(col -> col + "=?")
                .collect(Collectors.joining(", "));

        // 3. INSERT子句
        String insertValues = Collections.nCopies(columns.length, "?")
                .stream().collect(Collectors.joining(", "));

        return String.format(
                "MERGE INTO %s t " +
                        "USING (SELECT %s FROM DUAL) s " +
                        "ON (t.%s = s.%s) " +
                        "WHEN MATCHED THEN UPDATE SET %s " +
                        "WHEN NOT MATCHED THEN INSERT (%s) VALUES (%s)",
                tableName, usingParams, primaryKey, primaryKey,
                updateSet, String.join(", ", columns), insertValues);
    }

    /**
     * 参数绑定
     */
    private void bindParameters(PreparedStatement pstmt,
                                String[] columns,
                                String primaryKey,
                                Map<String, Object> row) throws SQLException {
        // 参数索引计数器（从1开始）
        int paramIndex = 1;

        // === 第一阶段：USING子句参数 ===
        // 这些参数仅用于ON条件匹配
        for (String col : columns) {
            setParameter(pstmt, paramIndex++, row.get(col));
        }

        // === 第二阶段：UPDATE SET参数 ===
        // 仅绑定需要更新的非主键字段（注意值来自原数据）
        for (String col : columns) {
            if (!col.equals(primaryKey)) {
                setParameter(pstmt, paramIndex++, row.get(col));
            }
        }

        // === 第三阶段：INSERT VALUES参数 ===
        // 需要插入的所有字段值（包括主键）
        for (String col : columns) {
            setParameter(pstmt, paramIndex++, row.get(col));
        }
    }

    /**
     * 安全设置参数
     */
    private void setParameter(PreparedStatement pstmt, int index, Object value)
            throws SQLException {
        if (value == null) {
            pstmt.setNull(index, Types.NULL);
        } else {
            pstmt.setObject(index, value);
        }
    }

    /**
     * 分页获取数据
     */
    private List<Map<String, Object>> fetchBatchData(
            String tableName, String[] columns, String primaryKey, Object lastId) {
        String columnStr = String.join(", ", columns);
        String querySql;

        if (lastId == null) {
            querySql = String.format(
                    "SELECT %s FROM %s ORDER BY %s LIMIT ?",
                    columnStr, tableName, primaryKey);
            return openGaussJdbcTemplate.queryForList(querySql, batchSize);
        } else {
            querySql = String.format(
                    "SELECT %s FROM %s WHERE %s > ? ORDER BY %s LIMIT ?",
                    columnStr, tableName, primaryKey, primaryKey);
            return openGaussJdbcTemplate.queryForList(querySql, lastId, batchSize);
        }
    }

    private void logBatchResults(int[] results) {
        if (log.isDebugEnabled()) {
            long successCount = Arrays.stream(results).filter(r -> r >= 0).count();
            log.debug("批量操作结果: 成功 {} 条", successCount);
        }
    }

    private void logProgress(String tableName, long current, long total) {
        log.info("迁移进度 - {}: {}/{} ({}%)",
                tableName, current, total,
                String.format("%.2f", current * 100.0 / total));
    }

    private void logCompletion(String tableName, long count, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        double recordsPerSecond = count / (duration / 1000.0);

        log.info("迁移完成 - {}: 共 {} 条记录, 耗时 {} 毫秒 (约 {}/秒)",
                tableName, count, duration,
                String.format("%.2f", recordsPerSecond));
    }

    public void migrateAppointmentEntity(Class<?> appointmentClass) {
        String tableName = tableUtil.getTableName(appointmentClass);
        String primaryKey = tableUtil.getPrimaryKey(appointmentClass);

        // 避免直接修改tableUtil返回的原始集合
        List<String> columns = new ArrayList<>(
                tableUtil.getTableColumns(appointmentClass)
        );

        addColumnIfAbsent(columns, primaryKey);
        addColumnIfAbsent(columns, "JOB_ONE");
        addColumnIfAbsent(columns, "JOB_TWO");
        addColumnIfAbsent(columns, "JOB_THREE");

        log.info("开始迁移表: {}", tableName);
        log.info("主键列: {}", primaryKey);
        log.info("迁移列: {}", String.join(", ", columns));

        long startTime = System.currentTimeMillis();

        String columnStr = String.join(", ", columns);
        String placeholders = String.join(
                ", ",
                Collections.nCopies(columns.size(), "?")
        );

        String insertSql = String.format(
                "INSERT INTO %s (%s) VALUES (%s)",
                tableName,
                columnStr,
                placeholders
        );

        /*
         * 离职类型既包含中文值，也包含数据库中可能存在的编码值。
         */
        final String leaveChangeTypes =
                "'离职','退休','因病提前退休','实习生','离休'," +
                        "'特殊工种提前退休','死亡','集团内调出','集团外调出'," +
                        "'入站博士后','015','016','017','018'";

        /*
         * hr_emp_basic理论上每个user_code应只有一条有效记录。
         * 为防止存在重复数据，这里按照更新时间、创建时间、ID取最新状态。
         */
        final String basicStatusCte =
                "basic_ranked AS (" +
                        "    SELECT heb.user_code, " +
                        "           heb.is_employed, " +
                        "           ROW_NUMBER() OVER (" +
                        "               PARTITION BY heb.user_code " +
                        "               ORDER BY heb.update_time DESC NULLS LAST, " +
                        "                        heb.create_time DESC NULLS LAST, " +
                        "                        heb.id DESC" +
                        "           ) AS basic_rank " +
                        "    FROM hr_emp_basic heb " +
                        "    WHERE heb.deleted = 0 " +
                        "      AND heb.user_code IS NOT NULL" +
                        "), " +
                        "basic_status AS (" +
                        "    SELECT user_code, is_employed " +
                        "    FROM basic_ranked " +
                        "    WHERE basic_rank = 1" +
                        ")";

        /*
         * 严格按照本次文字要求：
         *
         * 1. is_employed = '1'：
         *    只保留离职信息。
         *
         * 2. is_employed不是'1'，包括0或NULL：
         *    只保留任职信息，即change_type不属于离职类型。
         *
         * 注意：该逻辑与建表文件中“1=在职、0=离职”的注释相反。
         */
        final String recordSelectionCondition =
                " AND (" +
                        "       (" +
                        "           bs.is_employed = '1' " +
                        "           AND (" +
                        "               src.change_type IS NULL " +
                        "               OR src.change_type NOT IN (" + leaveChangeTypes + ")" +
                        "           )" +
                        "       ) " +
                        "       OR " +
                        "       (" +
                        "           bs.is_employed = '0' " +
                        "           AND src.change_type IN (" + leaveChangeTypes + ")" +
                        "       )" +
                        " ) ";

        /*
         * 总数统计必须和实际迁移条件一致，否则进度可能不准确。
         */
        String totalCountSql = String.format(
                "WITH " + basicStatusCte + " " +
                        "SELECT COUNT(DISTINCT src.user_code) " +
                        "FROM %s src " +
                        "JOIN basic_status bs " +
                        "  ON bs.user_code = src.user_code " +
                        "WHERE src.deleted = 0 " +
                        "  AND src.user_code IS NOT NULL " +
                        recordSelectionCondition,
                tableName
        );

        Long totalCountValue = openGaussJdbcTemplate.queryForObject(
                totalCountSql,
                Long.class
        );

        long totalCount = totalCountValue == null ? 0L : totalCountValue;

        try (Connection oracleConn = OracleJdbcUtil.getConnection()) {
            oracleConn.setAutoCommit(false);

            try {
                // 清空Oracle目标表
                try (Statement stmt = oracleConn.createStatement()) {
                    stmt.execute("TRUNCATE TABLE " + tableName);
                }

                String lastUserCode = null;
                long migratedCount = 0L;

                try (PreparedStatement pstmt =
                             oracleConn.prepareStatement(insertSql)) {

                    while (true) {
                        boolean firstPage = lastUserCode == null;

                        String pageCondition = firstPage
                                ? ""
                                : " AND src.user_code > ? ";

                        String querySql = String.format(
                                "WITH " + basicStatusCte + ", " +

                                        /*
                                         * 先按照is_employed筛选出指定类型的候选记录。
                                         */
                                        "candidate_data AS (" +
                                        "    SELECT src.* " +
                                        "    FROM %s src " +
                                        "    JOIN basic_status bs " +
                                        "      ON bs.user_code = src.user_code " +
                                        "    WHERE src.deleted = 0 " +
                                        "      AND src.user_code IS NOT NULL " +
                                        pageCondition +
                                        recordSelectionCondition +
                                        "), " +

                                        /*
                                         * 候选记录已经被限定为离职或者任职中的一种，
                                         * 再按时间取该类型的最新一条。
                                         */
                                        "ranked_data AS (" +
                                        "    SELECT cd.*, " +
                                        "           ROW_NUMBER() OVER (" +
                                        "               PARTITION BY cd.user_code " +
                                        "               ORDER BY " +
                                        "                   cd.create_time DESC NULLS LAST, " +
                                        "                   cd.start_date DESC NULLS LAST, " +
                                        "                   cd.end_date DESC NULLS LAST, " +
                                        "                   cd.id DESC" +
                                        "           ) AS group_rank " +
                                        "    FROM candidate_data cd" +
                                        ") " +

                                        "SELECT ra.start_date, " +
                                        "       ra.end_date, " +
                                        "       COALESCE(ci2.code_value, ra.change_event) " +
                                        "           AS change_event, " +
                                        "       COALESCE(ci1.code_value, ra.change_type) " +
                                        "           AS change_type, " +
                                        "       COALESCE(ci3.code_value, ra.change_reason) " +
                                        "           AS change_reason, " +
                                        "       ra.personnel_category, " +
                                        "       COALESCE(ci4.code_value, ra.duty_status) " +
                                        "           AS duty_status, " +
                                        "       ra.org, " +
                                        "       ra.dept, " +
                                        "       ra.org_unit, " +
                                        "       COALESCE(pi.\"name\", ra.\"position\") " +
                                        "           AS \"position\", " +
                                        "       ra.hierarchy_rank, " +
                                        "       ra.position_name, " +
                                        "       ra.position_hierarchy, " +
                                        "       COALESCE(ci5.code_value, ra.position_category) " +
                                        "           AS position_category, " +
                                        "       ra.salary_group, " +
                                        "       ra.id, " +
                                        "       ra.user_code, " +
                                        "       ra.deleted, " +
                                        "       ra.create_time, " +
                                        "       ra.create_by, " +
                                        "       ra.update_time, " +
                                        "       ra.update_by, " +
                                        "       ra.tenant_id, " +
                                        "       COALESCE(pi.job_type, ra.job_type) " +
                                        "           AS job_type, " +
                                        "       ra.sort_no " +

                                        "FROM ranked_data ra " +

                                        "LEFT JOIN code_info ci1 " +
                                        "       ON ci1.code = ra.change_type " +
                                        "      AND ci1.\"type\" = " +
                                        "          'APPOINTMENT_CHANGE_TYPE' " +

                                        "LEFT JOIN code_info ci2 " +
                                        "       ON ci2.code = ra.change_event " +
                                        "      AND ci2.\"type\" = " +
                                        "          'APPOINTMENT_CHANGE_EVENT' " +

                                        "LEFT JOIN code_info ci3 " +
                                        "       ON ci3.code = ra.change_reason " +
                                        "      AND ci3.\"type\" = " +
                                        "          'APPOINTMENT_CHANGE_REASON' " +

                                        "LEFT JOIN code_info ci4 " +
                                        "       ON ci4.code = ra.duty_status " +
                                        "      AND ci4.\"type\" = " +
                                        "          'APPOINTMENT_DUTY_STATUS' " +

                                        "LEFT JOIN code_info ci5 " +
                                        "       ON ci5.code = ra.position_category " +
                                        "      AND ci5.\"type\" = " +
                                        "          'APPOINTMENT_POSITION_CATEGORY' " +

                                        "LEFT JOIN position_info pi " +
                                        "       ON pi.code = ra.\"position\" " +

                                        "WHERE ra.group_rank = 1 " +
                                        "ORDER BY ra.user_code " +
                                        "LIMIT ?",
                                tableName
                        );

                        List<Map<String, Object>> batchData;

                        if (firstPage) {
                            batchData = openGaussJdbcTemplate.queryForList(
                                    querySql,
                                    batchSize
                            );
                        } else {
                            batchData = openGaussJdbcTemplate.queryForList(
                                    querySql,
                                    lastUserCode,
                                    batchSize
                            );
                        }

                        // 没有更多数据时结束循环，防止死循环
                        if (batchData == null || batchData.isEmpty()) {
                            break;
                        }

                        pstmt.clearBatch();

                        for (Map<String, Object> row : batchData) {
                            /*
                             * 如果JOB_ONE、JOB_TWO、JOB_THREE是派生字段，
                             * 应在这里执行项目原有的职类职群职种转换逻辑。
                             *
                             * 当前查询没有直接返回这三个字段，
                             * 未填充时将向Oracle写入NULL。
                             */

                            for (int i = 0; i < columns.size(); i++) {
                                String column = columns.get(i);
                                Object value = getValueIgnoreCase(row, column);
                                pstmt.setObject(i + 1, value);
                            }

                            pstmt.addBatch();
                        }

                        pstmt.executeBatch();
                        oracleConn.commit();

                        // 游标必须取当前批次排序后的最后一个user_code
                        Map<String, Object> lastRow =
                                batchData.get(batchData.size() - 1);

                        Object nextUserCodeValue =
                                getValueIgnoreCase(lastRow, "user_code");

                        if (nextUserCodeValue == null) {
                            throw new IllegalStateException(
                                    "分页查询结果中的user_code为空，无法继续分页"
                            );
                        }

                        String nextUserCode =
                                String.valueOf(nextUserCodeValue);

                        // 防止异常数据导致游标没有前进，形成死循环
                        if (Objects.equals(lastUserCode, nextUserCode)) {
                            throw new IllegalStateException(
                                    "分页游标未前进，当前user_code=" +
                                            nextUserCode
                            );
                        }

                        lastUserCode = nextUserCode;
                        migratedCount += batchData.size();

                        double progress = totalCount == 0
                                ? 100.0
                                : Math.min(
                                100.0,
                                migratedCount * 100.0 / totalCount
                        );

                        log.info(
                                "表 {} 迁移进度: {}/{} ({}%)",
                                tableName,
                                migratedCount,
                                totalCount,
                                String.format(
                                        Locale.ROOT,
                                        "%.2f",
                                        progress
                                )
                        );
                    }
                }

                long endTime = System.currentTimeMillis();

                log.info(
                        "表 {} 迁移完成，共 {} 条记录，耗时 {} 毫秒",
                        tableName,
                        migratedCount,
                        endTime - startTime
                );

            } catch (SQLException e) {
                rollbackQuietly(oracleConn, e);
                throw e;
            } catch (RuntimeException e) {
                rollbackQuietly(oracleConn, e);
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Oracle数据库操作失败", e);
        }
    }

    /**
     * 忽略大小写添加列，避免主键或者JOB字段重复。
     */
    private void addColumnIfAbsent(
            List<String> columns,
            String column
    ) {
        if (column == null || column.trim().isEmpty()) {
            return;
        }

        boolean exists = columns.stream()
                .filter(Objects::nonNull)
                .anyMatch(item ->
                        item.replace("\"", "")
                                .equalsIgnoreCase(
                                        column.replace("\"", "")
                                )
                );

        if (!exists) {
            columns.add(column);
        }
    }

    /**
     * Spring JDBC返回的Map键名可能是大写或者小写；
     * 同时兼容列名中带双引号的情况。
     */
    private Object getValueIgnoreCase(
            Map<String, Object> row,
            String column
    ) {
        if (row == null || column == null) {
            return null;
        }

        String normalizedColumn =
                column.replace("\"", "").trim();

        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String key = entry.getKey();

            if (key != null &&
                    key.replace("\"", "")
                            .equalsIgnoreCase(normalizedColumn)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * 回滚失败时将异常附加到原始异常中。
     */
    private void rollbackQuietly(
            Connection connection,
            Throwable originalException
    ) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }

    public void migrateCareerPathEntity(Class<HrEmpCareerPath> hrEmpCareerPathClass) {
        String tableName = tableUtil.getTableName(hrEmpCareerPathClass);
        List<String> columns = tableUtil.getTableColumns(hrEmpCareerPathClass);
        String primaryKey = tableUtil.getPrimaryKey(hrEmpCareerPathClass);
        columns.add(primaryKey);

        log.info("开始迁移表: " + tableName);
        log.info("主键列: " + primaryKey);
        log.info("迁移列: " + String.join(", ", columns));

        long startTime = System.currentTimeMillis();

        String columnStr = String.join(", ", columns);
        String placeholders = String.join(", ",
                java.util.Collections.nCopies(columns.size(), "?"));

        // 查询总数
        long totalCount = openGaussJdbcTemplate.queryForList(
                "SELECT COUNT(*) FROM " + tableName + " GROUP BY user_code", Long.class).size();

        // Oracle连接
        try (Connection oracleConn = OracleJdbcUtil.getConnection()) {
            // 清空目标表
            try (Statement stmt = oracleConn.createStatement()) {
                stmt.execute("TRUNCATE TABLE " + tableName);
            }

            // 准备插入语句
            String insertSql = String.format(
                    "INSERT INTO %s (%s) VALUES (%s)",
                    tableName, columnStr, placeholders);

            Object lastUserCode = null; // 初始为null
            AtomicLong migratedCount = new AtomicLong(0);

            while (true) {
                // 分页查询源数据
                String querySql;
                List<Map<String, Object>> batchData;

                if (lastUserCode == null) {
                    // 第一次查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    // 先取create_time有值的
                                    "           ROW_NUMBER() OVER " +
                                    "(PARTITION BY user_code ORDER BY CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC, CASE WHEN qualification_date IS NULL THEN 1 ELSE 0 END ASC, qualification_date DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND user_code IS NOT NULL" +
                                    ") " +
                                    "SELECT ranked_data.id, ranked_data.user_code, CASE WHEN code_value IS NULL THEN path_id ELSE code_value END AS path_id, level_id, level_name, \"hierarchy\", qualification_date, effective_date, expiry_date, is_expired, deleted, ranked_data.create_time, ranked_data.create_by, ranked_data.update_time, ranked_data.update_by, ranked_data.tenant_id, is_updating FROM ranked_data LEFT JOIN code_info ci ON path_id = ci.code AND ci.\"type\" = 'CAREER_PATH_ID' " +
                                    "WHERE group_rank = 1 " +
                                    "ORDER BY user_code " +
                                    "LIMIT ?",
                            // 不从视图取值
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, batchSize);
                } else {
                    // 后续查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    // 先取create_time有值的
                                    "           ROW_NUMBER() OVER " +
                                    "(PARTITION BY user_code ORDER BY CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC, CASE WHEN qualification_date IS NULL THEN 1 ELSE 0 END ASC, qualification_date DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND user_code IS NOT NULL AND user_code > ?" +
                                    ") " +
                                    // 翻译path_id字段
                                    "SELECT ranked_data.id, ranked_data.user_code, CASE WHEN code_value IS NULL THEN path_id ELSE code_value END AS path_id, level_id, level_name, \"hierarchy\", qualification_date, effective_date, expiry_date, is_expired, deleted, ranked_data.create_time, ranked_data.create_by, ranked_data.update_time, ranked_data.update_by, ranked_data.tenant_id, is_updating FROM ranked_data LEFT JOIN code_info ci ON path_id = ci.code AND ci.\"type\" = 'CAREER_PATH_ID' " +
                                    "WHERE group_rank = 1 " +
                                    "ORDER BY user_code " +
                                    "LIMIT ?",
                            // 不从视图取值
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, lastUserCode, batchSize);
                }

                if (batchData.isEmpty()) {
                    break;
                }

                // 批量插入目标表
                try (PreparedStatement pstmt = oracleConn.prepareStatement(insertSql)) {
                    for (Map<String, Object> row : batchData) {
                        for (int i = 0; i < columns.size(); i++) {
                            pstmt.setObject(i + 1, row.get(columns.get(i)));
                        }
                        pstmt.addBatch();

                        // 更新最后ID
                        lastUserCode = row.get("user_code");
                    }
                    pstmt.executeBatch();
                }

                long currentCount = migratedCount.addAndGet(batchData.size());
                log.info("表 {} 迁移进度: {}/{} ({}%)",
                        tableName, currentCount, totalCount,
                        String.format("%.2f", currentCount * 100.0 / totalCount));
            }

            long endTime = System.currentTimeMillis();
            log.info("表 {} 迁移完成, 共 {} 条记录, 耗时 {} 毫秒",
                    tableName, migratedCount.get(), (endTime - startTime));

        } catch (SQLException e) {
            throw new RuntimeException("Oracle数据库操作失败", e);
        }
    }

    public void migrateProQuaEntity(Class<HrEmpProfessionalQualification> hrEmpProfessionalQualificationClass) {
        String tableName = tableUtil.getTableName(hrEmpProfessionalQualificationClass);
        List<String> columns = tableUtil.getTableColumns(hrEmpProfessionalQualificationClass);
        String primaryKey = tableUtil.getPrimaryKey(hrEmpProfessionalQualificationClass);
        columns.add(primaryKey);

        log.info("开始迁移表: " + tableName);
        log.info("主键列: " + primaryKey);
        log.info("迁移列: " + String.join(", ", columns));

        long startTime = System.currentTimeMillis();

        String columnStr = String.join(", ", columns);
        String placeholders = String.join(", ",
                java.util.Collections.nCopies(columns.size(), "?"));

        // 查询总数
        long totalCount = openGaussJdbcTemplate.queryForList(
                "SELECT COUNT(*) FROM " + tableName + " GROUP BY user_code", Long.class).size();

        // Oracle连接
        try (Connection oracleConn = OracleJdbcUtil.getConnection()) {
            // 清空目标表
            try (Statement stmt = oracleConn.createStatement()) {
                stmt.execute("TRUNCATE TABLE " + tableName);
            }

            // 准备插入语句
            String insertSql = String.format(
                    "INSERT INTO %s (%s) VALUES (%s)",
                    tableName, columnStr, placeholders);

            Object lastUserCode = null; // 初始为null
            AtomicLong migratedCount = new AtomicLong(0);

            while (true) {
                // 分页查询源数据
                String querySql;
                List<Map<String, Object>> batchData;

                if (lastUserCode == null) {
                    // 第一次查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    // 先取create_time有值的
                                    "           ROW_NUMBER() OVER " +
                                    "(PARTITION BY user_code ORDER BY CASE WHEN is_highest IS NULL THEN 1 ELSE 0 END ASC, is_highest DESC, CASE WHEN qua_date IS NULL THEN 1 ELSE 0 END DESC, CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND (is_highest = '1' OR is_highest IS NULL) AND user_code IS NOT NULL" +
                                    ") " +
                                    "SELECT ra.id, user_code, case when ci.code_value is null then ra.qua_id else ci.code_value end as qua_id, " +
                                    "qua_date, qua_title_level, qua_approval_unit, qua_path, is_highest, is_appointed, appointed_start_date, " +
                                    "appointed_end_date, ra.deleted, ra.create_time, ra.create_by, ra.update_time, ra.update_by, ra.tenant_id, is_updating " +
                                    "FROM ranked_data ra " +
                                    "left join code_info ci on ci.code = ra.qua_id and ci.\"type\" = 'QUA_ID' " +
                                    "WHERE group_rank = 1 " +
                                    "ORDER BY user_code " +
                                    "LIMIT ?",
                            // 不从视图取值
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, batchSize);
                } else {
                    // 后续查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    // 先取create_time有值的
                                    "           ROW_NUMBER() OVER " +
                                    "(PARTITION BY user_code ORDER BY CASE WHEN is_highest IS NULL THEN 1 ELSE 0 END ASC, is_highest DESC, CASE WHEN qua_date IS NULL THEN 1 ELSE 0 END DESC, CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND (is_highest = '1' OR is_highest IS NULL) AND user_code IS NOT NULL AND user_code > ?" +
                                    ") " +
                                    "SELECT ra.id, user_code, case when ci.code_value is null then ra.qua_id else ci.code_value end as qua_id, " +
                                    "qua_date, qua_title_level, qua_approval_unit, qua_path, is_highest, is_appointed, appointed_start_date, " +
                                    "appointed_end_date, ra.deleted, ra.create_time, ra.create_by, ra.update_time, ra.update_by, ra.tenant_id, is_updating " +
                                    "FROM ranked_data ra " +
                                    "left join code_info ci on ci.code = ra.qua_id and ci.\"type\" = 'QUA_ID' " +
                                    "WHERE group_rank = 1 " +
                                    "ORDER BY user_code " +
                                    "LIMIT ?",
                            // 不从视图取值
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, lastUserCode, batchSize);
                }

                if (batchData.isEmpty()) {
                    break;
                }

                // 批量插入目标表
                try (PreparedStatement pstmt = oracleConn.prepareStatement(insertSql)) {
                    for (Map<String, Object> row : batchData) {
                        for (int i = 0; i < columns.size(); i++) {
                            pstmt.setObject(i + 1, row.get(columns.get(i)));
                        }
                        pstmt.addBatch();

                        // 更新最后ID
                        lastUserCode = row.get("user_code");
                    }
                    pstmt.executeBatch();
                }

                long currentCount = migratedCount.addAndGet(batchData.size());
                log.info("表 {} 迁移进度: {}/{} ({}%)",
                        tableName, currentCount, totalCount,
                        String.format("%.2f", currentCount * 100.0 / totalCount));
            }

            long endTime = System.currentTimeMillis();
            log.info("表 {} 迁移完成, 共 {} 条记录, 耗时 {} 毫秒",
                    tableName, migratedCount.get(), (endTime - startTime));

        } catch (SQLException e) {
            throw new RuntimeException("Oracle数据库操作失败", e);
        }
    }

    public void migrateSalaryStandardEntity(Class<HrEmpSalaryStandard> hrEmpSalaryStandardClass) {
        String tableName = tableUtil.getTableName(hrEmpSalaryStandardClass);
        List<String> columns = tableUtil.getTableColumns(hrEmpSalaryStandardClass);
        String primaryKey = tableUtil.getPrimaryKey(hrEmpSalaryStandardClass);
        columns.add(primaryKey);

        log.info("开始迁移表: " + tableName);
        log.info("主键列: " + primaryKey);
        log.info("迁移列: " + String.join(", ", columns));

        long startTime = System.currentTimeMillis();

        String columnStr = String.join(", ", columns);
        String placeholders = String.join(", ",
                java.util.Collections.nCopies(columns.size(), "?"));

        // 查询总数
        long totalCount = openGaussJdbcTemplate.queryForList(
                "SELECT COUNT(*) FROM " + tableName + " GROUP BY user_code", Long.class).size();

        // Oracle连接
        try (Connection oracleConn = OracleJdbcUtil.getConnection()) {
            // 清空目标表
            try (Statement stmt = oracleConn.createStatement()) {
                stmt.execute("TRUNCATE TABLE " + tableName);
            }

            // 准备插入语句
            String insertSql = String.format(
                    "INSERT INTO %s (%s) VALUES (%s)",
                    tableName, columnStr, placeholders);

            Object lastUserCode = null; // 初始为null
            AtomicLong migratedCount = new AtomicLong(0);

            while (true) {
                // 分页查询源数据
                String querySql;
                List<Map<String, Object>> batchData;

                if (lastUserCode == null) {
                    // 第一次查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    // 先取create_time有值的
                                    "           ROW_NUMBER() OVER " +
                                    "(PARTITION BY user_code ORDER BY CASE WHEN effective_date IS NULL THEN 1 ELSE 0 END ASC, effective_date DESC, CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND user_code IS NOT NULL" +
                                    ") " +
                                    "SELECT * FROM ranked_data " +
                                    "WHERE group_rank = 1 " +
                                    "ORDER BY user_code " +
                                    "LIMIT ?",
                            // 不从视图取值
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, batchSize);
                } else {
                    // 后续查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    // 先取create_time有值的
                                    "           ROW_NUMBER() OVER " +
                                    "(PARTITION BY user_code ORDER BY CASE WHEN effective_date IS NULL THEN 1 ELSE 0 END ASC, effective_date DESC, CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND user_code IS NOT NULL AND user_code > ?" +
                                    ") " +
                                    "SELECT * FROM ranked_data " +
                                    "WHERE group_rank = 1 " +
                                    "ORDER BY user_code " +
                                    "LIMIT ?",
                            // 不从视图取值
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, lastUserCode, batchSize);
                }

                if (batchData.isEmpty()) {
                    break;
                }

                // 批量插入目标表
                try (PreparedStatement pstmt = oracleConn.prepareStatement(insertSql)) {
                    for (Map<String, Object> row : batchData) {
                        for (int i = 0; i < columns.size(); i++) {
                            pstmt.setObject(i + 1, row.get(columns.get(i)));
                        }
                        pstmt.addBatch();

                        // 更新最后ID
                        lastUserCode = row.get("user_code");
                    }
                    pstmt.executeBatch();
                }

                long currentCount = migratedCount.addAndGet(batchData.size());
                log.info("表 {} 迁移进度: {}/{} ({}%)",
                        tableName, currentCount, totalCount,
                        String.format("%.2f", currentCount * 100.0 / totalCount));
            }

            long endTime = System.currentTimeMillis();
            log.info("表 {} 迁移完成, 共 {} 条记录, 耗时 {} 毫秒",
                    tableName, migratedCount.get(), (endTime - startTime));

        } catch (SQLException e) {
            throw new RuntimeException("Oracle数据库操作失败", e);
        }
    }

    public void migrateSkillLevelEntity(Class<HrEmpSkillLevel> hrEmpSkillLevelClass) {
        String tableName = tableUtil.getTableName(hrEmpSkillLevelClass);
        List<String> columns = tableUtil.getTableColumns(hrEmpSkillLevelClass);
        String primaryKey = tableUtil.getPrimaryKey(hrEmpSkillLevelClass);
        columns.add(primaryKey);

        log.info("开始迁移表: " + tableName);
        log.info("主键列: " + primaryKey);
        log.info("迁移列: " + String.join(", ", columns));

        long startTime = System.currentTimeMillis();

        String columnStr = String.join(", ", columns);
        String placeholders = String.join(", ",
                java.util.Collections.nCopies(columns.size(), "?"));

        // 查询总数
        long totalCount = openGaussJdbcTemplate.queryForList(
                "SELECT COUNT(*) FROM " + tableName + " GROUP BY user_code", Long.class).size();

        // Oracle连接
        try (Connection oracleConn = OracleJdbcUtil.getConnection()) {
            // 清空目标表
            try (Statement stmt = oracleConn.createStatement()) {
                stmt.execute("TRUNCATE TABLE " + tableName);
            }

            // 准备插入语句
            String insertSql = String.format(
                    "INSERT INTO %s (%s) VALUES (%s)",
                    tableName, columnStr, placeholders);

            Object lastUserCode = null; // 初始为null
            AtomicLong migratedCount = new AtomicLong(0);

            while (true) {
                // 分页查询源数据
                String querySql;
                List<Map<String, Object>> batchData;

                if (lastUserCode == null) {
                    // 第一次查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    // 先取create_time有值的
                                    "           ROW_NUMBER() OVER " +
                                    "(PARTITION BY user_code ORDER BY CASE WHEN is_highest IS NULL THEN 1 ELSE 0 END ASC, is_highest DESC, " +
                                    "CASE WHEN appointed_start_date IS NULL THEN 1 ELSE 0 END ASC, CASE WHEN get_date IS NULL THEN 1 ELSE 0 END ASC, " +
                                    "get_date DESC, CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND (is_highest = '1' OR is_highest IS NULL) AND user_code IS NOT NULL" +
                                    ") " +
                                    "SELECT job_id, case when ci.code_value is null then skill_level else ci.code_value end as skill_level, get_method, " +
                                    "get_date, is_highest, is_appointed, appointed_start_date, appointed_end_date, grant_unit, certi_id, ra.id, user_code, " +
                                    "ra.deleted, ra.create_time, ra.create_by, ra.update_time, ra.update_by, ra.tenant_id, is_updating FROM ranked_data ra " +
                                    "left join code_info ci on ci.code = ra.skill_level " +
                                    "WHERE group_rank = 1 and (ci.\"type\" = 'SKILL_LEVEL' or ci.type is null) " +
                                    "ORDER BY user_code " +
                                    "LIMIT ?",
                            // 不从视图取值
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, batchSize);
                } else {
                    // 后续查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    // 先取create_time有值的
                                    "           ROW_NUMBER() OVER " +
                                    "(PARTITION BY user_code ORDER BY CASE WHEN is_highest IS NULL THEN 1 ELSE 0 END ASC, is_highest DESC, " +
                                    "CASE WHEN appointed_start_date IS NULL THEN 1 ELSE 0 END ASC, CASE WHEN get_date IS NULL THEN 1 ELSE 0 END ASC, " +
                                    "get_date DESC, CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND (is_highest = '1' OR is_highest IS NULL) AND user_code IS NOT NULL AND user_code > ?" +
                                    ") " +
                                    "SELECT job_id, case when ci.code_value is null then skill_level else ci.code_value end as skill_level, get_method, " +
                                    "get_date, is_highest, is_appointed, appointed_start_date, appointed_end_date, grant_unit, certi_id, ra.id, user_code, " +
                                    "ra.deleted, ra.create_time, ra.create_by, ra.update_time, ra.update_by, ra.tenant_id, is_updating FROM ranked_data ra " +
                                    "left join code_info ci on ci.code = ra.skill_level " +
                                    "WHERE group_rank = 1 and (ci.\"type\" = 'SKILL_LEVEL' or ci.type is null) " +
                                    "ORDER BY user_code " +
                                    "LIMIT ?",
                            // 不从视图取值
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, lastUserCode, batchSize);
                }

                if (batchData.isEmpty()) {
                    break;
                }

                // 批量插入目标表
                try (PreparedStatement pstmt = oracleConn.prepareStatement(insertSql)) {
                    for (Map<String, Object> row : batchData) {
                        for (int i = 0; i < columns.size(); i++) {
                            pstmt.setObject(i + 1, row.get(columns.get(i)));
                        }
                        pstmt.addBatch();

                        // 更新最后ID
                        lastUserCode = row.get("user_code");
                    }
                    pstmt.executeBatch();
                }

                long currentCount = migratedCount.addAndGet(batchData.size());
                log.info("表 {} 迁移进度: {}/{} ({}%)",
                        tableName, currentCount, totalCount,
                        String.format("%.2f", currentCount * 100.0 / totalCount));
            }

            long endTime = System.currentTimeMillis();
            log.info("表 {} 迁移完成, 共 {} 条记录, 耗时 {} 毫秒",
                    tableName, migratedCount.get(), (endTime - startTime));

        } catch (SQLException e) {
            throw new RuntimeException("Oracle数据库操作失败", e);
        }
    }

    public void migrateBasicEntity(Class<HrEmpBasic> hrEmpBasicClass) {
        // 获得部门翻译字典
        Map<String, String> orgCodeTranslateMap = codeInfoService.getTranslateMap(
                "hr_emp_appointment",
                "APPOINTMENT_ORG"
        );

        String tableName = tableUtil.getTableName(hrEmpBasicClass);
        List<String> columns = tableUtil.getTableColumns(hrEmpBasicClass);
        String primaryKey = tableUtil.getPrimaryKey(hrEmpBasicClass);
        columns.add(primaryKey);

        columns.remove("is_pic_updating");
        columns.add("department_code");

        log.info("开始迁移表: " + tableName);
        log.info("主键列: " + primaryKey);
        log.info("迁移列: " + String.join(", ", columns));

        long startTime = System.currentTimeMillis();

        String columnStr = String.join(", ", columns);
        String placeholders = String.join(", ",
                java.util.Collections.nCopies(columns.size(), "?"));

        // 查询总数
        long totalCount = openGaussJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName, Long.class);

        // Oracle连接
        try (Connection oracleConn = OracleJdbcUtil.getConnection()) {
            // 清空目标表
            try (Statement stmt = oracleConn.createStatement()) {
                stmt.execute("TRUNCATE TABLE " + tableName);
            }

            // 准备插入语句
            String insertSql = String.format(
                    "INSERT INTO %s (%s) VALUES (%s)",
                    tableName, columnStr, placeholders);

            Object lastUserCode = null; // 初始为null
            AtomicLong migratedCount = new AtomicLong(0);

            while (true) {
                // 分页查询源数据
                String querySql;
                List<Map<String, Object>> batchData;

                if (lastUserCode == null) {
                    // 第一次查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    "ROW_NUMBER() OVER (PARTITION BY user_code ORDER BY CASE WHEN create_time IS NULL THEN 1 ELSE 0 END, create_time DESC) AS group_rank " +
                                    "FROM %s t " +
                                    "WHERE t.DELETED = '0' AND t.user_code IS NOT NULL" +
                                    ") " +
                                    "SELECT " +
                                    "ra.id, " +
                                    "ra.user_code, " +
                                    "case when ci1.code_value is null then ra.gender else ci1.code_value end as gender, " +
                                    "case when ci2.code_value is null then ra.nation else ci2.code_value end as nation, " +
                                    "ra.native_place_name, " +
                                    "ra.nationality, " +
                                    "ra.birth_date, " +
                                    "ra.age, " +
                                    "ra.birth_place, " +
                                    "case when ci3.code_value is null then ra.political else ci3.code_value end as political, " +
                                    "ra.name, " +
                                    "ra.party_join_date, " +
                                    "ra.work_date, " +
                                    "ra.entry_date, " +
                                    "case when ci4.code_value is null then ra.emp_source else ci4.code_value end as emp_source, " +
                                    "case when ci5.code_value is null then ra.marital_status else ci5.code_value end as marital_status, " +
                                    "ra.identity_type, " +
                                    "ra.emp_job, " +
                                    "ra.emp_rank, " +
                                    "ra.campus_period, " +
                                    "ra.deleted, " +
                                    "ra.create_time, " +
                                    "ra.create_by, " +
                                    "ra.update_time, " +
                                    "ra.update_by, " +
                                    "ra.tenant_id, " +
                                    "ra.hire_date, " +
                                    "ra.native_place_code, " +
                                    "ra.dept, " +
                                    "ra.cost_center, " +
                                    "ra.is_updating, " +
                                    "case when ci6.code_value is null then ra.category1 else ci6.code_value end as category1, " +
                                    "case when ci7.code_value is null then ra.category4 else ci7.code_value end as category4, " +
                                    "case when ci8.code_value is null then ra.category7 else ci8.code_value end as category7, " +
                                    "case when ci9.code_value is null then ra.category8 else ci9.code_value end as category8, " +
                                    "ra.status, " +
                                    "ra.enlistment_date, " +
                                    "ra.former_name, " +
                                    "ra.promotion_time, " +
                                    "ra.death_time, " +
                                    "ra.service_break_time, " +
                                    "ra.is_employed, " +
                                    "ra.emp_type, " +
                                    "ra.file_header_id, " +
                                    "ra.profile_picture " +
                                    "FROM ranked_data ra " +
                                    "LEFT JOIN code_info ci1 ON ci1.code = ra.gender AND ci1.\"type\" = 'BASIC_GENDER' " +
                                    "LEFT JOIN code_info ci2 ON ci2.code = ra.nation AND ci2.\"type\" = 'BASIC_NATION' " +
                                    "LEFT JOIN code_info ci3 ON ci3.code = ra.political AND ci3.\"type\" = 'BASIC_POLITICAL' " +
                                    "LEFT JOIN code_info ci4 ON ci4.code = ra.emp_source AND ci4.\"type\" = 'BASIC_EMP_SOURCE' " +
                                    "LEFT JOIN code_info ci5 ON ci5.code = ra.marital_status AND ci5.\"type\" = 'BASIC_MARITAL_STATUS' " +
                                    "LEFT JOIN code_info ci6 ON ci6.code = ra.category1 AND ci6.\"type\" = 'category1' " +
                                    "LEFT JOIN code_info ci7 ON ci7.code = ra.category4 AND ci7.\"type\" = 'category4' " +
                                    "LEFT JOIN code_info ci8 ON ci8.code = ra.category7 AND ci8.\"type\" = 'category7' " +
                                    "LEFT JOIN code_info ci9 ON ci9.code = ra.category8 AND ci9.\"type\" = 'category8' " +
                                    "WHERE group_rank = 1 " +
                                    "ORDER BY user_code " +
                                    "LIMIT ?",
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, batchSize);
                } else {
                    // 后续查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    "ROW_NUMBER() OVER (PARTITION BY user_code ORDER BY CASE WHEN create_time IS NULL THEN 1 ELSE 0 END, create_time DESC) AS group_rank " +
                                    "FROM %s t " +
                                    "WHERE t.DELETED = '0' AND t.user_code IS NOT NULL AND t.user_code > ? " +
                                    ") " +
                                    "SELECT " +
                                    "ra.id, " +
                                    "ra.user_code, " +
                                    "case when ci1.code_value is null then ra.gender else ci1.code_value end as gender, " +
                                    "case when ci2.code_value is null then ra.nation else ci2.code_value end as nation, " +
                                    "ra.native_place_name, " +
                                    "ra.nationality, " +
                                    "ra.birth_date, " +
                                    "ra.age, " +
                                    "ra.birth_place, " +
                                    "case when ci3.code_value is null then ra.political else ci3.code_value end as political, " +
                                    "ra.name, " +
                                    "ra.party_join_date, " +
                                    "ra.work_date, " +
                                    "ra.entry_date, " +
                                    "case when ci4.code_value is null then ra.emp_source else ci4.code_value end as emp_source, " +
                                    "case when ci5.code_value is null then ra.marital_status else ci5.code_value end as marital_status, " +
                                    "ra.identity_type, " +
                                    "ra.emp_job, " +
                                    "ra.emp_rank, " +
                                    "ra.campus_period, " +
                                    "ra.deleted, " +
                                    "ra.create_time, " +
                                    "ra.create_by, " +
                                    "ra.update_time, " +
                                    "ra.update_by, " +
                                    "ra.tenant_id, " +
                                    "ra.hire_date, " +
                                    "ra.native_place_code, " +
                                    "ra.dept, " +
                                    "ra.cost_center, " +
                                    "ra.is_updating, " +
                                    "case when ci6.code_value is null then ra.category1 else ci6.code_value end as category1, " +
                                    "case when ci7.code_value is null then ra.category4 else ci7.code_value end as category4, " +
                                    "case when ci8.code_value is null then ra.category7 else ci8.code_value end as category7, " +
                                    "case when ci9.code_value is null then ra.category8 else ci9.code_value end as category8, " +
                                    "ra.status, " +
                                    "ra.enlistment_date, " +
                                    "ra.former_name, " +
                                    "ra.promotion_time, " +
                                    "ra.death_time, " +
                                    "ra.service_break_time, " +
                                    "ra.is_employed, " +
                                    "ra.emp_type, " +
                                    "ra.file_header_id, " +
                                    "ra.profile_picture " +
                                    "FROM ranked_data ra " +
                                    "LEFT JOIN code_info ci1 ON ci1.code = ra.gender AND ci1.\"type\" = 'BASIC_GENDER' " +
                                    "LEFT JOIN code_info ci2 ON ci2.code = ra.nation AND ci2.\"type\" = 'BASIC_NATION' " +
                                    "LEFT JOIN code_info ci3 ON ci3.code = ra.political AND ci3.\"type\" = 'BASIC_POLITICAL' " +
                                    "LEFT JOIN code_info ci4 ON ci4.code = ra.emp_source AND ci4.\"type\" = 'BASIC_EMP_SOURCE' " +
                                    "LEFT JOIN code_info ci5 ON ci5.code = ra.marital_status AND ci5.\"type\" = 'BASIC_MARITAL_STATUS' " +
                                    "LEFT JOIN code_info ci6 ON ci6.code = ra.category1 AND ci6.\"type\" = 'category1' " +
                                    "LEFT JOIN code_info ci7 ON ci7.code = ra.category4 AND ci7.\"type\" = 'category4' " +
                                    "LEFT JOIN code_info ci8 ON ci8.code = ra.category7 AND ci8.\"type\" = 'category7' " +
                                    "LEFT JOIN code_info ci9 ON ci9.code = ra.category8 AND ci9.\"type\" = 'category8' " +
                                    "WHERE group_rank = 1 " +
                                    "ORDER BY user_code " +
                                    "LIMIT ?",
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, lastUserCode, batchSize);
                }

                if (batchData.isEmpty()) {
                    break;
                }

                // 复制部门字段用于RSL_GZK的列支单位
                batchData.forEach(item -> item.put("department_code", item.get("dept")));

                // 翻译部门字段
                batchData.forEach(item -> item.computeIfPresent("dept", (k, v) -> orgCodeTranslateMap.get(String.valueOf(v))));

                // 批量插入目标表
                try (PreparedStatement pstmt = oracleConn.prepareStatement(insertSql)) {
                    for (Map<String, Object> row : batchData) {
                        for (int i = 0; i < columns.size(); i++) {
                            pstmt.setObject(i + 1, row.get(columns.get(i)));
                        }
                        pstmt.addBatch();

                        // 更新最后ID
                        lastUserCode = row.get("user_code");
                    }
                    pstmt.executeBatch();
                }

                long currentCount = migratedCount.addAndGet(batchData.size());
                log.info("表 {} 迁移进度: {}/{} ({}%)",
                        tableName, currentCount, totalCount,
                        String.format("%.2f", currentCount * 100.0 / totalCount));
            }

            long endTime = System.currentTimeMillis();
            log.info("表 {} 迁移完成, 共 {} 条记录, 耗时 {} 毫秒",
                    tableName, migratedCount.get(), (endTime - startTime));

        } catch (SQLException e) {
            throw new RuntimeException("Oracle数据库操作失败", e);
        }
    }

    public void migrateOtherAppointmentEntity(Class<HrEmpOtherAppointment> hrEmpOtherAppointmentClass) {
        String tableName = tableUtil.getTableName(hrEmpOtherAppointmentClass);
        List<String> columns = tableUtil.getTableColumns(hrEmpOtherAppointmentClass);
        String primaryKey = tableUtil.getPrimaryKey(hrEmpOtherAppointmentClass);
        columns.add(primaryKey);

        log.info("开始迁移表: " + tableName);
        log.info("主键列: " + primaryKey);
        log.info("迁移列: " + String.join(", ", columns));

        long startTime = System.currentTimeMillis();

        String columnStr = String.join(", ", columns);
        String placeholders = String.join(", ",
                java.util.Collections.nCopies(columns.size(), "?"));

        // 查询总数
        long totalCount = openGaussJdbcTemplate.queryForList(
                "SELECT COUNT(*) FROM " + tableName + " GROUP BY user_code", Long.class).size();

        // Oracle连接
        try (Connection oracleConn = OracleJdbcUtil.getConnection()) {
            // 清空目标表
            try (Statement stmt = oracleConn.createStatement()) {
                stmt.execute("TRUNCATE TABLE " + tableName);
            }

            // 准备插入语句
            String insertSql = String.format(
                    "INSERT INTO %s (%s) VALUES (%s)",
                    tableName, columnStr, placeholders);

            Object lastUserCode = null; // 初始为null
            AtomicLong migratedCount = new AtomicLong(0);

            while (true) {
                // 分页查询源数据
                String querySql;
                List<Map<String, Object>> batchData;

                if (lastUserCode == null) {
                    // 第一次查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    // 先取create_time有值的
                                    "           ROW_NUMBER() OVER " +
                                    "(PARTITION BY user_code ORDER BY CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC, CASE WHEN start_date IS NULL THEN 1 ELSE 0 END ASC, start_date DESC, CASE WHEN end_date IS NULL THEN 1 ELSE 0 END ASC, end_date DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND user_code IS NOT NULL" +
                                    ") " +
                                    "SELECT start_date, expected_end_date, end_date, personnel_category, org, dept, org_unit, archive_relation_weaken, " +
                                    "case when ci.code_value is null then \"position\" else ci.code_value end as \"position\", " +
                                    "case when ci1.code_value is null then other_appointment_type else ci1.code_value end as other_appointment_type, " +
                                    "external_app, case when ci3.code_value is null then position_category else ci3.code_value end as position_category, " +
                                    "case when ci2.code_value is null then duty_status else ci2.code_value end as duty_status, " +
                                    "org_path, dept_path, ra.id, user_code, ra.deleted, ra.create_time, ra.create_by, ra.update_time, ra.update_by, " +
                                    "ra.tenant_id, pci.\"name\" as job_type FROM ranked_data ra " +
                                    "LEFT JOIN code_info ci ON ci.code = ra.\"position\" and ci.\"type\" = 'APPOINTMENT_POSITION' " +
                                    "left join code_info ci1 on ci1.code = ra.other_appointment_type and ci1.\"type\" = 'APPOINTMENT_CHANGE_TYPE' " +
                                    "left join code_info ci2 on ci2.code = ra.duty_status and ci2.\"type\" = 'APPOINTMENT_DUTY_STATUS' " +
                                    "left join code_info ci3 on ci3.code = ra.position_category and ci3.\"type\" = 'APPOINTMENT_POSITION_CATEGORY' " +
                                    "left join post_catalog_info pci on pci.code = ra.job_type " +
                                    "WHERE group_rank = 1 " +
                                    "ORDER BY user_code " +
                                    "LIMIT ?",
                            // 不从视图取值
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, batchSize);
                } else {
                    // 后续查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    // 先取create_time有值的
                                    "           ROW_NUMBER() OVER " +
                                    "(PARTITION BY user_code ORDER BY CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC, CASE WHEN start_date IS NULL THEN 1 ELSE 0 END ASC, start_date DESC, CASE WHEN end_date IS NULL THEN 1 ELSE 0 END ASC, end_date DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND user_code IS NOT NULL AND user_code > ?" +
                                    ") " +
                                    "SELECT start_date, expected_end_date, end_date, personnel_category, org, dept, org_unit, archive_relation_weaken, " +
                                    "case when ci.code_value is null then \"position\" else ci.code_value end as \"position\", " +
                                    "case when ci1.code_value is null then other_appointment_type else ci1.code_value end as other_appointment_type, " +
                                    "external_app, case when ci3.code_value is null then position_category else ci3.code_value end as position_category, " +
                                    "case when ci2.code_value is null then duty_status else ci2.code_value end as duty_status, " +
                                    "org_path, dept_path, ra.id, user_code, ra.deleted, ra.create_time, ra.create_by, ra.update_time, ra.update_by, " +
                                    "ra.tenant_id, pci.\"name\" as job_type FROM ranked_data ra " +
                                    "LEFT JOIN code_info ci ON ci.code = ra.\"position\" and ci.\"type\" = 'APPOINTMENT_POSITION' " +
                                    "left join code_info ci1 on ci1.code = ra.other_appointment_type and ci1.\"type\" = 'APPOINTMENT_CHANGE_TYPE' " +
                                    "left join code_info ci2 on ci2.code = ra.duty_status and ci2.\"type\" = 'APPOINTMENT_DUTY_STATUS' " +
                                    "left join code_info ci3 on ci3.code = ra.position_category and ci3.\"type\" = 'APPOINTMENT_POSITION_CATEGORY' " +
                                    "left join post_catalog_info pci on pci.code = ra.job_type " +
                                    "WHERE group_rank = 1 " +
                                    "ORDER BY user_code " +
                                    "LIMIT ?",
                            // 不从视图取值
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, lastUserCode, batchSize);
                }

                if (batchData.isEmpty()) {
                    break;
                }

                // 手动翻译部门
                // 获得部门翻译字典
                Map<String, String> orgCodeTranslateMap = codeInfoService.getTranslateMap(
                        "hr_emp_appointment",
                        "APPOINTMENT_ORG"
                );

                // 翻译部门字段
                batchData.forEach(item -> item.computeIfPresent("org", (k, v) -> orgCodeTranslateMap.getOrDefault(String.valueOf(v), String.valueOf(v))));
                batchData.forEach(item -> item.computeIfPresent("dept", (k, v) -> orgCodeTranslateMap.getOrDefault(String.valueOf(v), String.valueOf(v))));

                // 批量插入目标表
                try (PreparedStatement pstmt = oracleConn.prepareStatement(insertSql)) {
                    for (Map<String, Object> row : batchData) {
                        for (int i = 0; i < columns.size(); i++) {
                            pstmt.setObject(i + 1, row.get(columns.get(i)));
                        }
                        pstmt.addBatch();

                        // 更新最后ID
                        lastUserCode = row.get("user_code");
                    }
                    pstmt.executeBatch();
                }

                long currentCount = migratedCount.addAndGet(batchData.size());
                log.info("表 {} 迁移进度: {}/{} ({}%)",
                        tableName, currentCount, totalCount,
                        String.format("%.2f", currentCount * 100.0 / totalCount));
            }

            long endTime = System.currentTimeMillis();
            log.info("表 {} 迁移完成, 共 {} 条记录, 耗时 {} 毫秒",
                    tableName, migratedCount.get(), (endTime - startTime));

        } catch (SQLException e) {
            throw new RuntimeException("Oracle数据库操作失败", e);
        }
    }

    public void migrateResumeEntity(Class<HrEmpResume> hrEmpResumeClass) {
        String tableName = tableUtil.getTableName(hrEmpResumeClass);
        List<String> columns = tableUtil.getTableColumns(hrEmpResumeClass);
        String primaryKey = tableUtil.getPrimaryKey(hrEmpResumeClass);
        columns.add(primaryKey);

        log.info("开始迁移表: " + tableName);
        log.info("主键列: " + primaryKey);
        log.info("迁移列: " + String.join(", ", columns));

        long startTime = System.currentTimeMillis();

        String columnStr = String.join(", ", columns);
        String placeholders = String.join(", ",
                java.util.Collections.nCopies(columns.size(), "?"));

        // 查询总数
        long totalCount = openGaussJdbcTemplate.queryForList(
                "SELECT COUNT(*) FROM " + tableName + " GROUP BY user_code", Long.class).size();

        // Oracle连接
        try (Connection oracleConn = OracleJdbcUtil.getConnection()) {
            // 清空目标表
            try (Statement stmt = oracleConn.createStatement()) {
                stmt.execute("TRUNCATE TABLE " + tableName);
            }

            // 准备插入语句
            String insertSql = String.format(
                    "INSERT INTO %s (%s) VALUES (%s)",
                    tableName, columnStr, placeholders);

            Object lastUserCode = null; // 初始为null
            AtomicLong migratedCount = new AtomicLong(0);

            while (true) {
                // 分页查询源数据
                String querySql;
                List<Map<String, Object>> batchData;

                if (lastUserCode == null) {
                    // 第一次查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    // 先取create_time有值的
                                    "           ROW_NUMBER() OVER " +
                                    "(PARTITION BY user_code ORDER BY CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC, CASE WHEN start_date IS NULL THEN 1 ELSE 0 END ASC, start_date DESC, CASE WHEN end_date IS NULL THEN 1 ELSE 0 END ASC, end_date DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND user_code IS NOT NULL" +
                                    ") " +
                                    "SELECT ra.id, user_code, start_date, end_date, unit, department, " +
                                    "case when ci.code_value is null then \"position\" else ci.code_value end as \"position\", " +
                                    "ra.deleted, ra.create_time, ra.create_by, ra.update_time, ra.update_by, ra.tenant_id " +
                                    "FROM ranked_data ra " +
                                    "left join code_info ci on ci.code = ra.\"position\" and ci.\"type\" = 'APPOINTMENT_POSITION' " +
                                    "WHERE group_rank = 1 " +
                                    "ORDER BY user_code " +
                                    "LIMIT ?",
                            // 不从视图取值
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, batchSize);
                } else {
                    // 后续查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    // 先取create_time有值的
                                    "           ROW_NUMBER() OVER " +
                                    "(PARTITION BY user_code ORDER BY CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC, CASE WHEN start_date IS NULL THEN 1 ELSE 0 END ASC, start_date DESC, CASE WHEN end_date IS NULL THEN 1 ELSE 0 END ASC, end_date DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND user_code IS NOT NULL AND user_code > ? " +
                                    ") " +
                                    "SELECT ra.id, user_code, start_date, end_date, unit, department, " +
                                    "case when ci.code_value is null then \"position\" else ci.code_value end as \"position\", " +
                                    "ra.deleted, ra.create_time, ra.create_by, ra.update_time, ra.update_by, ra.tenant_id " +
                                    "FROM ranked_data ra " +
                                    "left join code_info ci on ci.code = ra.\"position\" and ci.\"type\" = 'APPOINTMENT_POSITION' " +
                                    "WHERE group_rank = 1 " +
                                    "ORDER BY user_code " +
                                    "LIMIT ?",
                            // 不从视图取值
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, lastUserCode, batchSize);
                }

                if (batchData.isEmpty()) {
                    break;
                }

                // 批量插入目标表
                try (PreparedStatement pstmt = oracleConn.prepareStatement(insertSql)) {
                    for (Map<String, Object> row : batchData) {
                        for (int i = 0; i < columns.size(); i++) {
                            pstmt.setObject(i + 1, row.get(columns.get(i)));
                        }
                        pstmt.addBatch();

                        // 更新最后ID
                        lastUserCode = row.get("user_code");
                    }
                    pstmt.executeBatch();
                }

                long currentCount = migratedCount.addAndGet(batchData.size());
                log.info("表 {} 迁移进度: {}/{} ({}%)",
                        tableName, currentCount, totalCount,
                        String.format("%.2f", currentCount * 100.0 / totalCount));
            }

            long endTime = System.currentTimeMillis();
            log.info("表 {} 迁移完成, 共 {} 条记录, 耗时 {} 毫秒",
                    tableName, migratedCount.get(), (endTime - startTime));

        } catch (SQLException e) {
            throw new RuntimeException("Oracle数据库操作失败", e);
        }
    }


    public void migrateHealthEntity(Class<HrEmpHealth> hrEmpHealthClass) {
        String tableName = tableUtil.getTableName(hrEmpHealthClass);
        List<String> columns = tableUtil.getTableColumns(hrEmpHealthClass);
        String primaryKey = tableUtil.getPrimaryKey(hrEmpHealthClass);
        columns.add(primaryKey);

        log.info("开始迁移表: " + tableName);
        log.info("主键列: " + primaryKey);
        log.info("迁移列: " + String.join(", ", columns));

        long startTime = System.currentTimeMillis();

        String columnStr = String.join(", ", columns);
        String placeholders = String.join(", ",
                java.util.Collections.nCopies(columns.size(), "?"));

        // 查询总数
        long totalCount = openGaussJdbcTemplate.queryForList(
                "SELECT COUNT(*) FROM " + tableName + " GROUP BY user_code", Long.class).size();

        // Oracle连接
        try (Connection oracleConn = OracleJdbcUtil.getConnection()) {
            // 清空目标表
            try (Statement stmt = oracleConn.createStatement()) {
                stmt.execute("TRUNCATE TABLE " + tableName);
            }

            // 准备插入语句
            String insertSql = String.format(
                    "INSERT INTO %s (%s) VALUES (%s)",
                    tableName, columnStr, placeholders);

            Object lastUserCode = null; // 初始为null
            AtomicLong migratedCount = new AtomicLong(0);

            while (true) {
                // 分页查询源数据
                String querySql;
                List<Map<String, Object>> batchData;

                if (lastUserCode == null) {
                    // 第一次查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    // 先取create_time有值的
                                    "           ROW_NUMBER() OVER " +
                                    "(PARTITION BY user_code ORDER BY CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND user_code IS NOT NULL" +
                                    ") " +
                                    "SELECT ra.id, user_code, " +
                                    "case when ci.code_value is null then ra.health_status else ci.code_value end as health_status, " +
                                    "ra.deleted, ra.create_time, ra.create_by, ra.update_time, ra.update_by, ra.tenant_id " +
                                    "FROM ranked_data ra " +
                                    "left join code_info ci on ci.code = ra.health_status and ci.\"type\" = 'BASIC_HEALTH_STATUS' " +
                                    "WHERE group_rank = 1 " +
                                    "ORDER BY user_code " +
                                    "LIMIT ?",
                            // 不从视图取值
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, batchSize);
                } else {
                    // 后续查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    // 先取create_time有值的
                                    "           ROW_NUMBER() OVER " +
                                    "(PARTITION BY user_code ORDER BY CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND user_code IS NOT NULL AND user_code > ? " +
                                    ") " +
                                    "SELECT ra.id, user_code, " +
                                    "case when ci.code_value is null then ra.health_status else ci.code_value end as health_status, " +
                                    "ra.deleted, ra.create_time, ra.create_by, ra.update_time, ra.update_by, ra.tenant_id " +
                                    "FROM ranked_data ra " +
                                    "left join code_info ci on ci.code = ra.health_status and ci.\"type\" = 'BASIC_HEALTH_STATUS' " +
                                    "WHERE group_rank = 1 " +
                                    "ORDER BY user_code " +
                                    "LIMIT ?",
                            // 不从视图取值
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, lastUserCode, batchSize);
                }

                if (batchData.isEmpty()) {
                    break;
                }

                // 批量插入目标表
                try (PreparedStatement pstmt = oracleConn.prepareStatement(insertSql)) {
                    for (Map<String, Object> row : batchData) {
                        for (int i = 0; i < columns.size(); i++) {
                            pstmt.setObject(i + 1, row.get(columns.get(i)));
                        }
                        pstmt.addBatch();

                        // 更新最后ID
                        lastUserCode = row.get("user_code");
                    }
                    pstmt.executeBatch();
                }

                long currentCount = migratedCount.addAndGet(batchData.size());
                log.info("表 {} 迁移进度: {}/{} ({}%)",
                        tableName, currentCount, totalCount,
                        String.format("%.2f", currentCount * 100.0 / totalCount));
            }

            long endTime = System.currentTimeMillis();
            log.info("表 {} 迁移完成, 共 {} 条记录, 耗时 {} 毫秒",
                    tableName, migratedCount.get(), (endTime - startTime));

        } catch (SQLException e) {
            throw new RuntimeException("Oracle数据库操作失败", e);
        }
    }


    public void migrateSalaryInfoEntity(Class<HrEmpSalaryInfo> hrEmpSalaryInfoClass) {
        String tableName = tableUtil.getTableName(hrEmpSalaryInfoClass);
        List<String> columns = tableUtil.getTableColumns(hrEmpSalaryInfoClass);
        String primaryKey = tableUtil.getPrimaryKey(hrEmpSalaryInfoClass);
        columns.add(primaryKey);

        log.info("开始迁移表: " + tableName);
        log.info("主键列: " + primaryKey);
        log.info("迁移列: " + String.join(", ", columns));

        long startTime = System.currentTimeMillis();

        String columnStr = String.join(", ", columns);
        String placeholders = String.join(", ",
                java.util.Collections.nCopies(columns.size(), "?"));

        // 查询总数
        long totalCount = openGaussJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName, Long.class);

        // Oracle连接
        try (Connection oracleConn = OracleJdbcUtil.getConnection()) {
//            // 薪酬表不清空全部数据，只清空本表中可能存在的所有user_code字段能匹配上的数据
//            try (PreparedStatement deleteStmt = oracleConn.prepareStatement(
//                    "DELETE FROM " + tableName + " WHERE user_code IN (" +
//                            "SELECT DISTINCT user_code FROM " + tableName + " WHERE DELETED = '0' AND user_code IS NOT NULL)")) {
//                int deletedCount = deleteStmt.executeUpdate();
//                log.info("已清理{}条与源表user_code匹配的旧数据", deletedCount);
//            }
            // 准备插入语句
            String insertSql = String.format(
                    "INSERT INTO %s (%s) VALUES (%s)",
                    tableName, columnStr, placeholders);

            Object lastUserCode = null; // 初始为null
            AtomicLong migratedCount = new AtomicLong(0);

            while (true) {
                // 分页查询源数据
                String querySql;
                List<Map<String, Object>> batchData;

                if (lastUserCode == null) {
                    // 第一次查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    // 先取create_time有值的
                                    "           ROW_NUMBER() OVER " +
                                    "(PARTITION BY user_code ORDER BY CASE WHEN create_time IS NULL THEN 1 ELSE 0 END, create_time DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND user_code IS NOT NULL" +
                                    ") " +
                                    "SELECT * FROM ranked_data " +
                                    "WHERE group_rank = 1 " +
                                    "ORDER BY user_code " +
                                    "LIMIT ?",
                            // 不从视图取值
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, batchSize);
                } else {
                    // 后续查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    // 先取create_time有值的
                                    "           ROW_NUMBER() OVER " +
                                    "(PARTITION BY user_code ORDER BY CASE WHEN create_time IS NULL THEN 1 ELSE 0 END, create_time DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND user_code IS NOT NULL AND user_code > ?" +
                                    ") " +
                                    "SELECT * FROM ranked_data " +
                                    "WHERE group_rank = 1 " +
                                    "ORDER BY user_code " +
                                    "LIMIT ?",
                            // 不从视图取值
                            tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, lastUserCode, batchSize);
                }

                if (batchData.isEmpty()) {
                    break;
                }

                batchData.forEach(item -> item.replaceAll((k, v) -> v == null && !"create_time".equals(k)  && !"update_time".equals(k) ? "0" : v));

                // 薪酬表不清空全部数据，仅删除与源表user_code匹配的记录
                try (PreparedStatement deleteStmt = oracleConn.prepareStatement(
                        "DELETE FROM " + tableName + " WHERE user_code IN (" +
                                // 需要构建user_code列表
                                batchData.stream()
                                        .map(data -> data.get("user_code").toString())
                                        .collect(Collectors.joining("','", "'", "'")) +
                                ")")) {
                    int deletedCount = deleteStmt.executeUpdate();
                    log.info("已清理{}条与源表user_code匹配的旧数据", deletedCount);
                }


                // 批量插入目标表
                try (PreparedStatement pstmt = oracleConn.prepareStatement(insertSql)) {
                    for (Map<String, Object> row : batchData) {
                        for (int i = 0; i < columns.size(); i++) {
                            pstmt.setObject(i + 1, row.get(columns.get(i)));
                        }
                        pstmt.addBatch();

                        // 更新最后ID
                        lastUserCode = row.get("user_code");
                    }
                    pstmt.executeBatch();
                }

                long currentCount = migratedCount.addAndGet(batchData.size());
                log.info("表 {} 迁移进度: {}/{} ({}%)",
                        tableName, currentCount, totalCount,
                        String.format("%.2f", currentCount * 100.0 / totalCount));


            }

            long endTime = System.currentTimeMillis();
            log.info("表 {} 迁移完成, 共 {} 条记录, 耗时 {} 毫秒",
                    tableName, migratedCount.get(), (endTime - startTime));

        } catch (SQLException e) {
            throw new RuntimeException("Oracle数据库操作失败", e);
        }
    }
}