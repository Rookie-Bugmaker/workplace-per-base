package com.crrc.utils;

import com.crrc.client.FileClient;
import com.crrc.common.ResultData;
import com.crrc.pojo.dto.SysFileDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class FileUtils {

    private static FileClient fileClient;

    @Autowired
    public void setFileClient(FileClient fileClient) {
        FileUtils.fileClient = fileClient;
    }

    /**
     * 通过实体类中的"String fileHeaderId"字段，为传入实体类中的"List<SysFileDTO> files"赋值
     * 支持处理单个对象或对象列表
     * 请尽量调用service中的方法，不要在controller中调用
     */
    public static <T> void resolveFilesByHeaderId(T dto) throws Exception {
        if (dto instanceof List) {
            // 处理列表情况
            for (Object item : (List<?>) dto) {
                processSingleItem(item);
            }
        } else {
            // 处理单个对象情况
            processSingleItem(dto);
        }
    }

    /**
     * 处理单个对象的附件解析
     */
    private static void processSingleItem(Object item) throws Exception {
        try {
            // 使用反射检查对象是否包含所需字段
            Class<?> clazz = item.getClass();
            java.lang.reflect.Field fileHeaderIdField = clazz.getDeclaredField("fileHeaderId");
            java.lang.reflect.Field filesField = clazz.getDeclaredField("files");

            fileHeaderIdField.setAccessible(true);
            filesField.setAccessible(true);

            String fileHeaderId = (String) fileHeaderIdField.get(item);
            if (StringUtils.isNotBlank(fileHeaderId)) {
                List<Long> fileHeaderIds = new ArrayList<>(1);
                fileHeaderIds.add(Long.parseLong(fileHeaderId));
                ResultData<List<SysFileDTO>> files = fileClient.findByFileHeaderIds(fileHeaderIds);
                if (files.getSuccess()) {
                    filesField.set(item, files.getResult());
                }
            }
        } catch (NoSuchFieldException e) {
            // 如果对象没有所需字段，则跳过处理
            log.warn("对象缺少必要的字段: {}", e.getMessage());
        }
    }
}