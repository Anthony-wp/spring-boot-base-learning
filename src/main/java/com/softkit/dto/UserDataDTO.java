package com.softkit.dto;

import com.softkit.annotation.ValidPassword;
import com.softkit.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDataDTO {

    private String username;
    @Size(min = 2, max = 50, message = "Minimum first name length: 2 characters")
    @NotBlank
    private String firstName;
    @Size(min = 2, max = 50, message = "Minimum lastname name length: 2 characters")
    @NotBlank
    private String lastName;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Past
    private Calendar birthday;
    @Email(message = "Email not valid")
    private String email;
    @ValidPassword
    private String password;
    private boolean isActivate;
    private String userAvatar;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private List<Role> roles;
}
