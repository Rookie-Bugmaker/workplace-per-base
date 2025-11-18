package com.crrc.service.impl;

import com.crrc.entity.CategoryRecord;
import com.crrc.mapper.CategoryRecordMapper;
import com.crrc.service.CategoryRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CategoryServiceImpl extends WorkServiceImpl<CategoryRecordMapper, CategoryRecord> implements CategoryRecordService {
}
