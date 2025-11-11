package com.crrc.service.impl;

import com.crrc.entity.filedata.vo.FileDataStream;
import com.crrc.utils.MinIOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author why
 */
@Service
public class FileServiceImpl {
    @Resource
    private MinIOUtils minIoUtils;

    public String uploadTemplate(MultipartFile file) throws Exception {
        String fileId;
        try {
            fileId = minIoUtils.saveFile(file);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return fileId;
    }

    public String preview(String fileId){

         String result = minIoUtils.getFileOnline(fileId);
        if (StringUtils.isBlank(result)){
            return null;
        }else {
            return result + "&officePreviewType=pdf";
        }
    }

    public void download(String fileId, HttpServletResponse res) {
        minIoUtils.getFile(fileId,res);
    }

    public void downLoadBatch(List<String> idList , HttpServletResponse res){
        minIoUtils.listFile(idList,res);
    }

    public FileDataStream getFileStream(String fileId){
        return minIoUtils.getFileStream(fileId);
    }
}
