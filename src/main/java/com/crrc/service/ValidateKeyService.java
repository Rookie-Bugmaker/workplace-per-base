package com.crrc.service;

import com.alibaba.nacos.common.utils.MD5Utils;
import com.crrc.entity.ValidateKeyEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
/**
 * @author why
 * 逻辑是让业务类继承ValidateKeyEntity
 * 这个父类里有一个字段用于判断是否更新，这个字段需要加在hrsc的业务表中，但hcm不需要，因为加了mybatis的过滤器可以过滤这些父类字段
 * getKey -- 用于计算密钥
 * validate -- 处理密钥 null代表数据新增 NEW代表数据需要更新
 * processCollection -- 更新密钥，将所有数据的密钥更新为最新版
 *
 * 该类暂时启用，用sql实现增量同步，如果效率低在考虑这套
 */
public class ValidateKeyService {

    /**
     * 标记数据方法，计算密钥是否改变，如果改变就set为NEW
     * @param dataList
     * @param keyGetter
     * @param batchSaveFunction
     * @param <T>
     * @return
     */
    public <T extends ValidateKeyEntity> int validate(List<T> dataList,
                                                      Function<T, String> keyGetter,
                                                      Consumer<List<T>> batchSaveFunction) {
        List<T> toUpdate = dataList.stream()
                .filter(item -> {
                    String oldKey = keyGetter.apply(item);
                    return oldKey != null && !oldKey.equals(getKey(item));
                })
                .peek(item -> item.setValidateKey("NEW"))
                .collect(Collectors.toList());

        if (!toUpdate.isEmpty()) {
            batchSaveFunction.accept(toUpdate);
        }
        return toUpdate.size();
    }

    /**
     * 密钥生成方法
     * @param entity
     * @return
     */
    public String getKey(ValidateKeyEntity entity) {
        return MD5Utils.md5Hex(entity.toString(), "UTF-8");
    }

    /**
     * 处理所有validateKey为new的数据，重新计算validateKey
     * @param records
     * @param batchUpdater
     * @param <T>
     * @return
     */
    public <T extends ValidateKeyEntity> int processCollection(
            List<T> records,
            Consumer<List<T>> batchUpdater) {

        List<T> toUpdate = records.stream()
                .peek(entity -> {
                    // 直接调用父类方法
                    String newKey = getKey(entity);
                    if (!newKey.equals(entity.getValidateKey())) {
                        entity.setValidateKey(newKey);
                    } else {
                        // 标记为不需要更新
                        entity = null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!toUpdate.isEmpty()) {
            batchUpdater.accept(toUpdate);
        }
        return toUpdate.size();
    }


}
