package com.example.teamflow.dto;

import com.example.teamflow.entity.User;
import com.example.teamflow.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String lastName;
    private String firstName;
    private Role role;
    private Long departmentId;
    private String departmentName;

    // EntityからDTOへ変換するファクトリメソッド
    public static UserResponse from(User user) {
        UserResponse dto = new UserResponse();
        dto.id = user.getId();
        dto.lastName = user.getLastName();
        dto.firstName = user.getFirstName();
        dto.role = user.getRole();

        if (user.getDepartment() != null) {
            dto.departmentId = user.getDepartment().getId();
            dto.departmentName = user.getDepartment().getDepartmentName();
        }

        return dto;
    }
}