package com.hbjy;

import com.alibaba.excel.EasyExcel;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestEasyExcel {
    @Test
    public void myTest() {
        //构建数据集合
        List<User> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User u = new User();
            u.setId(i);
            u.setUsername("用户" + i);
            list.add(u);
        }

        //1、设置excel文件路径和文件名称
        String file = "C:\\阿里云下载\\毕设\\excelTest\\user.xlsx";
        //2、写操作
        EasyExcel.write(file, User.class).sheet("用户信息")
                .doWrite(list);


    }

    @Test
    public void TestRead() {
        //1、读操作 设置类，完成映射，并且添加列内容
        /*
        * @ExcelProperty(value = "用户编号",index = 0)
    private int id;
    @ExcelProperty(value = "用户名称",index = 1)
    private String username;

        * */
        //2、 写监听器，监听读写

        String fileName = "C:\\阿里云下载\\毕设\\excelTest\\user.xlsx";
        EasyExcel.read(fileName, User.class, new ExcelListener()).sheet().doRead();
    }

}
