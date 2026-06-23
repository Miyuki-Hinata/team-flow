package com.example.teamflow.service;

import com.example.teamflow.dto.TaskRequest;
import com.example.teamflow.entity.Project;
import com.example.teamflow.entity.Task;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    // @Mock を使って偽物のRepositoryを作成
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskHistoryRepository taskHistoryRepository;
    @Mock
    private TaskHistoryBuilder taskHistoryBuilder;

    // 本物の TaskService に注入
    @InjectMocks
    private TaskService taskService;

    // 正常系
    @Test
    void プロジェクトを指定してタスクを作成できる() {

        // 1. テストで使う「偽物が返すデータ」を用意
        Project testProject = new Project();
        testProject.setId(1L);

        // 2.　入力となるリクエストを用意
        TaskRequest request = new TaskRequest();
        request.setTitle("テストタスク");
        request.setProjectId(1L);

        // 3. @Mockで作った偽物repositoryは中身は空なので、偽物repositoryへの指示を事前に定義する（createTask の中で呼ばれるRepositoryに、台本を渡す）
        // projectRepository.findById(1L) が呼ばれたら、testProject の入った箱を返す
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        // taskRepository.save(...) が呼ばれたら、保存されたタスクを返
        when(taskRepository.save(any(Task.class))).thenReturn(new Task());

        // 4. 実行（テストしたいメソッド taskService.createTask(request) を呼んで、結果を受け取る。）
        Task createdTask = taskService.createTask(request);

        // 5. 検証
        assertNotNull(createdTask);
        verify(projectRepository).findById(1L);
    }

    @Test
    void プロジェクトを探さずにタスクを作ることができる() {
        TaskRequest request = new TaskRequest();
        request.setTitle("テストタイトル");

        when(taskRepository.save(any(Task.class))).thenReturn(new Task());

        Task createdTask = taskService.createTask(request);

        assertNotNull(createdTask);
        verify(projectRepository, never()).findById(any());
    }

    // 例外系
    @Test
    void 指定したprojectIdが存在しないときは例外を投げることができる() {
        TaskRequest request = new TaskRequest();
        request.setTitle("テストタスク");
        request.setProjectId(1L);


        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.createTask(request);
        });
    }


}
