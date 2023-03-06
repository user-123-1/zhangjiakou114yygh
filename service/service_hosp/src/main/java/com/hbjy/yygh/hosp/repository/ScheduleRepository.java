package com.hbjy.yygh.hosp.repository;

import com.hbjy.yygh.model.hosp.Department;
import com.hbjy.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends MongoRepository<Schedule,String> {
}
