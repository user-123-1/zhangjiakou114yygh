package com.hbjy.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;

import com.hbjy.yygh.hosp.repository.DepartmentRepository;
import com.hbjy.yygh.hosp.service.DepartmentService;
import com.hbjy.yygh.model.hosp.Department;

import com.hbjy.yygh.vo.hosp.DepartmentQueryVo;
import com.hbjy.yygh.vo.hosp.DepartmentVo;
import com.mongodb.client.result.DeleteResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;


import java.util.*;

import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    DepartmentRepository repository;
    //上传科室
    @Override
    public void save(List<Department> departmentList) {
        for (Department department:departmentList){
            Query query = new Query(Criteria.where("hoscode").is(department.getHoscode()).and("depcode").is(department.getDepcode()));
            List<Department> departments = mongoTemplate.find(query, Department.class);
            if (departments.isEmpty()){
                //为空则添加
                department.setCreateTime(new Date());
                department.setUpdateTime(new Date());
                department.setIsDeleted(0);
                mongoTemplate.save(department);
            }else {
                //不为空 则修改
                department.setUpdateTime(new Date());
                department.setIsDeleted(0);
                mongoTemplate.save(department);
            }
        }

    }
    //查询科室的方法
    @Override
    public Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo vo) {
        Pageable pageRequest = PageRequest.of(page - 1, limit);
        Department department = new Department();
        BeanUtils.copyProperties(vo,department);
        department.setIsDeleted(0);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Example<Department> example = Example.of(department,matcher);
        Page<Department> all = repository.findAll(example,pageRequest);
        return all;
    }

    //删除科室接口
    @Override
    public void remove(String hoscode, String depcode) {
        //先根据科室编号和医院编号查询是否存在
        Query q = new Query(Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode));
        List<Department> departments = mongoTemplate.find(q, Department.class);
        if (departments.isEmpty()){
            //如果为空，则不需要删除
        }else {

            mongoTemplate.remove(q,Department.class);
        }
    }

    //根据医院编号，查询科室信息
    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        //创建List集合，装东西
        List<DepartmentVo> result = new ArrayList<>();
        Query query = new Query(Criteria.where("hoscode").is(hoscode));
        //所有科室列表的信息
        List<Department> departments = mongoTemplate.find(query, Department.class);
        //根据大科室编号bigcode分组，获取每个大科室下面的子集科室
        //string是大科室的编号， List<Department>是大科室下面的小科室的信息
        Map<String, List<Department>> collect = departments.stream().collect(Collectors.groupingBy(Department::getBigcode));
        //遍历collect  目的是将数据封装成DepartmentVo 的形式， 也就是包含child节点
        for (Map.Entry<String,List<Department>> entry:collect.entrySet()){
            DepartmentVo tmp = new DepartmentVo();
            //封装 出child节点的属性
            String bigCode = entry.getKey();
            tmp.setDepcode(bigCode);
            List<Department> value = entry.getValue();
            tmp.setDepname(value.get(0).getBigname());

            List<DepartmentVo> child = new ArrayList<>();
            //封装child
            for (Department d:value){
                DepartmentVo tmp2 = new DepartmentVo();
                tmp2.setDepcode(d.getDepcode());
                tmp2.setDepname(d.getDepname());
                child.add(tmp2);
            }
            //设置child
            tmp.setChildren(child);
            //放到最终的result中
            result.add(tmp);
        }



        /*//自己的想法
        Map<String,List<Department>> bigCodeAndDepartmentList = new HashMap<>();
        Set<String> bigCodeSet = new HashSet<>();
        for (Department d:departments){
            bigCodeSet.add(d.getBigcode());
        }
        for (String bigCode:bigCodeSet){
            List<Department> list = new ArrayList<>();
            for (int i = 0; i < departments.size(); i++) {
                if (bigCode.equals(departments.get(i).getBigcode())){
                    list.add(departments.get(i));
                }
            }
            bigCodeAndDepartmentList.put(bigCode,list);
        }
        List<DepartmentVo> result2 = new ArrayList<>();
        for (Map.Entry<String,List<Department>> entry:bigCodeAndDepartmentList.entrySet()){
            DepartmentVo tmp = new DepartmentVo();
            //封装 出child节点的属性
            String bigCode = entry.getKey();
            tmp.setDepcode(bigCode);
            List<Department> value = entry.getValue();
            tmp.setDepname(value.get(0).getBigname());

            List<DepartmentVo> child = new ArrayList<>();
            //封装child
            for (Department d:value){
                DepartmentVo tmp2 = new DepartmentVo();
                tmp2.setDepcode(d.getDepcode());
                tmp2.setDepname(d.getDepname());
                child.add(tmp2);
            }
            //设置child
            tmp.setChildren(child);
            //放到最终的result中
            result2.add(tmp);
        }*/

        //System.out.println("两个方法是否得到了相同的结果："+bigCodeAndDepartmentList.equals(collect));


        return result;
    }
    //删除科室的接口
    @Override
    public void deleteDepartment(String hoscode) {
        Query query = new Query(Criteria.where("hoscode").is(hoscode));
        mongoTemplate.remove(query, Department.class);
    }

    //查找所有的科室，非树型结构
    @Override
    public List<Department> findDepartment(String hoscode) {
        Query query = new Query(Criteria.where("hoscode").is(hoscode));
        List<Department> departmentList = mongoTemplate.find(query, Department.class);
        return departmentList;
    }

    @Override
    public Department getDepartment(String hoscode, String depcode) {
        Query query = new Query(Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode));
        List<Department> list = mongoTemplate.find(query, Department.class);
        return list.get(0);
    }

    @Override
    public String getDepName(String hoscode, String depcode) {
        Query query = new Query(Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode));
        List<Department> list = mongoTemplate.find(query, Department.class);

        return list.get(0).getDepname();
    }


}
