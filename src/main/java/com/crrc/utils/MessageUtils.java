package com.crrc.utils;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.crrc.constants.RsCode;
import com.crrc.pojo.dto.RsMessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service
 *
 * @author Li Ze
 * @version 2021-11-25
 */
@Slf4j
@Component
public class MessageUtils {

    private final ObjectMapper objectMapper;
    private volatile String accessToken = "";
    @Value("${rs.agentId}")
    private String agentId;
    @Value("${rs.corpSecret}")
    private String corpSecret;
    @Value("${rs.corpId}")
    private String corpId;

    @Value("${rs.accessTokenUrl}")
    private String accessTokenUrl;

    @Value("${rs.postMsgUrl}")
    private String postMsgUrl;

    public MessageUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void createAccessToken() {
        String result;
        try {
            UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl(accessTokenUrl);

            urlBuilder.queryParam("corpid", corpId);
            urlBuilder.queryParam("corpsecret", corpSecret);

            String url = urlBuilder.toUriString();
            log.info("url = {}", url);
            result = HttpUtil.get(url);
            log.info("token result = {}", result);
        } catch (Exception e) {
            log.error("融数平台获取token异常：", e);
            throw new IllegalArgumentException("融数平台获取token异常：" + e.getMessage());
        }
        Map<String, Object> map = JSON.parseObject(result);
        Object accessTokenObj = map.get("access_token");
        if (accessTokenObj != null) {
            accessToken = accessTokenObj.toString();
            log.info("accessToken: {}", accessToken);
        }
    }

    public String pushMsg(List<String> userCodes, String url, String message) {
        if (ObjectUtils.isEmpty(userCodes) || !StringUtils.hasText(userCodes.get(0))) {
            throw new IllegalArgumentException("通知人不能为空");
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", "审批通知");
        jsonObject.put("description", message);
        jsonObject.put("url", url);

        try {
            StringBuilder toUser = new StringBuilder();
            for (String userCode : userCodes) {
                toUser.append("|").append(userCode);
            }
            Map<String, Object> requestMap = new HashMap<>(2);
            requestMap.put("touser", toUser.substring(1));
            requestMap.put("msgtype", "textcard");
            requestMap.put("agentid", agentId);
            requestMap.put("textcard", jsonObject);
            return postMsg(requestMap);
        } catch (Exception e) {
            log.info("融数平台发送消息异常: ", e);
            throw new IllegalArgumentException("融数平台发送消息异常：" + e.getMessage());
        }
    }

    public String pushPlainMsg(List<String> userCodes, String message) {
        if (ObjectUtils.isEmpty(userCodes) || !StringUtils.hasText(userCodes.get(0))) {
            throw new IllegalArgumentException("通知人不能为空");
        }
        try {
            StringBuilder toUser = new StringBuilder();
            for (String userCode : userCodes) {
                toUser.append("|").append(userCode);
            }
            Map<String, Object> requestMap = new HashMap<>(2);
            HashMap<String,String> text = new HashMap<String,String>();
            text.put("content",message);
            requestMap.put("touser", toUser.substring(1));
            requestMap.put("msgtype", "text");
            requestMap.put("agentid", agentId);
            requestMap.put("text", text);
            return postMsg(requestMap);
        } catch (Exception e) {
            log.info("融数平台发送消息异常: ", e);
            throw new IllegalArgumentException("融数平台发送消息异常：" + e.getMessage());
        }
    }

    private String postMsg(Map<String, Object> requestMap) throws JsonProcessingException {
        if (!StringUtils.hasText(accessToken)) {
            createAccessToken();
        }

        for(int i = 0; i < 4; i++) {
            UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl(postMsgUrl);
            urlBuilder.queryParam("access_token", accessToken);
            String msgUrl = urlBuilder.toUriString();
            log.info("msgUrl = {}", msgUrl);

            String result = HttpUtil.post(msgUrl, JSON.toJSONString(requestMap));
            RsMessageDTO rsMessageDTO = objectMapper.readValue(result, RsMessageDTO.class);
            log.info("rsMessageDTO: {}", rsMessageDTO);
            if(rsMessageDTO != null && RsCode.OK.getCode() == rsMessageDTO.getErrcode()) {
                log.info("result = {}", result);
                return result;
            } else {
                log.info("msg send failed = {}", result);
                createAccessToken();
            }
        }

        return "{\"errcode\":99999,\"errmsg\":\"Too many failed attempts\"}";
    }
}
