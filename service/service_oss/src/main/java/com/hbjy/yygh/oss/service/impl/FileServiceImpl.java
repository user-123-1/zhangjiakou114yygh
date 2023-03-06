package com.hbjy.yygh.oss.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.hbjy.yygh.oss.service.FileService;
import com.hbjy.yygh.oss.utils.OssUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
    @Override
    public String upload(MultipartFile multipartFile) {
        String endpoint = OssUtils.REGION_Id;
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = OssUtils.ACCESS_KEY_ID;
        String accessKeySecret = OssUtils.SECRECT;
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "biyesheji-yygh";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            InputStream inputStream = multipartFile.getInputStream();
            String fileName = multipartFile.getOriginalFilename();
            //使用uuid生成唯一名字
            String s = UUID.randomUUID().toString().replace("-", "");
            String newFileName = s+"-"+fileName;
            //按照日期创建文件夹，将文件放入其中
            String dateTime = new DateTime().toString("yyyy/MM/dd");
            newFileName = dateTime+"/"+newFileName;//  /2022/11/17/uuid-文件名.png

            //参数1 传入bucket名字 参数2 传入路径和文件名字 如/a/b/a.txt  参数3 传入输入流
            ossClient.putObject(bucketName,newFileName,inputStream);
            //返回上传之后的路径  https://biyesheji-yygh.oss-cn-zhangjiakou.aliyuncs.com/logo.png
            String url = "https://"+bucketName+".oss-cn-zhangjiakou.aliyuncs.com"+"/"+newFileName;
            return url;

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }
}
