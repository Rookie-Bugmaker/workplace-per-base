package com.crrc.pojo.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "Objects")
public class XmlResponseDTO {

    @JacksonXmlProperty(localName = "M_TYPE")
    private String mType;

    @JacksonXmlProperty(localName = "MESSAGE")
    private String message;
}
