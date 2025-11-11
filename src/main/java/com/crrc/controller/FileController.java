package com.crrc.controller;

import com.crrc.annotation.AuditLog;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.FileDownloadDTO;
import com.crrc.service.impl.FileServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * @author why
 */
@RestController
@RequestMapping("/file")
@RefreshScope
@Tag(name = "附件管理", description = "附件管理")
public class FileController {
    @Resource
    public FileServiceImpl fileService;

    @PostMapping("/upload")
    @AuditLog(value = "上传文件", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "上传文件")
    public ResponseResult<String> uploadTemplate(@RequestParam MultipartFile file) {
        String templateId;
        try {
            templateId = fileService.uploadTemplate(file);
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "文件上传失败，错误信息：" + e.getMessage());
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "导入模板信息报表成功",templateId);
    }

    @PostMapping("/preview")
    @AuditLog(value = "预览文件", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "预览文件")
    public ResponseResult<String> preview(@RequestBody FileDownloadDTO downloadDTO) {
        String result = fileService.preview(downloadDTO.getFileId());
        if (StringUtils.isBlank(result)){
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "获取失败");
        }else {
            return new ResponseResult<>(HttpStatus.OK.value(), "获取成功",result);
        }
    }

    @PostMapping("/download")
    @AuditLog(value = "下载单个文件", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "下载单个文件")
    public void download(@RequestBody FileDownloadDTO downloadDTO,HttpServletResponse res) {
        if (StringUtils.isNotBlank(downloadDTO.getFileId())){
            fileService.download(downloadDTO.getFileId(),res);
        }
    }

    @PostMapping("/downloadBatch")
    @AuditLog(value = "批量下载文件", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "批量下载文件")
    public void downloadBatch(@RequestBody FileDownloadDTO downloadDTO,HttpServletResponse res) {
        if (downloadDTO.getIdList() != null && !downloadDTO.getIdList().isEmpty()) {
            fileService.downLoadBatch(downloadDTO.getIdList(),res);
        }
    }
}
