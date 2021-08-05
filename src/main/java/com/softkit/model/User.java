package com.softkit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Entity(name = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Size(min = 4, max = 255, message = "Minimum username length: 4 characters")
    @Column(unique = true, nullable = false)
    private String username;

    @Size(min = 2, max = 50, message = "Minimum first name length: 2 characters")
    @Column(nullable = false)
    private String firstName;

    @Size(min = 2, max = 50, message = "Minimum lastname name length: 2 characters")
    @Column(nullable = false)
    private String lastName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Past
    @Column(nullable = false)
    private Calendar birthday;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Size(min = 8, message = "Minimum password length: 8 characters")
    private String password;

    @Column(unique = true)
    private String activationKey;

    @Column
    private boolean isActivate = false;

    @Column
    private String userAvatar;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private ZonedDateTime registrationDate;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Role> roles;


}
