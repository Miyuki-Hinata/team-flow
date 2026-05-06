package com.example.teamflow.service;

import com.example.teamflow.dto.ProjectRequest;
import com.example.teamflow.entity.Department;
import com.example.teamflow.entity.Project;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.repository.DepartmentRepository;
import com.example.teamflow.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<Project> getProjects() {
        return projectRepository.findAll();
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("該当するプロジェクトがありません id: " + id));
    }

    public Project createProject(ProjectRequest request) {
        Project project = new Project();
        project.setProjectName(request.getProjectName());

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("該当する部署がありません"));
        project.setDepartment(department);

        return projectRepository.save(project);
    }

    public Project updateProject(Long id, ProjectRequest request) {
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("該当するプロジェクトがありません id= " + id));

        existingProject.setProjectName(request.getProjectName());

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("該当する部署がありません"));
        existingProject.setDepartment(department);

        return projectRepository.save(existingProject);
    }

    public String deleteProject(Long id) {
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("該当するプロジェクトがありません id: " + id));

        existingProject.setDeletedAt(LocalDateTime.now());
        projectRepository.save(existingProject);

        return "project_id:" + id + " を削除しました";
    }


}


//GET    /api/projects          → 一般ユーザーOK
//GET    /api/projects/{id}     → 一般ユーザーOK
//POST   /api/projects          → 管理者のみ
//PUT    /api/projects/{id}     → 管理者のみ
//DELETE /api/projects/{id}     → 管理者のみ
