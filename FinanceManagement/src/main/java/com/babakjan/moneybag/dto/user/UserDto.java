package com.babakjan.moneybag.dto.user;

import com.babakjan.moneybag.entity.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private Long id;

    @Size(max = 40)
    private String firstName;

    @Size(max = 40)
    private String lastName;

    @Email
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Size(min = 1, max = 40)
    private String currency;

    private List<Long> accountIds;
}
