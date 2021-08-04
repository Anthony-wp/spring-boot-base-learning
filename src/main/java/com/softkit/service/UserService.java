package com.softkit.service;

import com.softkit.exception.CustomException;
import com.softkit.model.User;
import com.softkit.repository.UserRepository;
import com.softkit.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${images.path.string}")
    private String filePathToSaveUserImages;
    private final String baseUrl = "http://localhost:8080";
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public String signin(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            if (!userRepository.findByUsername(username).isActivate()){
                throw new CustomException("Account is not activated", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            return jwtTokenProvider.createToken(username, userRepository.findByUsername(username).getRoles());
        } catch (AuthenticationException e) {
            throw new CustomException("Invalid username/password supplied", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public String signup(User user) {
        if (!userRepository.existsByUsernameIgnoreCase(user.getUsername())) {
            if (userRepository.existsByEmailIgnoreCase(user.getEmail())){
                throw new CustomException("Email is already in use", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            UUID uuid = UUID.randomUUID();
            user.setActivationKey(uuid.toString());
            String url = String.format("%s/users/activation?uuid=%s", baseUrl, uuid);
            emailService.sendMail(user.getEmail(), url);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            return jwtTokenProvider.createToken(user.getUsername(), user.getRoles());
        } else {
            throw new CustomException("Username is already in use", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public User whoami(HttpServletRequest req) {
        return userRepository.findByUsername(jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(req)));
    }

    //  method must delete user, by username, throw appropriate exception is user doesn't exists
    public void delete(String username) {
        try{
            userRepository.deleteByUsername(username);
        } catch (Exception e){
            throw new CustomException("User doesn't exists", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    //  method must search user, by username, throw appropriate exception is user doesn't exists
    public User search(String username) {
        try{
            return userRepository.findByUsername(username);
        } catch (Exception e){
            throw new CustomException("User doesn't exists", HttpStatus.NOT_FOUND);
        }
    }

//  method must create a new access token, similar to login
    public String refresh(String username) {
        try{
            return jwtTokenProvider.createToken(username, userRepository.findByUsername(username).getRoles());
        } catch (Exception e){
            throw new CustomException("User doesn't exists", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public void activate(String uuid){
        if (userRepository.existsByActivationKeyAndIsActivateFalse(uuid)) {
            User user = userRepository.findByActivationKey(uuid);
            user.setActivate(true);
            userRepository.save(user);
        } else {
            throw new CustomException("Identifier doesn't exists or is it already activated", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public void uploadImage(String username, MultipartFile file) throws IOException {
        File uploadDir = new File(filePathToSaveUserImages);
        if (!new File(filePathToSaveUserImages).exists()){
            uploadDir.mkdir();
        }
        User user = userRepository.findByUsername(username);
        String[] arr = file.getOriginalFilename().split("\\.(?=[^\\.]+$)");
        String fileName = String.format("%s_%d.%s", arr[0], user.getId(), arr[1]);
        File dest = new File(filePathToSaveUserImages + "/" + fileName);
        file.transferTo(dest);
        user.setUserAvatar(String.format("%s/users/images/%s", baseUrl, fileName));
        userRepository.save(user);
    }

    public String loadImages(String username){
        return userRepository.findByUsername(username).getUserAvatar();
    }
}
