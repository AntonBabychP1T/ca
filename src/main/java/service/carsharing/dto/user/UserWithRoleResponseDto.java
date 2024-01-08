package service.carsharing.dto.user;

import lombok.Data;

@Data
public class UserWithRoleResponseDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String[] roles;
}

