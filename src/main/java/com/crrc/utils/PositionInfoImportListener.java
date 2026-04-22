package com.crrc.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.crrc.entity.PositionInfo;
import com.crrc.service.impl.PositionInfoServiceImpl;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author myy
 * @since 2026-1-12
 */
public class PositionInfoImportListener implements ReadListener<PositionInfo> {

    private static final int BATCH_SIZE = 1000;
    private final List<PositionInfo> buffer = new LinkedList<>();
    private final List<String> errors = new ArrayList<>();
    private int rowIndex = 0;
    private final PositionInfoServiceImpl positionInfoService;

    public PositionInfoImportListener(PositionInfoServiceImpl positionInfoService) {
        this.positionInfoService = positionInfoService;
    }

    @Override
    public void invoke(PositionInfo row, AnalysisContext analysisContext) {
        rowIndex++;

        if (row == null || !StringUtils.hasText(row.getCode())) {
            errors.add("第 " + rowIndex + " 行：职位编码为空");
            return;
        }

        if (!StringUtils.hasText(row.getJobGrade())) {
            errors.add("第 " + rowIndex + " 行：标准岗级为空");
            return;
        }

        if(row.getCode().startsWith("-")) {
            row.setCode(row.getCode().replaceFirst("-", ""));
        }

        buffer.add(row);

        if (buffer.size() >= BATCH_SIZE) {
            flush();
        }
    }

    private void flush() {
        if (buffer.isEmpty()) {
            return;
        }

        // 用于记录未被录入的职位编码
        List<String> notExistCodes = new ArrayList<>();

        // 用于保存最终需要更新的职位信息
        List<PositionInfo> updatedPositions = new ArrayList<>();

        // 遍历 buffer 中的职位信息，逐个查询是否存在
        for (PositionInfo positionInfo : buffer) {
            String code = positionInfo.getCode();

            List<String> codes = new ArrayList<>();
            codes.add(code);

            // 单条查询
            List<PositionInfo> existingList = positionInfoService.selectByCodes(codes);
            if (existingList.isEmpty()) {
                notExistCodes.add(code);
                continue;
            }

            // 获取查询到的完整数据
            PositionInfo existing = existingList.get(0);

            // 设置标准岗级（使用 buffer 中的 jobGrade）
            existing.setJobGrade(positionInfo.getJobGrade());

            // 将更新后的数据加入待保存列表
            updatedPositions.add(existing);
        }

        // 如果存在未被录入的职位编码，抛出异常
        if (!notExistCodes.isEmpty()) {
            throw new IllegalArgumentException("只允许录入已存在职位的标准岗级，职位编码" + String.join("、", notExistCodes) + "未被录入过，请检查导入Excel，删掉未录入的职位后进行重试");
        }

        // 执行批量保存或更新操作（使用查询后的完整数据）
        positionInfoService.saveOrUpdateBatch(updatedPositions);

        // 清空缓冲区
        buffer.clear();
    }
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        flush();
        if (!errors.isEmpty()) {
            // 这里可以抛出自定义异常，或返回给前端
            throw new IllegalArgumentException("导入完成，但存在错误：\n" + String.join("\n", errors));
        }
    }
}
