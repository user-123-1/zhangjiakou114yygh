package com.hbjy.yygh.user.service;

import com.hbjy.yygh.model.user.Patient;

import java.util.List;

public interface PatientService {
    List<Patient> findAllUserId(Long userId);

    void save(Patient patient);

    Patient getPatientById(Long id);

    void updateById(Patient patient);

    void removeById(Long id);
}
