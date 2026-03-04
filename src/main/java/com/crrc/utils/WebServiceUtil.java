package com.crrc.utils;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.crrc.entity.WSPData;
import com.crrc.entity.WSPMethod;
import com.crrc.entity.WSPParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class WebServiceUtil {


//    @Autowired
//    private DataLogService dataLogService;
    @Autowired
    private WebServiceUtils webServiceUtils;
    @Autowired
    private DictUtils dictUtils;

    /**
     *  webservice使用总线请求工具方法
     *  20250709，目前用于请求金蝶和智慧园区数据用于在移动端展示
     * @param content 请求参数， 目前为档案号
     * @param messageCode 申请的总线消息号
     * @return 返回的报文
     */
    public String sendByWebService(String content, String messageCode){
        String url =  dictUtils.get("webService_url").getItems().get(0).getItemName();
        WSPData wspData = new WSPData(url,
                "http://www.pgxxgc.com/IF_Service",
                "if");
        WSPMethod wspMethod = new WSPMethod("IFService");
        List<WSPParam> params = wspMethod.getParams();
        params.add(new WSPParam("sMsgID", messageCode));
        params.add(new WSPParam("sContent", content, true));
        wspData.getMethods().add(wspMethod);
        return webServiceUtils.send(wspData);
    }

    /**
     * 将webService发回的总线包装数据，拆分到实际有效的信息部分
     * @param xmlMsg 总线包装数据
     * @return 实际有效的信息部分
     */
    public String xmlTransformer(String xmlMsg) {


        try {
            // 1. 记录原始输入数据（截断前500字符避免日志过大）
            log.debug("开始处理XML/JSON转换，原始数据(前500字符): {}",
                    xmlMsg != null ? xmlMsg.substring(0, Math.min(xmlMsg.length(), 500)) : "NULL");

            // 2. 检查输入是否为空
            if (xmlMsg == null || xmlMsg.trim().isEmpty()) {
                log.error("输入数据为空");
                throw new IllegalArgumentException("输入XML/JSON数据不能为空");
            }

            // 3. 解析JSON
            log.debug("开始解析JSON...");
            JSONObject jsonObject = JSONUtil.parseObj(xmlMsg);
            log.debug("一级解析完成，获取到根对象");

            // 4. 逐层解析SOAP结构
            JSONObject soapEnvelope = jsonObject.getJSONObject("soap:Envelope");
            if (soapEnvelope == null) {
                log.error("缺少soap:Envelope节点");
                throw new RuntimeException("无效的SOAP格式: 缺少soap:Envelope");
            }

            JSONObject soapBody = soapEnvelope.getJSONObject("soap:Body");
            if (soapBody == null) {
                log.error("缺少soap:Body节点");
                throw new RuntimeException("无效的SOAP格式: 缺少soap:Body");
            }

            JSONObject ifServiceResponse = soapBody.getJSONObject("IFServiceResponse");
            if (ifServiceResponse == null) {
                log.error("缺少IFServiceResponse节点");
                throw new RuntimeException("无效的SOAP格式: 缺少IFServiceResponse");
            }

            JSONObject ifServiceReturn = ifServiceResponse.getJSONObject("IFServiceReturn");
            if (ifServiceReturn == null) {
                log.error("缺少IFServiceReturn节点");
                throw new RuntimeException("无效的SOAP格式: 缺少IFServiceReturn");
            }

            // 5. 返回结果并记录
            String result = ifServiceReturn.toString();
            log.debug("转换成功，结果数据长度: {}", result.length());
            log.trace("完整结果数据: {}", result);  // 注意：生产环境慎用trace级别

            return result;

        } catch (Exception e) {
            // 6. 异常处理
            log.error("XML/JSON转换失败: {}", e.getMessage(), e);
            throw new RuntimeException("XML/JSON转换失败: " + e.getMessage(), e);
        }
    }
}
