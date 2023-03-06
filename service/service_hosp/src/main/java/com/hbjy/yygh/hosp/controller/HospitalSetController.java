package com.hbjy.yygh.hosp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbjy.yygh.common.globalException.YyghException;
import com.hbjy.yygh.common.result.Result;
import com.hbjy.yygh.common.util.MD5;
import com.hbjy.yygh.hosp.service.HospitalSetService;
import com.hbjy.yygh.model.hosp.Department;
import com.hbjy.yygh.model.hosp.Hospital;
import com.hbjy.yygh.model.hosp.HospitalSet;
import com.hbjy.yygh.model.hosp.Schedule;
import com.hbjy.yygh.vo.hosp.HospitalSetQueryVo;

import com.hbjy.yygh.vo.order.SignInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@Api(tags = "医院设置管理")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
public class HospitalSetController {
    @Autowired
    private HospitalSetService service;
    @Autowired
    private MongoTemplate mongoTemplate;

    //1、查询医院表中的设置所有的信息
    @ApiOperation(value = "获取所有医院信息")
    @GetMapping("/findAll")
    public Result findAllHospital() {
        //调用 http://localhost:8201/admin/hosp/hospitalSet/findAll
        List<HospitalSet> list = service.list();
        Result<List<HospitalSet>> ok = Result.ok(list);
        return ok;
    }

    @ApiOperation(value = "根据id逻辑删除医院")
    //2、删除医院
    @DeleteMapping("{id}")
    public Result removeHospital(@PathVariable Long id) {
        boolean b = service.removeById(id);
        //级联删除详细信息
        HospitalSet hos = service.getById(id);
        Query query = new Query(Criteria.where("hoscode").is(hos.getHoscode()));
        List<Hospital> hospitals = mongoTemplate.find(query, Hospital.class);
        if (!hospitals.isEmpty()){
            mongoTemplate.remove(query,Hospital.class);//删除详细信息
            mongoTemplate.remove(query, Department.class);//删除科室信息
            mongoTemplate.remove(query, Schedule.class);//删除排班信息
        }
        if (b) {
            //封装查询结果
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //3、条件查询，带分页
    @PostMapping("/findPage/{current}/{limit}")
    public Result findHospitalPage(@PathVariable long current, @PathVariable long limit,
                                   //目的是传递信息 使用@RequestBody的意思是前端如果传来json数据，可以进行赋值，require=false意思是不是必需传递的参数
                                   @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo
    ) {
        //使用分页插件
        Page<HospitalSet> page = new Page<>(current, limit);
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        String hosname = hospitalSetQueryVo.getHosname();//医院名称
        String hoscode = hospitalSetQueryVo.getHoscode();//医院编号
        if (!StringUtils.isEmpty(hosname)) {//如果传来的医院名字不为空，则设置查询
            wrapper.like("hosname", hospitalSetQueryVo.getHosname());
        }
        if (!StringUtils.isEmpty(hoscode)) {//如果传来的医院编号不为空，则设置查询
            wrapper.eq("hoscode", hospitalSetQueryVo.getHoscode());
        }
        //如果两个都为空，则默认查询所有信息，并且使用分页
        Page<HospitalSet> pageResult = service.page(page, wrapper);
        return Result.ok(pageResult);
    }

    //4、添加医院接口
    /*
    * {
  "apiUrl": "http://localhost:9999",
  "contactsName": "王院长",
  "contactsPhone": "1512202220",
  "hoscode": "101",
  "hosname": "北京人民医院",
"isDeleted":0,
}只需要添加这几项就可以了
* 再数据库中：状态默认为0 时间不传自动填充，其中id自增长，不用写 isDelete默认为0
* */
    @PostMapping("/addHospital")
    public Result addHospital(@RequestBody HospitalSet hospitalSet) {

        //设置签名密钥
        Random random = new Random();
        String encrypt = MD5.encrypt(System.currentTimeMillis() + "" + random.nextInt(1000));
        hospitalSet.setSignKey(encrypt);
        boolean save = service.save(hospitalSet);
        if (save) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //6、根据id获取医院信息
    @GetMapping("/getHospitalById/{id}")
    public Result getHosById(@PathVariable long id) {
        HospitalSet hospital = service.getById(id);
        return Result.ok(hospital);
    }

    //7、修改医院信息
    @PostMapping("/updataHospital")
    public Result updataHospital(@RequestBody HospitalSet hospitalSet) {
        boolean b = service.updateById(hospitalSet);
        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //8、批量删除医院
    @DeleteMapping("/deleteHospitalMany")
    public Result RemoveManyHospital(@RequestBody List<Long> idList) {
        boolean b = service.removeByIds(idList);
        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }

    }

    //9、医院设置的锁定和解锁 （只有解锁状态，我们才可以操作）就是设置status的值
    @PutMapping("/LockHospital/{id}/{status}")
    public Result lockHospital(@PathVariable long id, @PathVariable Integer status) {
        //先根据id查出具体医院，然后再修改
        HospitalSet hospital = service.getById(id);
        //设置状态
        hospital.setStatus(status);
        //更新
        service.updateById(hospital);

        //更新医院的详细状态
        Query query = new Query(Criteria.where("hoscode").is(hospital.getHoscode()));
        List<Hospital> hospitals = mongoTemplate.find(query, Hospital.class);
        //设置修改的值
        hospitals.get(0).setStatus(status);
        hospitals.get(0).setUpdateTime(new Date());
        mongoTemplate.save(hospitals.get(0));
        return Result.ok();
    }

    //10、发送签名密钥
    @PutMapping("/SendKey/{id}")
    public Result sendKey(@PathVariable Long id) {
        //首先查出id所在医院
        HospitalSet h = service.getById(id);
        //get到密钥 编号等
        String hoscode = h.getHoscode();
        String signKey = h.getSignKey();
        //测试异常捕捉功能！！！
        throw new YyghException("功能还未开发！", 500);
        //TODO发送短信！
        //return Result.ok();
    }
    // 为service-order远程调用使用，获取签名信息
    @GetMapping("/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(@PathVariable String hoscode){
        return service.getSignInfoVo(hoscode);
    }

}
