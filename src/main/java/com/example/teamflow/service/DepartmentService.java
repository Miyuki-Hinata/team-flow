package com.example.teamflow.service;

import com.example.teamflow.entity.Department;
import com.example.teamflow.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<Department> getDepartment() {
        return departmentRepository.findAll();
    }

    public Department getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("該当する部署が登録されていません " + id));
        return department;
    }

    public Department createDepartment(Department department) {
        Department savedDepartment = departmentRepository.save(department);

        return savedDepartment;
    }

    public Department updateDepartment(Long id, Department department) {
        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("指定した部署が見つかりませんでした id: " + id ));

        // データ更新
        existingDepartment.setName(department.getName());

        return departmentRepository.save(existingDepartment);
    }

    public String deleteDepartment(Long id) {
        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("指定した部署が見つかりませんでした id: " + id ));

        existingDepartment.setDeletedAt(LocalDateTime.now());
        departmentRepository.save(existingDepartment);

        return "部署id: " + id + " 削除しました";
    }
}
