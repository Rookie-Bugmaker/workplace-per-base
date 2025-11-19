package com.crrc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class PictureError {
    @TableId(type = IdType.AUTO)
    Long id;
    String userCode;
}
