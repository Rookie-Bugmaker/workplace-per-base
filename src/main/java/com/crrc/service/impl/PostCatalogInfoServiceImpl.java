package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.entity.PostCatalogInfo;
import com.crrc.mapper.PostCatalogInfoMapper;
import com.crrc.pojo.vo.PostCatalogInfoTreeVO;
import com.crrc.pojo.vo.PostCatalogInfoVO;
import com.crrc.service.PostCatalogInfoService;
import com.crrc.utils.TreeUtils;
import com.crrc.utils.WrapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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

    /**
     * 根据实体进行分页查询
     * @param reqVO
     * @return
     */
    public void page(Page<PostCatalogInfo> page, PostCatalogInfo reqVO) {
        QueryWrapper<PostCatalogInfo> queryWrapper = WrapperUtils.buildWrapper(reqVO);
        this.page(page, queryWrapper);
    }

    public List<PostCatalogInfo> query(PostCatalogInfo reqVO) {
        QueryWrapper<PostCatalogInfo> queryWrapper = WrapperUtils.buildWrapper(reqVO);
        return this.baseMapper.selectList(queryWrapper);
    }

    public List<PostCatalogInfoTreeVO> queryTree() {
        List<PostCatalogInfo> postCatalogInfoList = this.query(new PostCatalogInfo());
        List<PostCatalogInfoTreeVO> fullList = postCatalogInfoList.stream().map(item -> {
            PostCatalogInfoTreeVO postCatalogInfoTreeVO = new PostCatalogInfoTreeVO();
            BeanUtils.copyProperties(item, postCatalogInfoTreeVO);
            return postCatalogInfoTreeVO;
        }).collect(Collectors.toList());

        return TreeUtils.buildCascadeList(
                fullList,
                PostCatalogInfoTreeVO::getId,
                PostCatalogInfoTreeVO::getParentId,
                PostCatalogInfoTreeVO::setChildren
        );
    }
}
