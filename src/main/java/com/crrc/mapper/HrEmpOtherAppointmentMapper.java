package com.crrc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crrc.entity.HrEmpOtherAppointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 其他任职信息 Mapper 接口
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Mapper
public interface HrEmpOtherAppointmentMapper extends BaseMapper<HrEmpOtherAppointment> {
    @Select("<script> " +
            "SELECT * " +
            "    FROM ( " +
            "        SELECT " +
            "            t.*, " +
            "            ROW_NUMBER() OVER ( " +
            "                PARTITION BY t.user_code " +
            "                ORDER BY " +
            "                    t.create_time DESC NULLS LAST, " +
            "                    t.start_date  DESC NULLS LAST, " +
            "                    t.end_date    DESC NULLS LAST, " +
            "                    t.id DESC " +
            "            ) AS rn " +
            "        FROM hr_emp_other_appointment t " +
            "        WHERE t.user_code IN " +
            "        <foreach collection=\"userCodes\" item=\"uc\" open=\"(\" separator=\",\" close=\")\"> " +
            "            #{uc} " +
            "        </foreach> " +
            "    ) x " +
            "    WHERE x.rn = 1" +
            "</script>"
    )
    List<HrEmpOtherAppointment> selectLatestByUserCodes(List<String> userCodes);
}