package com.crrc.service;

import com.crrc.pojo.dto.HrEmpBatchExportRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 人员信息集批量导出服务
 */
public interface HrEmpBatchExportService {

    void batchExport(HrEmpBatchExportRequest request, HttpServletResponse response) throws IOException;
}
