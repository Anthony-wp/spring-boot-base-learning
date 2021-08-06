package com.softkit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.ZonedDateTime;


@Entity(name = "Invitation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String email;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private ZonedDateTime departureDate;

    @Enumerated(EnumType.STRING)
    private InviteStatus status = InviteStatus.PENDING;
}
