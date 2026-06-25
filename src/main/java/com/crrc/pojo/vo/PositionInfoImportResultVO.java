package com.crrc.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 职位完整导入结果。
 */
@Data
@Schema(description = "职位完整导入结果")
public class PositionInfoImportResultVO {
    @Schema(description = "读取总行数")
    private int totalRows;

    @Schema(description = "新增数量")
    private int insertCount;

    @Schema(description = "更新数量")
    private int updateCount;

    @Schema(description = "错误信息")
    private List<String> errors = new ArrayList<>();

    public boolean hasError() {
        return errors != null && !errors.isEmpty();
    }
}
