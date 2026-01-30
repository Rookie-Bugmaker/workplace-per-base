package com.crrc.mapper;

import cn.hutool.core.lang.Opt;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crrc.entity.HrEmpProfessionalQualification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 员工专业职务任职资格表 Mapper 接口
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Mapper
public interface HrEmpProfessionalQualificationMapper extends BaseMapper<HrEmpProfessionalQualification> {

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
            "        FROM hr_emp_pro_qualification t " +
            "        WHERE t.user_code IN " +
            "        <foreach collection=\"userCodes\" item=\"uc\" open=\"(\" separator=\",\" close=\")\"> " +
            "            #{uc} " +
            "        </foreach> " +
            "    ) x " +
            "    WHERE x.rn = 1" +
            "</script>"
    )
    List<HrEmpProfessionalQualification> selectLatestByUserCodes(List<String> userCodes);
}