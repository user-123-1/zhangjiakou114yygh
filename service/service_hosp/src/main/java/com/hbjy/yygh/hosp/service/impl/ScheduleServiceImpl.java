package com.hbjy.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbjy.yygh.common.globalException.YyghException;
import com.hbjy.yygh.common.result.ResultCodeEnum;
import com.hbjy.yygh.hosp.repository.ScheduleRepository;
import com.hbjy.yygh.hosp.service.DepartmentService;
import com.hbjy.yygh.hosp.service.HospitalService;
import com.hbjy.yygh.hosp.service.ScheduleService;
import com.hbjy.yygh.model.hosp.BookingRule;
import com.hbjy.yygh.model.hosp.Department;
import com.hbjy.yygh.model.hosp.Hospital;
import com.hbjy.yygh.model.hosp.Schedule;
import com.hbjy.yygh.vo.hosp.BookingScheduleRuleVo;
import com.hbjy.yygh.vo.hosp.ScheduleOrderVo;
import com.hbjy.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    ScheduleRepository scheduleRepository;
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private DepartmentService departmentService;
    //上传排班
    @Override
    public void save(List<Schedule> scheduleList) {
        for (Schedule schedule:scheduleList){
            //根据医院编号和排班id查询排班
            Query q = new Query(Criteria.where("hoscode").is(schedule.getDocname()).and("hosScheduleId").is(schedule.getHosScheduleId()));
            List<Schedule> schedules = mongoTemplate.find(q, Schedule.class);
            if (schedules.isEmpty()){
                //如果为空 则添加
                schedule.setCreateTime(new Date());
                schedule.setUpdateTime(new Date());
                schedule.setIsDeleted(0);
                schedule.setStatus(1);
                mongoTemplate.save(schedule);
            }else{
                //如果不为空则 修改
                schedule.setUpdateTime(new Date());
                schedule.setIsDeleted(0);
                schedule.setStatus(1);
                mongoTemplate.save(schedule);
            }
        }
    }
    //删除排班
    @Override
    public void remove(String hoscode, String hosScheduleId) {
        //根据医院编号和排班编号查出信息
        Query query = new Query(Criteria.where("hoscode").is(hoscode).and("hosScheduleId").is(hosScheduleId));
        List<Schedule> schedules = mongoTemplate.find(query, Schedule.class);
        if (schedules.isEmpty()){
            //如果为空 则不操作
        }else{
            //进行删除
            mongoTemplate.remove(query,Schedule.class);
        }
    }
    //根据医院编号和科室编号，查询排班数据
    @Override
    public Map<String, Object> getScheduleRule(long page, long limit, String hoscode, String depcode) {
        //1、根据医院编号和科室编号进行查询
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        //2、根据workDate进行分组
        Aggregation agg = Aggregation.newAggregation(
                //匹配条件
          Aggregation.match(criteria),Aggregation.group("workDate").first("workDate").as("workDate")
                //3、统计号源数量
                .count().as("docCount").sum("reservedNumber").as("reservedNumber")
                .sum("availableNumber").as("availableNumber"),
                //排序
                Aggregation.sort(Sort.Direction.DESC,"workDate"),
                //分页
                Aggregation.skip((page-1)*limit),
                Aggregation.limit(limit)

        );
        //4、查询
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();


        //分组查询的总记录数量
        Aggregation totalAgg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalAggregateResult = mongoTemplate.aggregate(totalAgg, Schedule.class, BookingScheduleRuleVo.class);
        int total = totalAggregateResult.getMappedResults().size();


        //把日期对应的星期数获取
        for (BookingScheduleRuleVo bookingScheduleRuleVo:mappedResults){
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            //调用工具类
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }

        //设置最终结构进行返回
        Map<String,Object> result = new HashMap<>();
        result.put("bookingSchduleRuleList",mappedResults);
        result.put("total",total);//总记录数量 用于分页查询



        return result;
    }
    //根据医院编号、科室编号和工作日期，查询出排班的详细信息
    @Override
    public List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate) {
        Query query = new Query(Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").is(new DateTime(workDate).toDate()));
        List<Schedule> scheduleList = mongoTemplate.find(query, Schedule.class);

        return scheduleList;
    }
    //删除所有排班
    @Override
    public void removeAllSchedule(String hoscode) {
        Query query = new Query(Criteria.where("hoscode").is(hoscode));
        mongoTemplate.remove(query,Schedule.class);
    }

    @Override//获取所有医生的姓名
    public Set<String> getAllDocName(String hoscode) {
        Query query = new Query(Criteria.where("hoscode").is(hoscode));
        List<Schedule> schedules = mongoTemplate.find(query, Schedule.class);
        Set<String> docNameList = new HashSet<>();
        for (Schedule schedule:schedules){
            docNameList.add(schedule.getDocname());
        }
        return docNameList;
    }

    @Override
    public List<Schedule> getScheByNameAndDepcode(String hoscode, String depcode, String docname) {
        Query query = new Query(Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("docname").is(docname));
        List<Schedule> scheduleList = mongoTemplate.find(query, Schedule.class);
        return scheduleList;
    }

    //根据对象删除具体的排班信息
    @Override
    public void deleteDetailSchedule(Schedule schedule) {
        mongoTemplate.remove(schedule);
    }
    //获取可预约排班数据
    @Override
    public Map<String,Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        Map<String,Object> result = new HashMap<>();
        //获取预约规则
        //根据医院编号获取预约规则
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if (hospital==null){
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        //获取可预约的日期数据
        IPage iPage = this.getListDate(page,limit,bookingRule);
        //获取当前可预约的日期
        List<Date> dateList = iPage.getRecords();
        //获取可预约日期里面的科室剩余预约数量
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode)
                .and("workDate").in(dateList);
        Aggregation agg = Aggregation.newAggregation(Aggregation.match(criteria),
            Aggregation.group("workDate").first("workDate").as("workDate")
                .count().as("docCount").sum("availableNumber").as("availableNumber")
                .sum("reservedNumber").as("reservedNumber")
        );
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleVoList = aggregate.getMappedResults();
        //合并数据 map集合 key 日期
        Map<Date,BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(scheduleVoList)){
            scheduleVoMap = scheduleVoList.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate,BookingScheduleRuleVo->BookingScheduleRuleVo));

        }
        //获取可预约的排班规则集合
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        for (int i=0,len = dateList.size();i<len;i++){
            Date date = dateList.get(i);
            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
            //如果当天没有排班的医生
            if (bookingScheduleRuleVo==null){
                bookingScheduleRuleVo = new BookingScheduleRuleVo();

                bookingScheduleRuleVo.setDocCount(0);
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDate(date);
            //计算当前日期
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

            //最后一页最后一条记录为即将预约   状态 0：正常 1：即将放号 -1：当天已停止挂号
            if(i == len-1 && page == iPage.getPages()) {
                bookingScheduleRuleVo.setStatus(1);
            } else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //当天预约如果过了停号时间， 不能预约
            if(i == 0 && page == 1) {
                DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if(stopTime.isBeforeNow()) {
                    //停止预约
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }
        //可预约日期规则数据
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", iPage.getTotal());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getHospName(hoscode));
        //科室
        Department department =departmentService.getDepartment(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
//月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
//放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
//停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;

    }



    //获取可预约分页数据
    private IPage getListDate(Integer page, Integer limit, BookingRule bookingRule) {
        //获取当天放号时间， 年、月、日、 小时、分钟
        DateTime releaseTime = this.getDateTime(new Date(),bookingRule.getReleaseTime());
        //获取预约周期
        Integer cycle = bookingRule.getCycle();
        //如果当前放号时间已经过去，预约周期从后一天开始计算  周期+1
        if (releaseTime.isBeforeNow()) {
            cycle = cycle+1;
        }
        //获取可预约所有日志，最后一天显示即将放号
        List<Date> dateList = new ArrayList<>();
        for (int i=0;i<cycle;i++){
            DateTime currentDateTime = new DateTime().plusDays(i);
            String dateString = currentDateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(dateString).toDate());

        }
        //因为预约周期不同，每页最多显示七个，超过七天分页
        List<Date> pageList = new ArrayList<>();
        int start = (page-1)*limit;
        int end = (page-1)*limit+limit;
        //如果可以显示的数据小于7 直接显示
        if (end>dateList.size()){
            end = dateList.size();
        }
        for (int i=start;i<end;i++){
            pageList.add(dateList.get(i));
        }
        //如果显示数据大于7，进行分页
        IPage<Date> iPage = new Page<>(page,7,dateList.size());
        iPage.setRecords(pageList);
        return iPage;
    }

    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " "+ timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }



    //处理日期时间的工具类
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }
    //根据排班id获取排班信息
    @Override
    public Schedule getScheduleId(String scheduleId) {
        Schedule byId = mongoTemplate.findById(scheduleId, Schedule.class);

        return this.packageSchedule(byId);
    }



    private Schedule packageSchedule(Schedule schedule){
        schedule.getParam().put("hosname",hospitalService.getHospName(schedule.getHoscode()));
        schedule.getParam().put("depname",departmentService.getDepName(schedule.getHoscode(),schedule.getDepcode()));
        schedule.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
        return schedule;
    }

    //给service-oder远程调用使用，根据scheduleId获取详细信息
    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        //获取排班信息
        /*Query query = new Query(Criteria.where("hosScheduleId"));
        List<Schedule> scheduleList = mongoTemplate.find(query, Schedule.class);
        Schedule schedule = scheduleList.get(0);*/
        Schedule schedule = mongoTemplate.findById(scheduleId, Schedule.class);

        if (schedule==null){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }


        //根据排班信息，获取医院信息
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        if (hospital==null){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        BookingRule bookingRule = hospital.getBookingRule();
        if(null == bookingRule) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        //将获取的排班信息、医院信息设置到scheduleOrderVo中
        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospitalService.getHospName(schedule.getHoscode()));
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());

        //退号截止天数（如：就诊前一天为-1，当天为0）
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //预约开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //预约截止时间
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStopTime(stopTime.toDate());



        return scheduleOrderVo;
    }

    //在订单服务中，借助rabbitmq进行更新可预约数量的操作
    @Override
    public void updateAvail(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        //schedule.setAvailableNumber(schedule.getAvailableNumber()-1);
        mongoTemplate.save(schedule);
    }

}
