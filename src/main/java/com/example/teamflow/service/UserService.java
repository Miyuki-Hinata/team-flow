package com.example.teamflow.service;

import com.example.teamflow.entity.User;
import com.example.teamflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<User> getUser() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(()->new RuntimeException("該当するユーザーがいません id:" + id ));
        return user;
    }

    public User createUser(User user) {
        User savedUser = userRepository.save(user);
        return savedUser;
    }

    public User updateUser(Long id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("該当するユーザーがいません id = " + id));

        existingUser.setLoginId(user.getLoginId());
        existingUser.setLastName(user.getLastName());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastNameKana(user.getLastNameKana());
        existingUser.setFirstNameKana(user.getFirstNameKana());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(user.getPassword());
        existingUser.setDepartment(user.getDepartment());
        existingUser.setLevel(user.getLevel());
        existingUser.setUpdatedBy(user.getUpdatedBy());

        return userRepository.save(existingUser);
    }

    public String deleteUser(Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(()->new RuntimeException("指定したユーザーが存在しません id = " + id));

        // 論理削除（正しい実装）
        existingUser.setDeletedAt(LocalDateTime.now());
        userRepository.save(existingUser);

        return "user_id: " + id + " 削除しました";
    }
}