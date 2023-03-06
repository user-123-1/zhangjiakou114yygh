package com.hbjy.yygh.hosp.repository;

import com.hbjy.yygh.model.hosp.Department;
import com.hbjy.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HospitalRepository extends MongoRepository<Hospital,String> {
}
