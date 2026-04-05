package com.example.teamflow.controller;

import com.example.teamflow.entity.Department;
import com.example.teamflow.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/api/departments")
    public List<Department> getDepartments() {
        return departmentService.getDepartments();
    }

    @GetMapping("/api/departments/{id}")
    public Department getDepartmentById(@PathVariable Long id) {
        return departmentService.getDepartmentById(id);
    }

    @PostMapping("/api/departments")
    public ResponseEntity<Department> createDepartment(
            @Valid @RequestBody Department department,
            BindingResult bindingResult
    ) {
        Department created = departmentService.createDepartment(department);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/api/departments/{id}")
    public Department updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody Department department
    ) {
        return departmentService.updateDepartment(id, department);
    }

    @DeleteMapping("/api/departments/{id}")
    public String deleteDepartment(
            @PathVariable Long id
    ) {
        return departmentService.deleteDepartment(id);
    }
}