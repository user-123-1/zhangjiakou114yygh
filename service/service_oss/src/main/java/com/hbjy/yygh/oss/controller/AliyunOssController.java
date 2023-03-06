package com.hbjy.yygh.oss.controller;

import com.hbjy.yygh.common.result.Result;
import com.hbjy.yygh.oss.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/oss/file")
public class AliyunOssController {
    @Autowired
    private FileService fileService;
    //上传文件到阿里云Oss
    @PostMapping("/fileUpload")
    public Result fileUpload(MultipartFile file){
        //获取上传文件
        String path = fileService.upload(file);
        return Result.ok(path);

    }
}
