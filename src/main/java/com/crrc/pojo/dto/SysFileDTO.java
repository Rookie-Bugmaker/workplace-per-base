package com.crrc.pojo.dto;

import lombok.Data;

@Data
public class SysFileDTO {

    /**
     * 附件ID
     */
    private Long fileId;
    /**
     * 附件头ID
     */
    private Long fileHeaderId;
    /**
     * 附件原名称
     */
    private String oldFileName;
    /**
     * 组名称
     */
    private String groupName;
    /**
     * 文件地址
     */
    private String filePath;
    /**
     * 访问地址
     */
    private String url;
}
