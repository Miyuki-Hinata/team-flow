package com.example.teamflow.service;

import com.example.teamflow.dto.PasswordChangeRequest;
import com.example.teamflow.dto.UserRequest;
import com.example.teamflow.entity.Department;
import com.example.teamflow.entity.PasswordChangeLog;
import com.example.teamflow.entity.User;
import com.example.teamflow.enums.Role;
import com.example.teamflow.exception.BadRequestException;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.repository.DepartmentRepository;
import com.example.teamflow.repository.PasswordChangeLogRepository;
import com.example.teamflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordChangeLogRepository passwordChangeLogRepository;

    // 一覧は論理削除済みを除外して返す。findAll() だと削除したユーザーまで
    // 管理画面や担当者の選択肢に出てしまうため。
    public List<User> getUsers() {
        return userRepository.findByDeletedAtIsNull();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("該当するユーザーがいません id:" + id));
    }

    public User getUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResourceNotFoundException("該当するユーザーがいません loginId: " + loginId));
    }

    // 職種での絞り込みも同様に論理削除済みを除外する
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRoleAndDeletedAtIsNull(role);
    }

    public User createUser(UserRequest request) {
        User user = new User();
        user.setLoginId(request.getLoginId());
        user.setLastName(request.getLastName());
        user.setFirstName(request.getFirstName());
        user.setLastNameKana(request.getLastNameKana());
        user.setFirstNameKana(request.getFirstNameKana());
        user.setEmail(request.getEmail());

        // パスワードは DTO 側で必須にしていない（編集時に空欄を許すため）ので、
        // 新規作成のときだけここで必須チェックする。
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("パスワードを入力してください");
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(request.getRole());
        user.setLevel(request.getLevel());
        user.setUpdatedBy(request.getUpdatedBy());

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("該当する部署がありません"));
            user.setDepartment(department);
        }

        return userRepository.save(user);
    }

    public User updateUser(Long id, UserRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("該当するユーザーがいません id = " + id));

        existingUser.setLoginId(request.getLoginId());
        existingUser.setLastName(request.getLastName());
        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastNameKana(request.getLastNameKana());
        existingUser.setFirstNameKana(request.getFirstNameKana());
        existingUser.setEmail(request.getEmail());

        // パスワードは「空欄なら変更しない」。編集のたびに管理者が本人のパスワードを
        // 再入力させられるのを避けるため。入力があったときだけハッシュし直す。
        // （空欄で encode すると空文字のハッシュで上書きされ、本人がログインできなくなる）
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        existingUser.setRole(request.getRole());
        existingUser.setLevel(request.getLevel());
        existingUser.setUpdatedBy(request.getUpdatedBy());

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("該当する部署がありません"));
            existingUser.setDepartment(department);
        } else {
            existingUser.setDepartment(null);
        }

        return userRepository.save(existingUser);
    }

    public void changePassword(PasswordChangeRequest request) {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = getUserByLoginId(loginId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new BadRequestException("現在のパスワードが正しくありません");
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);

        PasswordChangeLog log = new PasswordChangeLog();
        log.setUser(currentUser);
        log.setChangedAt(LocalDateTime.now());
        passwordChangeLogRepository.save(log);
    }

    public String deleteUser(Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("指定したユーザーが存在しません id = " + id));

        existingUser.setDeletedAt(LocalDateTime.now());
        userRepository.save(existingUser);

        return "user_id: " + id + " 削除しました";
    }
}
