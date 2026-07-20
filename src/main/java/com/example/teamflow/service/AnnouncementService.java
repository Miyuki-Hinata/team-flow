package com.example.teamflow.service;

import com.example.teamflow.dto.AnnouncementRequest;
import com.example.teamflow.dto.AnnouncementResponse;
import com.example.teamflow.entity.Announcement;
import com.example.teamflow.entity.AnnouncementHistory;
import com.example.teamflow.entity.Category;
import com.example.teamflow.entity.Department;
import com.example.teamflow.entity.Project;
import com.example.teamflow.entity.User;
import com.example.teamflow.dto.UserResponse;
import com.example.teamflow.exception.ForbiddenException;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.repository.AnnouncementHistoryRepository;
import com.example.teamflow.repository.AnnouncementRepository;
import com.example.teamflow.repository.CategoryRepository;
import com.example.teamflow.repository.DepartmentRepository;
import com.example.teamflow.repository.ProjectRepository;
import com.example.teamflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnnouncementHistoryRepository announcementHistoryRepository;

    public List<AnnouncementResponse> getAnnouncements(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));
        Long userDepartmentId = user.getDepartment() != null ? user.getDepartment().getId() : null;

        return announcementRepository.findByDeletedAtIsNull().stream()
                .filter(announcement -> isVisibleToDepartment(announcement, userDepartmentId))
                .map(announcement -> toResponse(announcement, userId))
                .collect(Collectors.toList());
    }

    private boolean isVisibleToDepartment(Announcement announcement, Long userDepartmentId) {
        return announcement.getDepartment() == null
                || (userDepartmentId != null && announcement.getDepartment().getId().equals(userDepartmentId));
    }

    public List<AnnouncementResponse> getMyAnnouncements(Long userId) {
        return announcementRepository.findByCreatedBy_IdAndDeletedAtIsNull(userId).stream()
                .map(announcement -> toResponse(announcement, userId))
                .collect(Collectors.toList());
    }

    private AnnouncementResponse toResponse(Announcement announcement, Long userId) {
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
        response.setCreatedBy(announcement.getCreatedBy() != null ? UserResponse.from(announcement.getCreatedBy()) : null);
        // BaseEntity 由来の作成日時をコピー。フロントで「M/D H:mm」形式で表示する
        response.setCreatedAt(announcement.getCreatedAt());
        return response;
    }

    public AnnouncementResponse getAnnouncementById(Long id, Long userId) {
        Announcement announcement = announcementRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("該当するお知らせがありません id:" + id));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));
        Long userDepartmentId = user.getDepartment() != null ? user.getDepartment().getId() : null;

        boolean isOwner = announcement.getCreatedBy() != null
                && announcement.getCreatedBy().getId().equals(userId);

        if (!isVisibleToDepartment(announcement, userDepartmentId) && !isOwner && !user.isAdmin()) {
            throw new ResourceNotFoundException("該当するお知らせがありません id:" + id);
        }

        return toResponse(announcement, userId);
    }

    public Announcement createAnnouncement(AnnouncementRequest request) {
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setDescription(request.getDescription());
        announcement.setPriority(request.getPriority());
        announcement.setExpiredAt(request.getExpiredAt());
        announcement.setCreatedBy(getCurrentUser());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("該当するカテゴリーがありません"));

        announcement.setCategory(category);

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("該当する部署がありません"));

            announcement.setDepartment(department);
        }

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

        User changedBy = getCurrentUser();

        boolean isOwner = existingAnnouncement.getCreatedBy() != null
                && existingAnnouncement.getCreatedBy().getId().equals(changedBy.getId());

        if (!isOwner && !changedBy.isAdmin()) {
            throw new ForbiddenException("このお知らせを編集する権限がありません");
        }

        Category newCategory = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("該当するカテゴリーがありません"));

        Department newDepartment = request.getDepartmentId() != null
                ? departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("該当する部署がありません"))
                : null;

        Project newProject = request.getProjectId() != null
                ? projectRepository.findById(request.getProjectId())
                        .orElseThrow(() -> new ResourceNotFoundException("該当するプロジェクトがありません"))
                : null;

        List<AnnouncementHistory> histories = buildHistories(existingAnnouncement, request, newProject, newCategory, newDepartment, changedBy);

        existingAnnouncement.setTitle(request.getTitle());
        existingAnnouncement.setDescription(request.getDescription());
        existingAnnouncement.setPriority(request.getPriority());
        existingAnnouncement.setExpiredAt(request.getExpiredAt());
        existingAnnouncement.setCategory(newCategory);
        existingAnnouncement.setDepartment(newDepartment);
        existingAnnouncement.setProject(newProject);

        Announcement savedAnnouncement = announcementRepository.save(existingAnnouncement);

        if (!histories.isEmpty()) {
            announcementHistoryRepository.saveAll(histories);
        }

        return savedAnnouncement;
    }

    private List<AnnouncementHistory> buildHistories(Announcement announcement, AnnouncementRequest request,
                                                       Project newProject, Category newCategory,
                                                       Department newDepartment, User changedBy) {
        List<AnnouncementHistory> histories = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        record(histories, announcement, changedBy, now, "タイトル",
                announcement.getTitle(), request.getTitle());

        record(histories, announcement, changedBy, now, "詳細",
                announcement.getDescription(), request.getDescription());

        record(histories, announcement, changedBy, now, "優先度",
                announcement.getPriority() != null ? announcement.getPriority().name() : null,
                request.getPriority() != null ? request.getPriority().name() : null);

        record(histories, announcement, changedBy, now, "掲載終了日時",
                announcement.getExpiredAt() != null ? announcement.getExpiredAt().toString() : null,
                request.getExpiredAt() != null ? request.getExpiredAt().toString() : null);

        record(histories, announcement, changedBy, now, "プロジェクト",
                announcement.getProject() != null ? announcement.getProject().getProjectName() : null,
                newProject != null ? newProject.getProjectName() : null);

        record(histories, announcement, changedBy, now, "カテゴリー",
                announcement.getCategory() != null ? announcement.getCategory().getCategoryName() : null,
                newCategory != null ? newCategory.getCategoryName() : null);

        record(histories, announcement, changedBy, now, "部署",
                announcement.getDepartment() != null ? announcement.getDepartment().getDepartmentName() : null,
                newDepartment != null ? newDepartment.getDepartmentName() : null);

        return histories;
    }

    private void record(List<AnnouncementHistory> histories, Announcement announcement, User changedBy,
                         LocalDateTime changedAt, String fieldName,
                         String oldValue, String newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            AnnouncementHistory h = new AnnouncementHistory();
            h.setAnnouncement(announcement);
            h.setChangedBy(changedBy);
            h.setChangedAt(changedAt);
            h.setFieldName(fieldName);
            h.setOldValue(oldValue);
            h.setNewValue(newValue);
            histories.add(h);
        }
    }

    public String deleteAnnouncement(long id) {
        Announcement existingAnnouncement = announcementRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("該当するお知らせがありません id: " + id));

        User currentUser = getCurrentUser();
        boolean isOwner = existingAnnouncement.getCreatedBy() != null
                && existingAnnouncement.getCreatedBy().getId().equals(currentUser.getId());

        if (!isOwner && !currentUser.isAdmin()) {
            throw new ForbiddenException("このお知らせを削除する権限がありません");
        }

        existingAnnouncement.setDeletedAt(LocalDateTime.now());
        announcementRepository.save(existingAnnouncement);
        return "announcement_id: " + id  + "を削除しました";
    }

    private User getCurrentUser() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));
    }
}
