package com.crrc.pojo.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * 通用更新DTO
 * @param <T> 实体类型
 */
@Document
@Data
public class UpdateListDTO<T> {
    @Id
    private String id;
    private String userCode;
    private List<T> list;
}