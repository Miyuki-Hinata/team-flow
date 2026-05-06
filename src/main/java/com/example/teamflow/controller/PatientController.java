package com.example.teamflow.controller;

import com.example.teamflow.dto.PatientRequest;
import com.example.teamflow.entity.Patient;
import com.example.teamflow.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PatientController {
    @Autowired
    private PatientService patientService;

    @GetMapping("/api/patients")
    public List<Patient> getPatients() {
        return patientService.getPatients();
    }

    @GetMapping("/api/patients/{id}")
    public Patient getPatientById(@PathVariable Long id) {
        return patientService.getPatientById(id);
    }

    @PostMapping("/api/patients")
    public Patient createPatient(@Valid @RequestBody PatientRequest request) {
        return patientService.createPatient(request);
    }

    @PutMapping("/api/patients/{id}")
    public Patient updatePatient(@PathVariable Long id, @Valid @RequestBody PatientRequest request) {
        return patientService.updatePatient(id, request);
    }

    @DeleteMapping("/api/patients/{id}")
    public String deletePatient(@PathVariable Long id) {
        return patientService.deletePatient(id);
    }
}

//
//// patients
//GET    /api/patients          → 一般ユーザーOK
//GET    /api/patients/{id}     → 一般ユーザーOK
//POST   /api/patients          → 管理者のみ
//PUT    /api/patients/{id}     → 一般ユーザーOK
//DELETE /api/patients/{id}     → 管理者のみ
