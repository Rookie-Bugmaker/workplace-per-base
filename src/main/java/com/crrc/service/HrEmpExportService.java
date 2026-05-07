package com.crrc.service;

import com.crrc.pojo.dto.HrEmpBatchExportRequest;

/**
 * 人事信息导出服务（同步导出逻辑抽取）
 */
public interface HrEmpExportService {

    /**
     * 导出人事信息到字节数组
     *
     * @param request 导出请求参数
     * @param progressUpdater 进度更新器（可为null）
     * @return Excel字节数组
     */
    byte[] exportHrEmpInfo(HrEmpBatchExportRequest request, ExportProgressUpdater progressUpdater);
}
