package com.crrc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crrc.entity.PictureError;
import com.crrc.mapper.PictureErrorMapper;
import com.crrc.service.PictureErrorService;
import org.springframework.stereotype.Service;

@Service
public class PictureErrorServiceImpl extends ServiceImpl<PictureErrorMapper, PictureError> implements PictureErrorService {
}
