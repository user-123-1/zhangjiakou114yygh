package com.hbjy.yygh.hosp.service;

import com.hbjy.yygh.model.hosp.Schedule;
import com.hbjy.yygh.vo.hosp.ScheduleOrderVo;
import com.hbjy.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ScheduleService {
    void save(List<Schedule> scheduleList);



    void remove(String hoscode, String hosScheduleId);

    Map<String, Object> getScheduleRule(long page, long limit, String hoscode, String depcode);

    List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate);

    void removeAllSchedule(String hoscode);

    Set<String> getAllDocName(String hoscode);

    List<Schedule> getScheByNameAndDepcode(String hoscode, String depcode, String docname);

    void deleteDetailSchedule(Schedule schedule);

    Map<String,Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode);

    Schedule getScheduleId(String scheduleId);

    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    //在订单服务中，借助rabbitmq进行更新可预约数量的操作
    void updateAvail(Schedule schedule);
}
