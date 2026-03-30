package com.crrc.utils.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.crrc.entity.HrEmpBasic;
import com.crrc.mapper.HrEmpBasicMapper;
import com.crrc.service.impl.CodeInfoServiceImpl;
import com.crrc.service.impl.HrEmpBasicServiceImpl;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PoliticalStatusImportListener implements ReadListener<HrEmpBasic> {

    private final HrEmpBasicServiceImpl hrEmpBasicService;
    private final List<HrEmpBasic> buffer = new ArrayList<>();
    private final SqlSessionFactory sqlSessionFactory;
    private final Map<String, String> translateMap = new HashMap<>();

    public PoliticalStatusImportListener(HrEmpBasicServiceImpl hrEmpBasicService, CodeInfoServiceImpl codeInfoService, SqlSessionFactory sqlSessionFactory) {
        this.hrEmpBasicService = hrEmpBasicService;
        this.sqlSessionFactory = sqlSessionFactory;
        translateMap.putAll(codeInfoService.getTranslateMap("hr_emp_basic", "BASIC_POLITICAL").entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getValue,
                Map.Entry::getKey,
                (a, b) -> a
        )) );
    }

    @Override
    public void invoke(HrEmpBasic hrEmpBasic, AnalysisContext analysisContext) {
        buffer.add(hrEmpBasic);
        if(buffer.size() > 1000) {
            flush();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        flush();
    }

    public void flush() {

        HrEmpBasicMapper mapper = hrEmpBasicService.getBaseMapper();
        // 1. 打开一个BATCH模式的SqlSession，关闭自动提交
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            for (HrEmpBasic basic : buffer) {
                LambdaUpdateWrapper<HrEmpBasic> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(HrEmpBasic::getUserCode, basic.getUserCode());
                wrapper.set(HrEmpBasic::getPolitical, translateMap.get(basic.getPoliticalValue()));
                mapper.update(wrapper);
            }

            sqlSession.flushStatements(); // 刷新批处理语句
            sqlSession.commit();          // 提交事务
            sqlSession.clearCache();      // 清理缓存
            buffer.clear();
        } catch (Exception e) {
            // 发生异常时，由于未提交，事务会回滚
            throw new RuntimeException("批量更新失败", e);
        }
    }
}
