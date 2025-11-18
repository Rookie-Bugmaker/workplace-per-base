package com.crrc.pojo.dto;

import lombok.Data;

@Data
public class ResponseData<T> {
    T data;
    String msg;
    String code;
}
