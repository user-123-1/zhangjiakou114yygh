package com.hbjy.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hbjy.yygh.cmn.importListener.DictImportLister;
import com.hbjy.yygh.cmn.mapper.DictMapper;
import com.hbjy.yygh.cmn.service.DictService;
import com.hbjy.yygh.model.cmn.Dict;
import com.hbjy.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DictServiceImpl implements DictService {
    @Resource
    private DictMapper mapper;

    @Override
    //根据id查找数据
    //加入了缓存操作
    @Cacheable(value = "dict", keyGenerator = "keyGenerator")
    public List<Dict> findChildById(Long id) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", id);
        List<Dict> list = mapper.selectList(queryWrapper);
        //设置是否还有子节点
        for (Dict dict : list) {
            Long dictId = dict.getId();
            boolean isHasChild = this.isHasChild(dictId);
            dict.setHasChildren(isHasChild);
        }
        return list;
    }

    //判断id下是否还有子节点
    public boolean isHasChild(Long id) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", id);
        Integer count = mapper.selectCount(queryWrapper);
        return count > 0;
    }
    //导出数据字典的操作

    @Override
    public void exportDictData(HttpServletResponse response) {
        response.setContentType("application/vnd.ms-excel");//设置类型未excel
        response.setCharacterEncoding("utf-8");
        String fileName = "dict";
        //目的是以下载的方式打开
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        //查询数据库
        List<Dict> dicts = mapper.selectList(null);
        //因为提前准备好了实体类DicEeVo对象，专门用来写写excel的，但是我们查询数据库的来的是Dict对象
        //所以要将Dict对象装欢未DictEeVo对象
        List<DictEeVo> dictEeVos = new ArrayList<>();
        for (Dict dict : dicts) {
            DictEeVo vo = new DictEeVo();
            vo.setId(dict.getId());
            vo.setDictCode(dict.getDictCode());
            vo.setName(dict.getName());
            vo.setParentId(dict.getParentId());
            vo.setValue(dict.getValue());
            dictEeVos.add(vo);
        }
        //调用Excel写入  这里的地址指的是输出流
        try {
            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("dict")
                    .doWrite(dictEeVos);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //导入数据
    //加入缓存操作，操作为清空缓存， 因为要导入数据了吗
    @CacheEvict(value = "dict", allEntries = true)
    @Override
    public void importDictData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), DictEeVo.class, new DictImportLister(mapper)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getDictName(String dictCode, String value) {
        //如果dictCode为空
        if (StringUtils.isEmpty(dictCode)){
            QueryWrapper<Dict> wrapper = new QueryWrapper<>();
            wrapper.eq("value",value);
            Dict dict = mapper.selectOne(wrapper);
            return dict.getName();
        }else {//如果不为空
            //根据dict_code查询dict 对象，得到id值 然后查自己儿子
            QueryWrapper<Dict> wrapper = new QueryWrapper<>();
            wrapper.eq("dict_code",dictCode);
            Dict dict = mapper.selectOne(wrapper);
            Long parent_id = dict.getId();
            //根据parentid和value查询
            Dict find = mapper.selectOne(new QueryWrapper<Dict>().eq("parent_id",parent_id).eq("value",value));
            return find.getName();

        }

    }
    //根据dictCode获取下节点
    @Override
    public List<Dict> findByDictCode(String dictCode) {
        //根据dictCode获取对应的id
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code",dictCode);
        Dict dict = mapper.selectOne(wrapper);

        List<Dict> childById = this.findChildById(dict.getId());

        return childById;
    }
}
