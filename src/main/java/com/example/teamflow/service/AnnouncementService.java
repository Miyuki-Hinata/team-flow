package com.example.teamflow.service;

import com.example.teamflow.dto.AnnouncementRequest;
import com.example.teamflow.dto.AnnouncementResponse;
import com.example.teamflow.entity.Announcement;
import com.example.teamflow.entity.Category;
import com.example.teamflow.entity.Department;
import com.example.teamflow.entity.Project;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.repository.AnnouncementRepository;
import com.example.teamflow.repository.CategoryRepository;
import com.example.teamflow.repository.DepartmentRepository;
import com.example.teamflow.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {
    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private AnnouncementReadService announcementReadService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public List<AnnouncementResponse> getAnnouncements(Long userId) {
        return announcementRepository.findByDeletedAtIsNull().stream()
                .map(announcement -> {
                    AnnouncementResponse response = new AnnouncementResponse();
                    response.setId(announcement.getId());
                    response.setTitle(announcement.getTitle());
                    response.setDescription(announcement.getDescription());
                    response.setProject(announcement.getProject());
                    response.setCategory(announcement.getCategory());
                    response.setDepartment(announcement.getDepartment());
                    response.setPriority(announcement.getPriority());
                    response.setExpiredAt(announcement.getExpiredAt());
                    response.setIsRead(announcementReadService.isRead(announcement.getId(), userId));
                    return response;
                })
                .collect(Collectors.toList());
    }

    public Announcement getAnnouncementById(Long id) {
        return announcementRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("該当するお知らせがありません id:" + id));
    }

    public Announcement createAnnouncement(AnnouncementRequest request) {
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setDescription(request.getDescription());
        announcement.setPriority(request.getPriority());
        announcement.setExpiredAt(request.getExpiredAt());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("該当するカテゴリーがありません"));

        announcement.setCategory(category);

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("該当する部署がありません"));

        announcement.setDepartment(department);

        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("該当するプロジェクトがありません"));

            announcement.setProject(project);
        }

        return announcementRepository.save(announcement);
    }

    public Announcement updateAnnouncement(Long id, AnnouncementRequest request) {
        Announcement existingAnnouncement = announcementRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("該当するお知らせがありません id: "  + id));

        existingAnnouncement.setTitle(request.getTitle());
        existingAnnouncement.setDescription(request.getDescription());
        existingAnnouncement.setPriority(request.getPriority());
        existingAnnouncement.setExpiredAt(request.getExpiredAt());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("該当するカテゴリーがありません"));
        existingAnnouncement.setCategory(category);

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("該当する部署がありません"));
        existingAnnouncement.setDepartment(department);

        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("該当するプロジェクトがありません"));
            existingAnnouncement.setProject(project);
        } else {
            existingAnnouncement.setProject(null);
        }

        return announcementRepository.save(existingAnnouncement);
    }

    public String deleteAnnouncement(long id) {
        Announcement existingAnnouncement = announcementRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("該当するお知らせがありません id: " + id));

        existingAnnouncement.setDeletedAt(LocalDateTime.now());
        announcementRepository.save(existingAnnouncement);
        return "announcement_id: " + id  + "を削除しました";
    }
}