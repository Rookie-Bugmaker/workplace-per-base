package com.crrc.utils;

import com.crrc.client.FileClient;
import com.crrc.common.ResultData;
import com.crrc.pojo.dto.SysFileDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
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
     * 处理单个对象的附件解析（支持继承字段查找）
     */
    private static void processSingleItem(Object item) throws Exception {
        try {
            // 获取对象类及其所有父类
            Class<?> clazz = item.getClass();

            // 查找 fileHeaderId 字段（包括父类）
            Field fileHeaderIdField = findFieldInHierarchy(clazz, "fileHeaderId");
            // 查找 files 字段（当前类）
            Field filesField = findFieldInHierarchy(clazz, "files");

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
            log.warn("对象缺少必要的字段: {}", e.getMessage());
        }
    }

    /**
     * 在类继承链中查找字段（包括父类）
     */
    private static Field findFieldInHierarchy(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        throw new NoSuchFieldException("字段 '" + fieldName + "' 在类 " + clazz.getName() + " 及其父类中未找到");
    }
}