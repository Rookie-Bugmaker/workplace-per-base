package com.crrc.mongodb.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MongoDBService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoDBService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 以userCode作为Id保存数据
     * @param obj 数据
     */
    public <T> void saveDataByUserCode(T obj, String userCode) {
        Object data;
        try {
            data = obj.getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
        BeanUtils.copyProperties(obj, data);
        setDataId(data, userCode);
        mongoTemplate.save(data);
    }

    private <T> void setDataId(T data, String userCode) {
        BeanWrapper wrapper = new BeanWrapperImpl(data);
        if(wrapper.isWritableProperty("id")) {
            wrapper.setPropertyValue("id", userCode);
        } else {
            throw new IllegalArgumentException(String.format("类%s存在不可写入的id属性", data.getClass().getName()));
        }
    }

    public <T> void saveDataByUserCode(T obj, String userCode, String collectionName) {
        Object data;
        try {
            data = obj.getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
        BeanUtils.copyProperties(obj, data);
        setDataId(data, userCode);
        mongoTemplate.save(data, collectionName);
    }

    public <T> T getById(String id, Class<T> entityClass, String collectionName) {
        Query idQuery = new Query(Criteria.where("id").is(id));
        return mongoTemplate.findOne(idQuery, entityClass, collectionName);
    }

    public <T> List<T> getByUserCode(String userCode, Class<T> entityClass, String collectionName) {
        Query idQuery = new Query(Criteria.where("user_code").is(userCode));
        return mongoTemplate.find(idQuery, entityClass, collectionName);
    }
}
