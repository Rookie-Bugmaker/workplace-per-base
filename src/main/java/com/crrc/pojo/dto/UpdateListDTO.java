package com.crrc.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * 通用更新DTO
 * @param <T> 实体类型
 */
@Data
public class UpdateListDTO<T> {
    private String userCode;
    private List<T> list;
}