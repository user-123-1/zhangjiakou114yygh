package com.hbjy;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.Map;

public class ExcelListener extends AnalysisEventListener<User> {
    //作用：一行一行读取excel内容，从第二行读取
    @Override
    public void invoke(User user, AnalysisContext analysisContext) {
        System.out.println(user);//就是读取一行的内容
    }

    //读取之后执行
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    //读取表头信息
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        System.out.println("表头信息:" + headMap);
    }
}
