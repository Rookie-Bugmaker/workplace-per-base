package com.crrc.service;

import com.crrc.entity.*;
import com.crrc.utils.MyBatisPlusTableUtil;
import com.crrc.utils.OracleJdbcUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
                "SELECT COUNT(*) FROM " + "v_"+tableName, Long.class);

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
                "SELECT COUNT(*) FROM " + "v_"+tableName, Long.class);

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
                                    "           ROW_NUMBER() OVER (PARTITION BY user_code ORDER BY education DESC) AS group_rank " +  // 改为按education排序
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND user_code IS NOT NULL AND education IS NOT NULL" +  // 增加education不为空条件
                                    ") " +
                                    "SELECT * FROM ranked_data " +
                                    "WHERE group_rank <= 2 " +  // 取每组前两条
                                    "ORDER BY user_code " +  // 外层仍按user_code排序保证分页连续性
                                    "LIMIT ?",
                            "v_"+tableName);
                    batchData = openGaussJdbcTemplate.queryForList(querySql, batchSize);
                } else {
                    // 后续查询
                    querySql = String.format(
                            "WITH ranked_data AS (" +
                                    "    SELECT *, " +
                                    "           ROW_NUMBER() OVER (PARTITION BY user_code ORDER BY education DESC) AS group_rank " +  // 改为按education排序
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND user_code IS NOT NULL AND user_code > ? AND education IS NOT NULL" +  // 增加education不为空条件
                                    ") " +
                                    "SELECT * FROM ranked_data " +
                                    "WHERE group_rank <= 2 " +  // 取每组前两条
                                    "ORDER BY user_code " +  // 外层仍按user_code排序保证分页连续性
                                    "LIMIT ?",
                            "v_"+tableName);
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
                "SELECT COUNT(*) FROM " + "v_"+tableName + " WHERE DELETED = '0'", Long.class);

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
            String querySql = "SELECT * FROM " + "v_"+tableName + " WHERE DELETED = '0'";
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
        List<String> columns = tableUtil.getTableColumns(appointmentClass);
        String primaryKey = tableUtil.getPrimaryKey(appointmentClass);
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
                "SELECT COUNT(*) FROM " + tableName + " GROUP BY user_code", Long.class);

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
                                    "(PARTITION BY user_code ORDER BY CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC, start_date DESC, end_date DESC) " +
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
                                    "(PARTITION BY user_code ORDER BY CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC, start_date DESC, end_date DESC) " +
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
        long totalCount = openGaussJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " GROUP BY user_code", Long.class);

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
                                    "(PARTITION BY user_code ORDER BY CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC, effective_date DESC, qualification_date DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND is_expired = '0' AND user_code IS NOT NULL" +
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
                                    "(PARTITION BY user_code ORDER BY CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC, effective_date DESC, qualification_date DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND is_expired = '0' AND user_code IS NOT NULL AND user_code > ?" +
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
        long totalCount = openGaussJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " GROUP BY user_code", Long.class);

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
                                    "    WHERE DELETED = '0' AND is_highest = '1' OR is_highest IS NULL AND user_code IS NOT NULL" +
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
                                    "(PARTITION BY user_code ORDER BY CASE WHEN is_highest IS NULL THEN 1 ELSE 0 END ASC, is_highest DESC, CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND is_highest = '1' OR is_highest IS NULL AND user_code IS NOT NULL AND user_code > ?" +
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
        long totalCount = openGaussJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " GROUP BY user_code", Long.class);

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
        long totalCount = openGaussJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " GROUP BY user_code", Long.class);

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
                                    "(PARTITION BY user_code ORDER BY CASE WHEN is_highest IS NULL THEN 1 ELSE 0 END ASC, is_highest DESC, CASE WHEN appointed_start_date IS NULL THEN 1 ELSE 0 END ASC, CASE WHEN get_date IS NULL THEN 1 ELSE 0 END ASC, get_date DESC, CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND is_highest = '1' OR is_highest IS NULL AND user_code IS NOT NULL" +
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
                                    "(PARTITION BY user_code ORDER BY CASE WHEN is_highest IS NULL THEN 1 ELSE 0 END ASC, is_highest DESC, CASE WHEN appointed_start_date IS NULL THEN 1 ELSE 0 END ASC, CASE WHEN get_date IS NULL THEN 1 ELSE 0 END ASC, get_date DESC, CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, create_time DESC) " +
                                    "AS group_rank " +
                                    "    FROM %s " +
                                    "    WHERE DELETED = '0' AND is_highest = '1' OR is_highest IS NULL AND user_code IS NOT NULL AND user_code > ?" +
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
}