package com.crrc.service.impl;

import com.crrc.entity.PostCatalogInfo;
import com.crrc.mapper.PostCatalogInfoMapper;
import com.crrc.pojo.vo.PostCatalogInfoVO;
import com.crrc.service.PostCatalogInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yukun
 */
@Service
@Slf4j
public class PostCatalogInfoServiceImpl extends WorkServiceImpl<PostCatalogInfoMapper, PostCatalogInfo> implements PostCatalogInfoService {
    /**
     * 只能查询叶子节点！
     * @param reqVO 默认会拼装nodeLevel=3
     * @return 结果
     */
    public List<PostCatalogInfoVO> queryVO(PostCatalogInfo reqVO) {
        reqVO.setNodeLevel("3");
        Map<Long, PostCatalogInfo> cacheMap = new HashMap<>();
        List<PostCatalogInfo> query = this.query(reqVO);
        // 带缓存的拼装
        return query.stream().map(type -> {
            PostCatalogInfoVO postCatalogInfoVO = new PostCatalogInfoVO();
            if (!cacheMap.containsKey(type.getParentId())) {
                PostCatalogInfo group = this.baseMapper.selectById(type.getParentId());
                cacheMap.put(group.getId(), group);
                if (!cacheMap.containsKey(group.getParentId())) {
                    PostCatalogInfo category = this.baseMapper.selectById(group.getParentId());
                    cacheMap.put(category.getId(), category);
                }
            }
            postCatalogInfoVO.setTypeName(type.getName());
            postCatalogInfoVO.setTypeCode(type.getCode());
            PostCatalogInfo group = cacheMap.get(type.getParentId());
            postCatalogInfoVO.setGroupName(group.getName());
            postCatalogInfoVO.setGroupCode(group.getCode());
            PostCatalogInfo category = cacheMap.get(group.getParentId());
            postCatalogInfoVO.setCategoryName(category.getName());
            postCatalogInfoVO.setCategoryCode(category.getCode());
            return postCatalogInfoVO;
        }).collect(Collectors.toList());
    }

}
