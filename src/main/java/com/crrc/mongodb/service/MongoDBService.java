package com.crrc.mongodb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Service
public class MongoDBService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoDBService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 以userCode作为Id保存数据
     * @param data 数据
     */
    public <T> void saveDataByUserCode(T data, String userCode) {
        setDataId(data, userCode);
        mongoTemplate.save(data);
    }

    private <T> void setDataId(T data, String userCode) {
        Class<?> clazz = data.getClass();
        try {
            // 断定是否存在id的setter
            Method setter = clazz.getMethod("setId", String.class);
            setter.invoke(data, userCode);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("类: %s不存在setId方法，或id类型不为String", data.getClass().getName()));
        }
    }

    public <T> void saveDataByUserCode(T data, String userCode, String collectionName) {
        setDataId(data, userCode);
        mongoTemplate.save(data, collectionName);
    }

    public <T> T getById(String id, Class<T> entityClass, String collectionName) {
        Query idQuery = new Query(Criteria.where("id").is(id));
        return mongoTemplate.findOne(idQuery, entityClass, collectionName);
    }
}
