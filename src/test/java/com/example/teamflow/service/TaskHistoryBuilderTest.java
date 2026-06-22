package com.example.teamflow.service;

import com.example.teamflow.dto.TaskRequest;
import com.example.teamflow.entity.Task;
import com.example.teamflow.entity.TaskHistory;
import com.example.teamflow.entity.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.teamflow.enums.Priority.MEDIUM;
import static com.example.teamflow.enums.TaskStatus.CREATED;
import static org.junit.jupiter.api.Assertions.assertEquals;

// TaskHistoryBuilder の差分検知ロジックを検証するテスト
class TaskHistoryBuilderTest {

    @Test
    void タイトルを変更したら履歴が1件作られる() {
        // === 準備（Arrange）===
        TaskHistoryBuilder builder = new TaskHistoryBuilder();

        // 変更前のタスク：タイトルが「古いタイトル」
        Task task = new Task();
        task.setTitle("古いタイトル");

        // 変更リクエスト：タイトルを「新しいタイトル」に変える
        TaskRequest request = new TaskRequest();
        request.setTitle("新しいタイトル");
        request.setPriority(MEDIUM);
        request.setTaskStatus(CREATED);

        // 誰が変更したか（中身は何でもよい）
        User changedBy = new User();

        // === 実行（Act）===
        List<TaskHistory> histories = builder.buildHistories(
                task, request, null, null, null, null, changedBy);

        // === 検証（Assert）===
        // タイトルが変わったので、履歴は1件できているはず
        assertEquals(1, histories.size());
    }

    @Test
    void 何も変えなければ履歴は0件() {
        TaskHistoryBuilder builder = new TaskHistoryBuilder();

        Task task = new Task();
        task.setTitle("タスクタイトル");

        TaskRequest request = new TaskRequest();
        request.setTitle("タスクタイトル");
        request.setPriority(MEDIUM);
        request.setTaskStatus(CREATED);

        User changedUser = new User();

        List<TaskHistory> histories = builder.buildHistories(task, request, null,null,null,null,changedUser);

        assertEquals(0, histories.size());
    }
}