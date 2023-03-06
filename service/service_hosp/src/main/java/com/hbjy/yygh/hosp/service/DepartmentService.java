package com.hbjy.yygh.hosp.service;


import com.hbjy.yygh.model.hosp.Department;
import com.hbjy.yygh.vo.hosp.DepartmentQueryVo;
import com.hbjy.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    void save(List<Department> departmentList);

    Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo vo);

    void remove(String hoscode, String depcode);

    List<DepartmentVo> findDeptTree(String hoscode);

    void deleteDepartment(String hoscode);


    List<Department> findDepartment(String hoscode);

    Department getDepartment(String hoscode, String depcode);

    String getDepName(String hoscode, String depcode);
}
