package com.crrc.mapper;

import com.crrc.entity.HrEmpEducation;
import com.crrc.entity.HrEmpSalaryStandard;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 员工教育信息表 Mapper 接口
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Mapper
public interface HrEmpEducationMapper extends MPJBaseMapper<HrEmpEducation> {
    @Select("<script> " +
            "SELECT * " +
            "    FROM ( " +
            "        SELECT " +
            "            t.*, " +
            "            ROW_NUMBER() OVER ( " +
            "                PARTITION BY t.user_code " +
            "                ORDER BY " +
            "                    t.school_entry_date  DESC NULLS LAST, " +
            "                    t.education          ASC NULLS LAST,  " +
            "                    t.create_time        DESC NULLS LAST, " +
            "                    t.id DESC " +
            "            ) AS rn " +
            "        FROM hr_emp_education t " +
            "        WHERE t.user_code IN " +
            "        <foreach collection=\"userCodes\" item=\"uc\" open=\"(\" separator=\",\" close=\")\"> " +
            "            #{uc}                     " +
            "        </foreach>                    " +
            "        AND t.is_full_time = '00010'  " +
            "    ) x " +
            "    WHERE x.rn = 1" +
            "</script>"
    )
    List<HrEmpEducation> selectLatestFullTimeByUserCodes(List<String> userCodes);

    @Select("<script> " +
            "SELECT * " +
            "    FROM ( " +
            "        SELECT " +
            "            t.*, " +
            "            ROW_NUMBER() OVER ( " +
            "                PARTITION BY t.user_code " +
            "                ORDER BY " +
            "                    t.school_entry_date  DESC NULLS LAST, " +
            "                    t.education          ASC NULLS LAST,  " +
            "                    t.create_time        DESC NULLS LAST, " +
            "                    t.id DESC " +
            "            ) AS rn " +
            "        FROM hr_emp_education t " +
            "        WHERE t.user_code IN " +
            "        <foreach collection=\"userCodes\" item=\"uc\" open=\"(\" separator=\",\" close=\")\"> " +
            "            #{uc}                     " +
            "        </foreach>                    " +
            "        AND t.is_full_time = '00020' " +
            "    ) x " +
            "    WHERE x.rn = 1" +
            "</script>"
    )
    List<HrEmpEducation> selectLatestPartTimeByUserCodes(List<String> userCodes);

    @Select("<script> " +
            "SELECT * " +
            "    FROM ( " +
            "        SELECT " +
            "            t.*, " +
            "            ROW_NUMBER() OVER ( " +
            "                PARTITION BY t.user_code " +
            "                ORDER BY " +
            "                    t.school_entry_date  DESC NULLS LAST, " +
            "                    t.education          ASC NULLS LAST,  " +
            "                    t.create_time        DESC NULLS LAST, " +
            "                    t.id DESC " +
            "            ) AS rn " +
            "        FROM hr_emp_education t " +
            "        WHERE t.user_code IN " +
            "        <foreach collection=\"userCodes\" item=\"uc\" open=\"(\" separator=\",\" close=\")\"> " +
            "            #{uc}                     " +
            "        </foreach>                    " +
            "    ) x " +
            "    WHERE x.rn = 1" +
            "</script>"
    )
    List<HrEmpEducation> selectLatestByUserCodes(List<String> userCodes);
}