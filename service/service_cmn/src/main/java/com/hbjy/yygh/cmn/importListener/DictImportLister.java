package com.hbjy.yygh.cmn.importListener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.hbjy.yygh.cmn.mapper.DictMapper;
import com.hbjy.yygh.model.cmn.Dict;
import com.hbjy.yygh.vo.cmn.DictEeVo;

public class DictImportLister extends AnalysisEventListener<DictEeVo> {
    private DictMapper mapper;

    public DictImportLister(DictMapper mapper) {
        this.mapper = mapper;
    }

    @Override//一行一行的读，其中dictEeVo就是数据
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        //因为传来的是dictEeVo对象，但是数据库中对应的是Dict对象，所以要转换
        Dict dict = new Dict();
        dict.setId(dictEeVo.getId());
        dict.setDictCode(dictEeVo.getDictCode());
        dict.setName(dictEeVo.getName());
        dict.setValue(dictEeVo.getValue());
        dict.setParentId(dictEeVo.getParentId());
        mapper.insert(dict);//每次读取一行，然后调用方法进行添加

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
