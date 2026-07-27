package com.example.teamflow.service;

import com.example.teamflow.entity.Department;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<Department> getDepartments() {
        // 論理削除済み（deletedAt あり）は一覧に出さない
        return departmentRepository.findByDeletedAtIsNull();
    }

    public Department getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("該当する部署が登録されていません " + id));
        return department;
    }

    public Department createDepartment(Department department) {
        Department savedDepartment = departmentRepository.save(department);

        return savedDepartment;
    }

    public Department updateDepartment(Long id, Department department) {
        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("指定した部署が見つかりませんでした id: " + id ));

        // データ更新
        existingDepartment.setDepartmentName(department.getDepartmentName());

        return departmentRepository.save(existingDepartment);
    }

    public String deleteDepartment(Long id) {
        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("指定した部署が見つかりませんでした id: " + id ));

        existingDepartment.setDeletedAt(LocalDateTime.now());
        departmentRepository.save(existingDepartment);

        return "部署id: " + id + " 削除しました";
    }
}
