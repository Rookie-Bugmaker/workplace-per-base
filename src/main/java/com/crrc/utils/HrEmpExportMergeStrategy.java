package com.crrc.utils;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 人员信息导出合并策略：对同一人员（userCode相同）的连续行，合并前两列（姓名、档案号）
 */
public class HrEmpExportMergeStrategy extends AbstractMergeStrategy {

    /**
     * 每行对应的userCode，按数据行顺序排列（不含表头行）
     */
    private final List<String> userCodes;

    /**
     * 已经合并过的起始行索引，避免重复合并
     */
    private final Set<Integer> mergedStartRows = new HashSet<>();

    public HrEmpExportMergeStrategy(List<String> userCodes) {
        this.userCodes = userCodes;
    }

    @Override
    protected void merge(Sheet sheet, Cell cell, Head head, Integer relativeRowIndex) {
        if (relativeRowIndex == null || relativeRowIndex < 0 || relativeRowIndex >= userCodes.size()) {
            return;
        }

        // 只处理第0列（姓名列）触发合并
        if (cell.getColumnIndex() != 0) {
            return;
        }

        // 如果该行已经在某组处理过，跳过
        if (mergedStartRows.contains(relativeRowIndex)) {
            return;
        }

        String currentUserCode = userCodes.get(relativeRowIndex);
        if (currentUserCode == null || currentUserCode.isEmpty()) {
            return;
        }

        // 向后查找相同userCode的连续范围
        int endDataIndex = relativeRowIndex;
        while (endDataIndex + 1 < userCodes.size() && currentUserCode.equals(userCodes.get(endDataIndex + 1))) {
            endDataIndex++;
        }

        // 如果只有一行，不需要合并
        if (endDataIndex == relativeRowIndex) {
            mergedStartRows.add(relativeRowIndex);
            return;
        }

        // 计算绝对行号（+1 因为表头占一行）
        int firstRow = relativeRowIndex + 1;
        int lastRow = endDataIndex + 1;

        // 合并第0列和第1列
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, 0, 0));
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, 1, 1));

        mergedStartRows.add(relativeRowIndex);
        mergedStartRows.addAll(IntStream.rangeClosed(relativeRowIndex, endDataIndex).boxed().collect(Collectors.toSet()));
    }
}
