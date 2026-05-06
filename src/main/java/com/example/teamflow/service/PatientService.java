package com.example.teamflow.service;

import com.example.teamflow.dto.PatientRequest;
import com.example.teamflow.entity.Department;
import com.example.teamflow.entity.Patient;
import com.example.teamflow.entity.User;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.repository.DepartmentRepository;
import com.example.teamflow.repository.PatientRepository;
import com.example.teamflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PatientService {
    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<Patient> getPatients() {
        return patientRepository.findAll();
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("該当する患者が存在しません id: " + id));
    }

    public Patient createPatient(PatientRequest request) {
        Patient patient = new Patient();
        patient.setLastName(request.getLastName());
        patient.setFirstName(request.getFirstName());
        patient.setLastNameKana(request.getLastNameKana());
        patient.setFirstNameKana(request.getFirstNameKana());
        patient.setBirth(request.getBirth());
        patient.setSex(request.getSex());
        patient.setAddress(request.getAddress());
        patient.setTel(request.getTel());
        patient.setEmergencyContactName(request.getEmergencyContactName());
        patient.setEmergencyContactTel(request.getEmergencyContactTel());
        patient.setUpdatedBy(request.getUpdatedBy());

        if (request.getDoctorId() != null) {
            User doctor = userRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("該当するユーザーがありません"));
            patient.setDoctor(doctor);
        }

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("該当する部署がありません"));
            patient.setDepartment(department);
        }

        return patientRepository.save(patient);
    }

    public Patient updatePatient(Long id, PatientRequest request) {
        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("該当する患者が存在しません id: " + id));

        existingPatient.setLastName(request.getLastName());
        existingPatient.setFirstName(request.getFirstName());
        existingPatient.setLastNameKana(request.getLastNameKana());
        existingPatient.setFirstNameKana(request.getFirstNameKana());
        existingPatient.setBirth(request.getBirth());
        existingPatient.setSex(request.getSex());
        existingPatient.setAddress(request.getAddress());
        existingPatient.setTel(request.getTel());
        existingPatient.setEmergencyContactName(request.getEmergencyContactName());
        existingPatient.setEmergencyContactTel(request.getEmergencyContactTel());
        existingPatient.setUpdatedBy(request.getUpdatedBy());

        if (request.getDoctorId() != null) {
            User doctor = userRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("該当するユーザーがありません"));
            existingPatient.setDoctor(doctor);
        } else {
            existingPatient.setDoctor(null);
        }

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("該当する部署がありません"));
            existingPatient.setDepartment(department);
        } else {
            existingPatient.setDepartment(null);
        }

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
