package com.softkit.service;

import com.softkit.exception.CustomException;
import com.softkit.model.Invite;
import com.softkit.model.InviteStatus;
import com.softkit.model.User;
import com.softkit.repository.InviteRepository;
import com.softkit.repository.UserRepository;
import com.softkit.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final InviteRepository inviteRepository;

    public void sendInvite(HttpServletRequest req, String email){
        if (userRepository.existsByEmail(email)){
            throw new CustomException("User is already registered", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        User user = userRepository.findByUsername(jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(req)));
        if (inviteRepository.existsByEmailAndUser(email, user)){
            throw new CustomException("You have already sent an invitation to this email", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        Invite invite = new Invite();
        invite.setEmail(email);
        invite.setDepartureDate(ZonedDateTime.now());
        invite.setUser(user);
        inviteRepository.save(invite);
    }
}
