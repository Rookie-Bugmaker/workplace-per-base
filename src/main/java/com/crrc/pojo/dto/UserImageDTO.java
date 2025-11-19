package com.crrc.pojo.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserImageDTO {
    /**
     * 图片的路径
     */
    private String imageUri;
    /**
     * 人员姓名
     */
    private String name;
    /**
     * 工号
     */
    private String userCode;
    /**
     * 图片
     */
    private MultipartFile imageFile;
}
