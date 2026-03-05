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
    public static <T> MultipartFile processTemplate(InputStream is, Object data, List<T> dataList) throws IOException {
        Map<String, String> replacements = extractRequireData(data);

        try (XWPFDocument doc = new XWPFDocument(is);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            // 替换段落中的文本

            replacePlaceholders(doc, replacements);

            doc.write(bos);

            //向表格填充内容
            //将替换完内容的文档转换成MultipartFile
            return new ByteArrayMultipartFile(
                    replaceTable(bos.toByteArray(), dataList),
                    "processed_document.docx",
                    "processed_document.docx",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            );
        }
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
        List<org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText> tList = run.getCTR().getTList();
        for (org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText t : tList) {
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
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr copy =
                    (org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr) src.getCTR().getRPr().copy();
            dest.getCTR().addNewRPr().set(copy);
        }
    }

    public static <T> byte[] replaceTable(byte[] docxBytes, List<T> dataList) throws IOException {
        // 1. 从字节数组创建输入流
        ByteArrayInputStream in = new ByteArrayInputStream(docxBytes);

        // 2. 打开现有文档
        XWPFDocument document = new XWPFDocument(in);
        in.close(); // 及时关闭输入流

        // 3. 获取文档中的第一个表格
        XWPFTable table = document.getTables().get(0);
        // 继承表头的字体与边框
        XWPFTableRow templateRow = table.getRow(table.getNumberOfRows() - 1);

        List<Map<String, String>> addTexts = extractRequireData(dataList);

        // 4. 向表格添加内容
        for (Map<String, String> item : addTexts) {
            addRowLikeTemplate(table, templateRow, item.values().toArray(new String[0]));
        }

        // 5. 将文档写入字节数组输出流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.write(out);
        document.close();

        // 6. 获取字节数组并返回
        byte[] result = out.toByteArray();
        out.close();

        return result;
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

                    rowData.put(placeHolder, String.valueOf(value));
                } catch (IllegalAccessException e) {
                    log.error("extractRequireData出现错误, {}", e.getMessage(), e);
                }
            }

            resultList.add(rowData);
        }

        return resultList;
    }

    /**
     * 在表尾新增一行，复制 templateRow 的样式（行/单元格/段落/字体），并填充文本
     */
    private static void addRowLikeTemplate(XWPFTable table, XWPFTableRow templateRow, String[] texts) {
        // 在末尾插入新行（也可用 insertNewTableRow(pos) 指定位置）
        XWPFTableRow newRow = table.insertNewTableRow(table.getNumberOfRows());

        // 复制行属性（高度、表头重复、间距等）
        if (templateRow.getCtRow().getTrPr() != null) {
            newRow.getCtRow().setTrPr((CTTrPr) templateRow.getCtRow().getTrPr().copy());
        }

        List<XWPFTableCell> tplCells = templateRow.getTableCells();

        for (int i = 0; i < texts.length; i++) {
            XWPFTableCell tplCell = tplCells.get(i >= tplCells.size() ? tplCells.size() - 1 : i);

            // 新建单元格
            XWPFTableCell newCell = newRow.addNewTableCell();

            // 复制单元格属性（边框、填充、垂直对齐、网格/合并等）
            if (tplCell.getCTTc().getTcPr() != null) {
                newCell.getCTTc().setTcPr((CTTcPr) tplCell.getCTTc().getTcPr().copy());
            }

            // 清空默认段落
            int pc = newCell.getParagraphs().size();
            for (int p = pc - 1; p >= 0; p--) newCell.removeParagraph(p);

            // 用模板单元格的第一个段落作为样式来源
            XWPFParagraph tplP = tplCell.getParagraphs().isEmpty() ? null : tplCell.getParagraphs().get(0);
            XWPFParagraph newP = newCell.addParagraph();

            // 复制段落属性（对齐、缩进、间距、样式ID等）
            if (tplP != null && tplP.getCTP().getPPr() != null) {
                newP.getCTP().setPPr((CTPPr) tplP.getCTP().getPPr().copy());
            }

            // 准备 Run（字体、字号、加粗、颜色等），从模板段落第一个 Run 继承
            CTRPr rpr = null;
            if (tplP != null && !tplP.getRuns().isEmpty() && tplP.getRuns().get(0).getCTR().getRPr() != null) {
                rpr = (CTRPr) tplP.getRuns().get(0).getCTR().getRPr().copy();
            }

            XWPFRun newRun = newP.createRun();
            if (rpr != null) {
                newRun.getCTR().setRPr(rpr);
            }

            // 写入文本
            String text = texts[i];
            newRun.setText(text);
        }
    }
}