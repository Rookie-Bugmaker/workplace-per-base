package com.crrc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crrc.entity.HrEmpSalaryStandard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 薪资标准 Mapper 接口
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Mapper
public interface HrEmpSalaryStandardMapper extends BaseMapper<HrEmpSalaryStandard> {

    @Select("<script> " +
            "SELECT * " +
            "    FROM ( " +
            "        SELECT " +
            "            t.*, " +
            "            ROW_NUMBER() OVER ( " +
            "                PARTITION BY t.user_code " +
            "                ORDER BY " +
            "                    t.create_time     DESC NULLS LAST, " +
            "                    t.effective_date  DESC NULLS LAST, " +
            "                    t.id DESC " +
            "            ) AS rn " +
            "        FROM hr_emp_salary_standard t " +
            "        WHERE t.user_code IN " +
            "        <foreach collection=\"userCodes\" item=\"uc\" open=\"(\" separator=\",\" close=\")\"> " +
            "            #{uc} " +
            "        </foreach> " +
            "    ) x " +
            "    WHERE x.rn = 1" +
            "</script>"
    )
    List<HrEmpSalaryStandard> selectLatestByUserCodes(List<String> userCodes);

    /**
     * 查询 coefficient * 100 != position_salary 的用户列表
     */
    @Select("<script>" +
            "SELECT DISTINCT t.user_code " +
            "FROM hr_emp_salary_standard t " +
            "LEFT JOIN hr_emp_salary_info a ON a.user_code = t.user_code " +
            "WHERE a.position_salary IS NOT NULL " +
            "  AND a.position_salary != '' " +
            "  AND t.coefficient IS NOT NULL " +
            "  AND t.coefficient != '' " +
            "  AND CAST(t.coefficient AS NUMERIC) * 100 != CAST(a.position_salary AS NUMERIC)" +
            "</script>")
    List<String> findInconsistentUserCodes();

    /**
     * 查询指定用户列表中，每个用户最新的薪资标准ID
     * 排序逻辑与你的迁移SQL完全一致：
     * effective_date IS NULL 排后面，effective_date DESC，
     * create_time IS NULL 排后面，create_time DESC
     */
    @Select("<script>" +
            "WITH ranked_data AS (" +
            "    SELECT id, user_code, tenant_id," +
            "           ROW_NUMBER() OVER (" +
            "               PARTITION BY user_code " +
            "               ORDER BY " +
            "                   CASE WHEN effective_date IS NULL THEN 1 ELSE 0 END ASC, " +
            "                   effective_date DESC, " +
            "                   CASE WHEN create_time IS NULL THEN 1 ELSE 0 END ASC, " +
            "                   create_time DESC" +
            "           ) AS group_rank " +
            "    FROM hr_emp_salary_standard " +
            "    WHERE deleted = 0 " +
            "      AND user_code IS NOT NULL " +
            "      AND user_code IN " +
            "      <foreach collection='userCodeList' item='code' open='(' separator=',' close=')'>" +
            "          #{code}" +
            "      </foreach>" +
            ") " +
            "SELECT id FROM ranked_data WHERE group_rank = 1" +
            "</script>")
    List<String> findLatestStandardIds(@Param("userCodeList") List<String> userCodeList);
}