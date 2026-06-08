package com.crrc.utils;

import com.crrc.annotation.WordTemplateRequire;
import com.crrc.entity.ByteArrayMultipartFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

@Slf4j
public class WordTemplateProcessor {



    /**
     * ------入口函数---------
     * <p>
     * 处理Word模板并返回处理后的字节数组
     *
     * @param is           上传的Word模板的流
     * @param data 需要替换的数据
     * @return 处理后的Word文件字节数组
     * @throws IOException 文件读写异常
     */
    public static <T> byte[] processTemplate(InputStream is, T data) throws IOException {
        Map<String, String> replacements = extractRequireData(data);

        try (XWPFDocument doc = new XWPFDocument(is);
            ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            // 替换段落中的文本

            replacePlaceholders(doc, replacements);

            doc.write(bos);

            //向表格填充内容
            //将替换完内容的文档转换成MultipartFile
            return bos.toByteArray();
        }
    }


    /**
     * 将字节流转换为ByteArrayMultipartFile
     * @param bytes 需要转换的字节数组
     * @return ByteArrayMultipartFile
     */
    public static MultipartFile buildMultipartFile(byte[] bytes) {
        return new ByteArrayMultipartFile(
                bytes,
                "processed_document.docx",
                "processed_document.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );
    }

    /**
     * 替换段落中的文本内容
     *
     * @param doc 整个docx
     * @param replacements 替换内容键值对
     */
    public static void replacePlaceholders(XWPFDocument doc, Map<String, String> replacements) {
        // 正文（段落 + 表格）
        replaceInBody(doc, replacements);

        // ✅最佳实践：模板里占位符经常也会出现在页眉/页脚
        for (XWPFHeader header : doc.getHeaderList()) {
            replaceInBody(header, replacements);
        }
        for (XWPFFooter footer : doc.getFooterList()) {
            replaceInBody(footer, replacements);
        }
    }

    private static void replaceInBody(IBody body, Map<String, String> replacements) {
        for (IBodyElement element : body.getBodyElements()) {
            if (element instanceof XWPFParagraph) {
                replaceTextPreserveRuns((XWPFParagraph) element, replacements);
            } else if (element instanceof XWPFTable) {
                replaceInTable((XWPFTable) element, replacements);
            }
        }
    }

    private static void replaceInTable(XWPFTable table, Map<String, String> replacements) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                // 单元格内段落
                for (XWPFParagraph p : cell.getParagraphs()) {
                    replaceTextPreserveRuns(p, replacements);
                }
                // 单元格内嵌套表格（必须递归，否则会漏）
                for (XWPFTable nested : cell.getTables()) {
                    replaceInTable(nested, replacements);
                }
            }
        }
    }

    private static void replaceTextPreserveRuns(XWPFParagraph paragraph, Map<String, String> replacements) {
        if (paragraph == null || paragraph.getRuns() == null || paragraph.getRuns().isEmpty()) {
            return;
        }

        // 1) key 最佳实践：按长度倒序，避免 ${a} 抢先替换影响 ${abc}
        List<Map.Entry<String, String>> entries = new ArrayList<>(replacements.entrySet());
        entries.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));

        List<XWPFRun> runs = paragraph.getRuns();

        // 2) 先做“run 内替换”（不跨 run 的情况下，格式完全不变）
        boolean changed = false;
        for (XWPFRun run : runs) {
            String runText = getRunText(run);
            if (runText == null || runText.isEmpty()) continue;

            String newText = runText;
            for (Map.Entry<String, String> e : entries) {
                if (newText.contains(e.getKey())) {
                    newText = newText.replace(e.getKey(), e.getValue());
                }
            }

            if (!newText.equals(runText)) {
                setRunText(run, newText);
                changed = true;
            }
        }
        if (changed) {
            return; // run 内已完成替换，格式最优
        }

        // 3) 如果 run 内没变，但段落整体可能存在“占位符跨 run”的情况：拼全文再替换
        String originalFull = buildParagraphText(runs);
        if (originalFull.isEmpty()) return;

        String replacedFull = originalFull;
        for (Map.Entry<String, String> e : entries) {
            if (replacedFull.contains(e.getKey())) {
                replacedFull = replacedFull.replace(e.getKey(), e.getValue());
            }
        }
        if (replacedFull.equals(originalFull)) return;

        // 4) 回填：按原 run 的“文本长度”把 replacedFull 切回去，最大程度保留 run 样式结构
        int[] lens = new int[runs.size()];
        for (int i = 0; i < runs.size(); i++) {
            String t = getRunText(runs.get(i));
            lens[i] = (t == null) ? 0 : t.length();
        }

        int pos = 0;
        for (int i = 0; i < runs.size(); i++) {
            XWPFRun run = runs.get(i);
            int remain = replacedFull.length() - pos;

            // 非最后一个 run：尽量按原长度回填；最后一个 run：吃掉所有剩余
            int take = (i == runs.size() - 1) ? remain : Math.min(lens[i], Math.max(remain, 0));
            String part = (take > 0) ? replacedFull.substring(pos, pos + take) : "";
            setRunText(run, part);
            pos += Math.max(take, 0);
            if (pos >= replacedFull.length()) {
                // 后续 run 清空（避免残留）
                for (int j = i + 1; j < runs.size(); j++) {
                    setRunText(runs.get(j), "");
                }
                break;
            }
        }

        // 5) 如果替换后文本更长（比原 run 总长度还长），追加一个 run，复制最后一个 run 样式
        if (pos < replacedFull.length()) {
            XWPFRun last = runs.get(runs.size() - 1);
            XWPFRun extra = paragraph.createRun();
            copyRunStyle(last, extra);
            setRunText(extra, replacedFull.substring(pos));
        }
    }

    private static String getRunText(XWPFRun run) {
        // run.getText(0) 在 run 有多个 <w:t> 时可能不完整，这里做稳健拼接
        if (run == null || run.getCTR() == null) return null;
        StringBuilder sb = new StringBuilder();
        List<CTText> tList = run.getCTR().getTList();
        for (CTText t : tList) {
            sb.append(t.getStringValue());
        }
        return sb.toString();
    }

    private static void setRunText(XWPFRun run, String text) {
        if (run == null || run.getCTR() == null) return;

        // 清掉所有 <w:t>，但保留 run 的样式、br、tab、drawing 等其它结构
        int tSize = run.getCTR().sizeOfTArray();
        for (int i = tSize - 1; i >= 0; i--) {
            run.getCTR().removeT(i);
        }

        if (text == null) text = "";
        // 用 run.setText 让 POI 正确处理 xml:space="preserve"
        run.setText(text, 0);
    }

    private static String buildParagraphText(List<XWPFRun> runs) {
        StringBuilder sb = new StringBuilder();
        for (XWPFRun r : runs) {
            String t = getRunText(r);
            if (t != null) sb.append(t);
        }
        return sb.toString();
    }

    private static void copyRunStyle(XWPFRun src, XWPFRun dest) {
        if (src == null || dest == null) return;
        if (src.getCTR() != null && src.getCTR().isSetRPr()) {
            CTRPr copy = (CTRPr) src.getCTR().getRPr().copy();
            dest.getCTR().addNewRPr().set(copy);
        }
    }

    /**
     * 按列向表格插入
     * @param docxBytes 需要插入的docx的byte[]文件
     * @param dataList 需要插入的数据列表
     * @param tableNum 需要插入的表格在docx的排序，从0开始
     * @param headerRowNumber 需要插入的行在该表格的排序，从0开始
     * @return 插入完成后的byte[]数组
     */
    public static <T> byte[] replaceTable(byte[] docxBytes, List<T> dataList, int tableNum, int headerRowNumber) throws IOException {
        if (docxBytes == null || docxBytes.length == 0) {
            throw new IllegalArgumentException("docxBytes 不能为空");
        }

        try (ByteArrayInputStream in = new ByteArrayInputStream(docxBytes);
            XWPFDocument document = new XWPFDocument(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFTable table = getTable(document, tableNum);
            XWPFTableRow templateRow = getRow(table, headerRowNumber);
            List<Map<String, String>> rowDataList = extractRequireData(dataList);

            // 固定从表头下一行开始插入
            int insertPos = headerRowNumber + 1;

            for (int i = 0; i < rowDataList.size(); i++) {
                Map<String, String> rowData = rowDataList.get(i);
                String[] cellValues = toCellValues(rowData, templateRow.getTableCells().size());
                // 每插入一行，位置后移，保证插入顺序与 dataList 一致
                insertRowByTemplate(table, templateRow, insertPos + i, cellValues);
            }

            document.write(out);
            return out.toByteArray();
        }
    }


    /**
     * 获取指定表格，带边界校验
     */
    private static XWPFTable getTable(XWPFDocument document, int tableNum) {

        List<XWPFTable> tables = document.getTables();
        if (tableNum < 0 || tableNum >= tables.size()) {
            throw new IllegalArgumentException("tableNum 超出范围，当前文档表格数量: " + tables.size());
        }
        return tables.get(tableNum);
    }



    /**
     * 获取指定行，带边界校验
     */
    private static XWPFTableRow getRow(XWPFTable table, int rowNum) {
        int rowCount = table.getNumberOfRows();
        if (rowNum < 0 || rowNum >= rowCount) {
            throw new IllegalArgumentException("headerRowNumber 超出范围，当前表格行数: " + rowCount);
        }
        return table.getRow(rowNum);
    }



    /**
     * 将行数据转为单元格数组。
     * 若数据列数小于模板列数，则补空；若大于模板列数，则截断。
     */
    private static String[] toCellValues(Map<String, String> rowData, int templateCellSize) {

        List<String> values = new ArrayList<>(rowData.values());

        if (values.size() < templateCellSize) {
            while (values.size() < templateCellSize) {
                values.add("");
            }
        } else if (values.size() > templateCellSize) {
            values = values.subList(0, templateCellSize);
        }

        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) == null) {
                values.set(i, "");
            }
        }

        return values.toArray(new String[0]);
    }



    /**
     * 在指定位置插入一行，并完整复制模板行样式
     */
    private static void insertRowByTemplate(XWPFTable table, XWPFTableRow templateRow, int insertPos, String[] cellValues) {

        XWPFTableRow newRow = table.insertNewTableRow(insertPos);

        if (newRow == null) {
            throw new IllegalStateException("插入表格行失败，insertPos=" + insertPos);
        }

        copyRowStyle(templateRow, newRow);

        List<XWPFTableCell> templateCells = templateRow.getTableCells();

        // 始终以模板行单元格数为准创建新行结构
        for (int i = 0; i < templateCells.size(); i++) {
            XWPFTableCell templateCell = templateCells.get(i);
            XWPFTableCell newCell = newRow.createCell();

            copyCellStyle(templateCell, newCell);

            String text = i < cellValues.length ? cellValues[i] : "";
            writeCellTextLikeTemplate(templateCell, newCell, text);
        }
    }



    /**
     * 复制行样式
     */
    private static void copyRowStyle(XWPFTableRow sourceRow, XWPFTableRow targetRow) {

        if (sourceRow.getCtRow().getTrPr() != null) {
            targetRow.getCtRow().setTrPr((CTTrPr) sourceRow.getCtRow().getTrPr().copy());
        }
    }



    /**
     * 复制单元格样式
     */

    private static void copyCellStyle(XWPFTableCell sourceCell, XWPFTableCell targetCell) {
        if (sourceCell.getCTTc().getTcPr() != null) {
            targetCell.getCTTc().setTcPr((CTTcPr) sourceCell.getCTTc().getTcPr().copy());
        }

        // 清空目标单元格默认段落
        for (int i = targetCell.getParagraphs().size() - 1; i >= 0; i--) {
            targetCell.removeParagraph(i);
        }
    }



    /**
     * 按模板单元格的段落和字体样式写入文本
     */
    private static void writeCellTextLikeTemplate(XWPFTableCell templateCell, XWPFTableCell targetCell, String text) {

        XWPFParagraph templateParagraph = templateCell.getParagraphs().isEmpty() ? null : templateCell.getParagraphs().get(0);
        XWPFParagraph targetParagraph = targetCell.addParagraph();

        // 复制段落样式
        if (templateParagraph != null && templateParagraph.getCTP().getPPr() != null) {
            targetParagraph.getCTP().setPPr((CTPPr) templateParagraph.getCTP().getPPr().copy());
        }

        XWPFRun targetRun = targetParagraph.createRun();

        // 复制 run 样式
        if (templateParagraph != null && !templateParagraph.getRuns().isEmpty()) {
            XWPFRun templateRun = templateParagraph.getRuns().get(0);
            if (templateRun.getCTR().getRPr() != null) {
                targetRun.getCTR().setRPr((CTRPr) templateRun.getCTR().getRPr().copy());
            }
        }

        targetRun.setText(text == null ? "" : text);

    }

    public static <T> Map<String, String> extractRequireData(T dataList) {
        return extractRequireData(Collections.singletonList(dataList)).get(0);
    }

    /**
     * 解析List数据，提取出带有TableRequire的列，并拿到其placeholder
     */
    public static <T> List<Map<String, String>> extractRequireData(List<T> dataList) {
        List<Map<String, String>> resultList = new ArrayList<>();
        if(dataList == null || dataList.isEmpty()) {
            return resultList;
        }

        Class<?> clazz = dataList.get(0).getClass();

        // 在循环外拿到标记了@TemplateTableRequire的列
        List<Field> requiredFields = new ArrayList<>();

        for(Field field : clazz.getDeclaredFields()) {
            if(field.isAnnotationPresent(WordTemplateRequire.class)) {

                field.setAccessible(true);
                requiredFields.add(field);
            }
        }

        requiredFields.sort(Comparator.comparingInt(f -> f.getAnnotation(WordTemplateRequire.class).index()));

        // 遍历List，获得需要的字段值
        for(T item : dataList) {
            // 需要保证顺序
            Map<String, String> rowData = new LinkedHashMap<>();

            for(Field field : requiredFields) {
                try {
                    WordTemplateRequire annotation = field.getAnnotation(WordTemplateRequire.class);
                    // 获取占位符
                    String placeHolder = annotation.value().isEmpty() ? String.format("${%s}", field.getName()) : annotation.value();

                    Object value = field.get(item);

                    rowData.put(placeHolder, value == null ? "" : String.valueOf(value));
                } catch (IllegalAccessException e) {
                    log.error("extractRequireData出现错误, {}", e.getMessage(), e);
                }
            }

            resultList.add(rowData);
        }

        return resultList;
    }

    /**
     * 构造带文件名的 MultipartFile（现有 buildMultipartFile 文件名写死，这里支持自定义）
     */
    public static MultipartFile buildMultipartFile(byte[] bytes, String filename) {
        return new ByteArrayMultipartFile(
                bytes,
                filename,
                filename,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );
    }

    /**
     * 多行文本填充：把“占位符所在段落”的内容整体替换为按 \n 换行的多行文本，保留首个 run 的样式，
     * 并清除该段落的缩进，使所有行（含自动折行）左对齐对齐到单元格左边。
     * 适用于模板中单元格内仅放一个占位符的场景（如 ${resume}、${rewards}）。
     * 注意：需在标量 replacePlaceholders 之前调用，此时占位符仍存在。
     */
    public static void fillMultiline(XWPFDocument doc, String placeholder, String value) {
        String safe = (value == null) ? "" : value;
        for (XWPFParagraph p : collectParagraphs(doc)) {
            if (!buildParagraphText(p.getRuns()).contains(placeholder)) {
                continue;
            }
            resetParagraphIndent(p);
            XWPFRun first = prepareSingleRun(p);
            String[] lines = safe.split("\n", -1);
            first.setText(lines[0], 0);
            for (int i = 1; i < lines.length; i++) {
                first.addBreak();
                first.setText(lines[i]);
            }
        }
    }

    /**
     * 简历多行填充并自适应：内容越多，字号与行距同步减小，尽量在单元格内展示全部任职信息。
     * 按“每行约 42 个汉字”折算自动换行估算显示行数，分档设置字号(半磅)与行距(AUTO,240=单倍)。
     * 注意：需在标量 replacePlaceholders 之前调用。
     */
    public static void fillMultilineAutoFit(XWPFDocument doc, String placeholder, String value) {
        String safe = (value == null) ? "" : value;
        for (XWPFParagraph p : collectParagraphs(doc)) {
            if (!buildParagraphText(p.getRuns()).contains(placeholder)) {
                continue;
            }
            resetParagraphIndent(p);
            int[] tier = pickResumeTier(safe);   // {字号半磅, 行距(240ths)}

            // 行距：AUTO 规则 + 去掉段前段后
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr ppr =
                    p.getCTP().isSetPPr() ? p.getCTP().getPPr() : p.getCTP().addNewPPr();
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing sp =
                    ppr.isSetSpacing() ? ppr.getSpacing() : ppr.addNewSpacing();
            sp.setLineRule(org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule.AUTO);
            sp.setLine(java.math.BigInteger.valueOf(tier[1]));
            sp.setBefore(java.math.BigInteger.ZERO);
            sp.setAfter(java.math.BigInteger.ZERO);

            XWPFRun first = prepareSingleRun(p);
            String[] lines = safe.split("\n", -1);
            first.setText(lines[0], 0);
            for (int i = 1; i < lines.length; i++) {
                first.addBreak();
                first.setText(lines[i]);
            }

            // 字号（半磅）：sz 与 szCs 同步（CTRPr 中 sz/szCs 为单元素，使用 isSetSz/getSz/addNewSz）
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr rpr =
                    first.getCTR().isSetRPr() ? first.getCTR().getRPr() : first.getCTR().addNewRPr();
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHpsMeasure sz =
                    rpr.isSetSz() ? rpr.getSz() : rpr.addNewSz();
            sz.setVal(java.math.BigInteger.valueOf(tier[0]));
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHpsMeasure szCs =
                    rpr.isSetSzCs() ? rpr.getSzCs() : rpr.addNewSzCs();
            szCs.setVal(java.math.BigInteger.valueOf(tier[0]));
        }
    }

    /**
     * 选档：在“能一页放下”的前提下尽量保留较大字号与行距（允许自动换行）。
     * 从大到小试每一档，估算显示行数(按全角宽度折算，ASCII按半宽)，命中可容纳的最大字号即返回。
     * 经实测：五号(10.5磅,单倍)在简历单元格约可容纳 22~23 行，CELL_HEIGHT_PT 取保守值留 1 行余量。
     * 返回 {字号(半磅), 行距(240=单倍)}。
     */
    private static int[] pickResumeTier(String value) {
        String[] lines = (value == null || value.isEmpty()) ? new String[0] : value.split("\n", -1);
        // 从大到小：字号(半磅) + 行距(240ths)。前两档保持单倍行距，越往后越紧。
        int[][] tiers = {
                {21, 240}, {20, 240}, {19, 232}, {18, 228},
                {16, 222}, {15, 216}, {14, 210}, {13, 204}, {12, 200}
        };
        final double BASE_HALF = 21.0;        // 五号 = 10.5磅
        final double BASE_CHARS = 42.0;       // 五号下每行约 42 个全角
        final double CELL_HEIGHT_PT = 270.0;  // 简历单元格一页内可用高度(磅)，保守留余量
        for (int[] t : tiers) {
            double fontPt = t[0] / 2.0;
            double charsPerLine = Math.max(10.0, BASE_CHARS * BASE_HALF / t[0]);
            int visual = 0;
            for (String raw : lines) {
                String ln = raw.trim();
                double w = 0;
                for (int i = 0; i < ln.length(); i++) {
                    w += (ln.charAt(i) <= 0x7F) ? 0.5 : 1.0;   // ASCII 半宽
                }
                visual += Math.max(1, (int) Math.ceil(w / charsPerLine));
            }
            double lineHeightPt = fontPt * 1.15 * (t[1] / 240.0);
            int capacity = (int) Math.floor(CELL_HEIGHT_PT / lineHeightPt);
            if (visual <= capacity) {
                return t;
            }
        }
        return tiers[tiers.length - 1];   // 极端情况用最小档
    }

    /**
     * 照片填充：把“占位符所在单元格”清空并插入图片；image 为空则仅清空占位符。
     * 默认按身份证照尺寸（约 2.6cm×3.6cm）插入。
     */
    public static void fillPhoto(XWPFDocument doc, String placeholder, byte[] image) {
        for (XWPFParagraph p : collectParagraphs(doc)) {
            if (!buildParagraphText(p.getRuns()).contains(placeholder)) {
                continue;
            }
            XWPFRun run = prepareSingleRun(p);
            if (image == null || image.length == 0) {
                return;
            }
            try (ByteArrayInputStream in = new ByteArrayInputStream(image)) {
                int type = ((image[0] & 0xFF) == 0x89)
                        ? XWPFDocument.PICTURE_TYPE_PNG : XWPFDocument.PICTURE_TYPE_JPEG;
                int width = 936000;    // ≈2.6cm（1cm = 360000 EMU）
                int height = 1296000;  // ≈3.6cm
                run.addPicture(in, type, "photo", width, height);
            } catch (Exception e) {
                log.error("插入照片失败", e);
            }
            return;
        }
    }

    /** 清除段落缩进(<w:ind>)，使内容左对齐、不再右移 */
    private static void resetParagraphIndent(XWPFParagraph p) {
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr ppr = p.getCTP().getPPr();
        if (ppr != null && ppr.isSetInd()) {
            ppr.unsetInd();
        }
    }

    /** 清空段落中除首个 run 外的所有 run，并清空首个 run 的文本/换行，返回首个 run */
    private static XWPFRun prepareSingleRun(XWPFParagraph p) {
        List<XWPFRun> runs = p.getRuns();
        for (int i = (runs == null ? 0 : runs.size()) - 1; i >= 1; i--) {
            p.removeRun(i);
        }
        XWPFRun run = (runs != null && !runs.isEmpty()) ? runs.get(0) : p.createRun();
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR ctr = run.getCTR();
        if (ctr != null) {
            for (int i = ctr.sizeOfTArray() - 1; i >= 0; i--) {
                ctr.removeT(i);
            }
            for (int i = ctr.sizeOfBrArray() - 1; i >= 0; i--) {
                ctr.removeBr(i);
            }
        }
        return run;
    }

    /** 递归收集 body（含表格、嵌套表格）中的所有段落 */
    private static List<XWPFParagraph> collectParagraphs(IBody body) {
        List<XWPFParagraph> out = new ArrayList<>();
        for (IBodyElement el : body.getBodyElements()) {
            if (el instanceof XWPFParagraph) {
                out.add((XWPFParagraph) el);
            } else if (el instanceof XWPFTable) {
                for (XWPFTableRow row : ((XWPFTable) el).getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        out.addAll(collectParagraphs(cell));
                    }
                }
            }
        }
        return out;
    }
}