package com.example.teamflow.controller;

import com.example.teamflow.entity.Project;
import com.example.teamflow.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProjectController {
    @Autowired
    private ProjectService projectService;

    @GetMapping("/api/projects")
    public List<Project> getProject() {
        return projectService.getProject();
    }

    @GetMapping("/api/projects/{id}")
    public Project getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id);
    }

    @PostMapping("/api/projects")
    public Project createProject(
            @Valid @RequestBody Project project
    ) {
        return projectService.createProject(project);
    }

    @PutMapping("/api/projects/{id}")
    public Project updateProject(
            @PathVariable Long id,
            @RequestBody Project project
    ) {
        return projectService.updateProject(id, project);
    }

    @DeleteMapping("/api/projects/{id}")
    public String deleteProject(
            @PathVariable Long id
    ) {
        return projectService.deleteProject(id);
    }
}


//GET    /api/projects          → 一般ユーザーOK
//GET    /api/projects/{id}     → 一般ユーザーOK
//POST   /api/projects          → 管理者のみ
//PUT    /api/projects/{id}     → 管理者のみ
//DELETE /api/projects/{id}     → 管理者のみ