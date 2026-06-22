package com.crrc.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportResult {

    private int totalCount;

    private int successCount;

    private List<RowError> errors;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RowError {
        private int rowIndex;
        private String userCode;
        private String message;
    }
}
