package com.softkit.dto;

import com.softkit.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private Integer id;
    private String username;
    private String firstName;
    private String lastName;
    private Calendar birthday;
    private String email;
    private String userAvatar;
    private boolean isActivate;
    private ZonedDateTime registrationDate;
    private List<Role> roles;


}
