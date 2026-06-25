package com.crrc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crrc.entity.PositionInfo;
import com.crrc.pojo.vo.PositionIncumbentDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author yukun
 * @since 2025-10-17 17:36:20
 */
@Mapper
public interface PositionInfoMapper extends BaseMapper<PositionInfo> {

    /**
     * 按职位编码批量查询当前在岗人员。
     * 统计口径：hr_emp_appointment.position = position_info.code，任职开始日期为空或不晚于当前日期，任职结束日期为空或不早于当前日期；
     * 若能关联到 hr_emp_basic，则仅保留未离职或未维护离职标记的人员。
     */
    @Results(id = "PositionIncumbentDTOMap", value = {
            @Result(column = "position_code", property = "positionCode"),
            @Result(column = "user_code", property = "userCode"),
            @Result(column = "user_name", property = "userName")
    })
    @Select({
            "<script>",
            "SELECT DISTINCT a.position AS position_code,",
            "       a.user_code AS user_code,",
            "       COALESCE(b.name, a.user_code) AS user_name",
            "  FROM hr_emp_appointment a",
            "  LEFT JOIN hr_emp_basic b",
            "    ON b.user_code = a.user_code",
            "   AND (b.deleted = 0 OR b.deleted IS NULL)",
            " WHERE (a.deleted = 0 OR a.deleted IS NULL)",
            "   AND a.position IN",
            "   <foreach collection='positionCodes' item='code' open='(' separator=',' close=')'>",
            "       #{code}",
            "   </foreach>",
            "   AND (a.start_date IS NULL OR a.start_date &lt;= CURRENT_DATE)",
            "   AND (a.end_date IS NULL OR a.end_date &gt;= CURRENT_DATE)",
            "   AND (b.user_code IS NULL OR b.is_employed IS NULL OR b.is_employed = '1')",
            " ORDER BY a.position, a.user_code",
            "</script>"
    })
    List<PositionIncumbentDTO> selectActiveIncumbentsByPositionCodes(@Param("positionCodes") Collection<String> positionCodes);
}
