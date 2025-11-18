package com.crrc.pojo.dto;

import lombok.Data;

@Data
public class UserImageDTO {
    /**
     * 图片的路径
     */
    String imageUri;
    /**
     * 人员姓名
     */
    String name;
    /**
     * 工号
     */
    String uuid;
}
