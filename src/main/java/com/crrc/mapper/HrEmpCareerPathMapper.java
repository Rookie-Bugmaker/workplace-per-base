package com.crrc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crrc.entity.HrEmpCareerPath;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 职业发展通道 Mapper 接口
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Mapper
public interface HrEmpCareerPathMapper extends BaseMapper<HrEmpCareerPath> {

    @Select("<script> " +
            "SELECT * " +
            "    FROM ( " +
            "        SELECT " +
            "            t.*, " +
            "            ROW_NUMBER() OVER ( " +
            "                PARTITION BY t.user_code " +
            "                ORDER BY " +
            "                    t.is_expired          ASC  NULLS LAST, " +
            "                    t.create_time         DESC NULLS LAST, " +
            "                    t.qualification_date  DESC NULLS LAST, " +
            "                    t.id DESC " +
            "            ) AS rn " +
            "        FROM hr_emp_career_path t " +
            "        WHERE t.user_code IN " +
            "        <foreach collection=\"userCodes\" item=\"uc\" open=\"(\" separator=\",\" close=\")\"> " +
            "            #{uc} " +
            "        </foreach> " +
            "    ) x " +
            "    WHERE x.rn = 1" +
            "</script>"
    )
    List<HrEmpCareerPath> selectLatestByUserCodes(List<String> userCodes);
}