package com.crrc.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class QueryDTO {
    String sql;
    List<String> params;
}
