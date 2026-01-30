package com.crrc.mapper;

import cn.hutool.core.lang.Opt;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crrc.entity.HrEmpSkillLevel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 职业技能等级 Mapper 接口
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Mapper
public interface HrEmpSkillLevelMapper extends BaseMapper<HrEmpSkillLevel> {

    @Select("<script> " +
            "SELECT * " +
            "    FROM ( " +
            "        SELECT " +
            "            t.*, " +
            "            ROW_NUMBER() OVER ( " +
            "                PARTITION BY t.user_code " +
            "                ORDER BY " +
            "                    t.is_appointed DESC NULLS LAST, " +
            "                    t.is_highest   DESC NULLS LAST, " +
            "                    t.create_time  DESC NULLS LAST, " +
            "                    t.id DESC " +
            "            ) AS rn " +
            "        FROM hr_emp_skill_level t " +
            "        WHERE t.user_code IN " +
            "        <foreach collection=\"userCodes\" item=\"uc\" open=\"(\" separator=\",\" close=\")\"> " +
            "            #{uc} " +
            "        </foreach> " +
            "    ) x " +
            "    WHERE x.rn = 1" +
            "</script>"
    )
    List<HrEmpSkillLevel> selectLatestByUserCodes(List<String> userCodes);
}