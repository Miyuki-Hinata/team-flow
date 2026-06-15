package com.example.teamflow.service;

import com.example.teamflow.entity.Patient;
import com.example.teamflow.entity.TaskSummary;
import com.example.teamflow.entity.User;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.repository.PatientRepository;
import com.example.teamflow.repository.TaskSummaryRepository;
import com.example.teamflow.repository.UserRepository;
import com.example.teamflow.service.llm.LlmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TaskSummaryService {

    @Autowired
    private TaskSummaryRepository taskSummaryRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LlmService llmService;  // ← インターフェース型！中身はモックでも本物でもOK

    /**
     * 患者IDから既存のサマリを取得（キャッシュ表示用）
     */
    public TaskSummary getSummary(Long patientId) {
        return taskSummaryRepository.findByPatient_Id(patientId)
                .orElse(null);
    }

    /**
     * 患者IDに対してサマリを新規生成 or 更新
     */
    public TaskSummary generateSummary(Long patientId) {
        // 1. 患者を取得
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("該当する患者がありません id: " + patientId));

        // 2. ログインユーザーを取得（generatedBy 用）
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));

        // 3. LLM でサマリ生成（プロンプトは仮、後で正式実装）
        String prompt = "タスクサマリを生成してください";
        String generatedText = llmService.generateSummary(prompt);

        // 4. 既存サマリがあれば取得、なければ新規作成
        TaskSummary summary = taskSummaryRepository.findByPatient_Id(patientId)
                .orElse(new TaskSummary());

        // 5. フィールドを更新
        summary.setPatient(patient);
        summary.setSummary(generatedText);
        summary.setGeneratedAt(LocalDateTime.now());
        summary.setGeneratedBy(currentUser);

        // 6. DBに保存（既存なら UPDATE、新規なら INSERT）
        return taskSummaryRepository.save(summary);
    }
}