package com.crrc.controller;

import com.crrc.pojo.ResponseResult;
import com.crrc.service.impl.CadreAppointmentFormGenerateServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 干部任免审批表
 */
@RestController
@RequestMapping("/cadreAppointmentForm")
@Tag(name = "干部任免审批表")
public class CadreAppointmentFormController {

    @Autowired
    private CadreAppointmentFormGenerateServiceImpl cadreAppointmentFormGenerateService;

    /**
     * 根据档案号(userCode)生成干部任免审批表，支持一个或多个。
     * 返回每个档案号的生成结果：{userCode, fileId} 或 {userCode, error}。
     */
    @PostMapping("/generate")
    @Operation(summary = "根据档案号生成干部任免审批表（支持批量）")
    public ResponseResult<List<Map<String, String>>> generate(@RequestBody List<String> userCodes) {
        if (userCodes == null || userCodes.isEmpty()) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "档案号不能为空");
        }
        List<Map<String, String>> data = cadreAppointmentFormGenerateService.generateForUserCodes(userCodes);
        return new ResponseResult<>(HttpStatus.OK.value(), "生成成功", data);
    }

    /**
     * 根据档案号批量生成干部任免审批表，并打包为 zip 直接下载（多选时无需逐个用 fileId 下载）。
     */
    @PostMapping("/generateZip")
    @Operation(summary = "根据档案号批量生成干部任免审批表并打包zip直接下载")
    public ResponseEntity<byte[]> generateZip(@RequestBody List<String> userCodes) throws IOException {
        if (userCodes == null || userCodes.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        byte[] zip = cadreAppointmentFormGenerateService.generateZipForUserCodes(userCodes);
        String fileName = "干部任免审批表_" + LocalDate.now() + ".zip";
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded);
        return new ResponseEntity<>(zip, headers, HttpStatus.OK);
    }
}
