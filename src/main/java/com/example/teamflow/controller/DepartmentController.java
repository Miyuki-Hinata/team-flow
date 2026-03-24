package com.example.teamflow.controller;

import com.example.teamflow.entity.Department;
import com.example.teamflow.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Department createDepartment(
            @Valid @RequestBody Department department,
            BindingResult bindingResult
    ) {
//        // バリデーションエラーチェック
//        if (bindingResult.hasErrors()) {
//            List<String> errorMessage = bindingResult.getAllErrors()
//                    .stream()
//                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
//                    .toList();
//
//            // ErrorResponse を作成
//            ErrorResponse errorResponse = new ErrorResponse(
//                    400,
//                    "Bad Request",
//                    errorMessage
//            );
//
//
//            return ResponseEntity
//                    .badRequest()
//                    .body(errorResponse);
//        }
        return departmentService.createDepartment(department);
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