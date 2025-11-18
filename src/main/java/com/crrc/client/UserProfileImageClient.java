package com.crrc.client;

import com.crrc.pojo.dto.ResponseData;
import com.crrc.pojo.dto.ImageReqDTO;
import com.crrc.pojo.dto.UserImageDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "userProfile", url = "http://10.217.216.115")
public interface UserProfileImageClient {

    /**
     * 根据人员工号列表，获取这些人的图片数据
     * @param listDTO 包含人员工号列表的DTO
     * @return 包含图片数据的DTO
     */
    @PostMapping("/v1/api/person/uuid/list")
    ResponseData<List<UserImageDTO>> getPersonImageByUserCode(ImageReqDTO listDTO);
}
