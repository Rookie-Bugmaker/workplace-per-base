package com.crrc.mapper;

import cn.hutool.core.lang.Opt;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crrc.entity.HrEmpSalaryStandard;
import org.apache.ibatis.annotations.Mapper;
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
}