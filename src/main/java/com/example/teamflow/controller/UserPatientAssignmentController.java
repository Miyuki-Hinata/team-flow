package com.example.teamflow.controller;

import com.example.teamflow.dto.AssignedPatientsRequest;
import com.example.teamflow.entity.Patient;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.repository.UserRepository;
import com.example.teamflow.service.UserPatientAssignmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 受け持ち患者 API。
 *
 * URL に userId を出さず、常に「ログイン中の自分（me）」を対象にする。
 * 誰の受け持ちかは JWT から解決するので、他人の受け持ちを覗く/書き換える経路が生まれない。
 */
@RestController
@RequestMapping("/api/me/assigned-patients")
public class UserPatientAssignmentController {
    @Autowired
    private UserPatientAssignmentService assignmentService;

    @Autowired
    private UserRepository userRepository;

    // 受け持ち患者の一覧を返す
    @GetMapping
    public List<Patient> getAssignedPatients() {
        return assignmentService.getAssignedPatients(getCurrentUserId());
    }

    // 受け持ちを集合置換で更新する。更新後の最新状態を返すので、フロントは再フェッチ不要。
    @PutMapping
    public List<Patient> replaceAssignedPatients(@Valid @RequestBody AssignedPatientsRequest request) {
        Long userId = getCurrentUserId();
        assignmentService.replaceAssignments(userId, request.getPatientIds());
        return assignmentService.getAssignedPatients(userId);
    }

    /**
     * JWT のログインID から現在ユーザーの id を解決する。
     * 既存の TaskController / AnnouncementReadController と同じ方式に揃えている。
     */
    private Long getCurrentUserId() {
        String loginId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"))
                .getId();
    }
}
