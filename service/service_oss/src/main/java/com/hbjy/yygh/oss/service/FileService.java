package com.hbjy.yygh.oss.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String upload(MultipartFile multipartFile);
}
