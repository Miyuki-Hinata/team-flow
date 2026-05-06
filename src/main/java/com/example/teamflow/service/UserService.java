package com.example.teamflow.service;

import com.example.teamflow.dto.UserRequest;
import com.example.teamflow.entity.Department;
import com.example.teamflow.entity.User;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.repository.DepartmentRepository;
import com.example.teamflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("該当するユーザーがいません id:" + id));
    }

    public User createUser(UserRequest request) {
        User user = new User();
        user.setLoginId(request.getLoginId());
        user.setLastName(request.getLastName());
        user.setFirstName(request.getFirstName());
        user.setLastNameKana(request.getLastNameKana());
        user.setFirstNameKana(request.getFirstNameKana());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
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
        existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
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

    public String deleteUser(Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("指定したユーザーが存在しません id = " + id));

        existingUser.setDeletedAt(LocalDateTime.now());
        userRepository.save(existingUser);

        return "user_id: " + id + " 削除しました";
    }
}
