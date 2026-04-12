package com.example.teamflow.service;

import com.example.teamflow.entity.Patient;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PatientService {
    @Autowired
    private PatientRepository patientRepository;

    public List<Patient> getPatients() {
        return patientRepository.findAll();
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("該当する患者が存在しません id: " + id));
    }

    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }
    
    public Patient updatePatient(Long id, Patient patient) {
        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("該当する患者が存在しません id: " + id));
        
        existingPatient.setLastName(patient.getLastName());
        existingPatient.setFirstName(patient.getFirstName());
        existingPatient.setLastNameKana(patient.getLastNameKana());
        existingPatient.setFirstNameKana(patient.getFirstNameKana());
        existingPatient.setBirth(patient.getBirth());
        existingPatient.setSex(patient.getSex());
        existingPatient.setAddress(patient.getAddress());
        existingPatient.setTel(patient.getTel());
        existingPatient.setEmergencyContactName(patient.getEmergencyContactName());
        existingPatient.setEmergencyContactTel(patient.getEmergencyContactTel());
        existingPatient.setDoctor(patient.getDoctor());
        existingPatient.setDepartment(patient.getDepartment());
        // TODO: Week9 JWT実装時に SecurityContextHolder から自動取得に変更する
        existingPatient.setUpdatedBy(patient.getUpdatedBy());

        return patientRepository.save(existingPatient);
    }

    public String deletePatient(Long id) {
        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("該当する患者が存在しません id: " + id));

        existingPatient.setDeletedAt(LocalDateTime.now());
        patientRepository.save(existingPatient);

        return "patient_id: " + id + " 削除しました";
    }
}