package com.softkit.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import com.softkit.exception.CustomException;
import com.softkit.model.Invite;
import com.softkit.model.InviteStatus;
import com.softkit.model.User;
import com.softkit.repository.InviteRepository;
import com.softkit.repository.UserRepository;
import com.softkit.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${images.path.string}")
    private String filePathToSaveUserImages;
    @Value("${file.csv.path}")
    private String pathToCsvFile;
    @Value("${bulk.file.upload}")
    private String bulkFileUpload;
    private final String baseUrl = "http://localhost:8080";
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final InviteRepository inviteRepository;
    private final InviteService inviteService;

    public String signin(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            if (!userRepository.findByUsername(username).isActivate()){
                throw new CustomException("Account is not activated", HttpStatus.NOT_ACCEPTABLE);
            }
            return jwtTokenProvider.createToken(username, userRepository.findByUsername(username).getRoles());
        } catch (AuthenticationException e) {
            throw new CustomException("Invalid username/password supplied", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public String signup(User user, String username) {
        if (!username.equals("")){
            Invite invite = inviteRepository.findByEmailAndUser(user.getEmail(), userRepository.findByUsername(username));
            invite.setStatus(InviteStatus.CLOSED);
            inviteRepository.save(invite);
            inviteRepository.deleteByEmailAndStatus(user.getEmail(), InviteStatus.PENDING);
        }
        if (!userRepository.existsByUsernameIgnoreCase(user.getUsername())) {
            if (userRepository.existsByEmailIgnoreCase(user.getEmail())){
                throw new CustomException("Email is already in use", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            String uuid = UUID.randomUUID().toString();
            user.setActivationKey(uuid);
            user.setRegistrationDate(ZonedDateTime.now());
            String url = String.format("%s/users/activation?uuid=%s", baseUrl, uuid);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            emailService.sendMail(user.getEmail(), url, "Registration successful");
            return jwtTokenProvider.createToken(user.getUsername(), user.getRoles());
        } else {
            throw new CustomException("Username is already in use", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public User whoami(HttpServletRequest req) {
        return userRepository.findByUsername(jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(req)));
    }

    @Transactional
    public void delete(String username) {
        if (userRepository.deleteByUsername(username) == 0){
            throw new CustomException("User doesn't exists", HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    @Cacheable(cacheNames = "searchCash", key = "#username")
    public User search(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null){
            return user;
        } else {
            throw new CustomException("User doesn't exists", HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public String refresh(String username) {
        try{
            return jwtTokenProvider.createToken(username, userRepository.findByUsername(username).getRoles());
        } catch (NoSuchElementException e){
            throw new CustomException("User doesn't exists", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Transactional
    public void activate(String uuid){
        if (userRepository.existsByActivationKeyAndIsActivateFalse(uuid)) {
            User user = userRepository.findByActivationKey(uuid);
            user.setActivate(true);
            userRepository.save(user);
        } else {
            throw new CustomException("Identifier doesn't exists or is it already activated", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Transactional
    @CacheEvict(cacheNames = "searchCash")
    public void uploadImage(HttpServletRequest req, MultipartFile file) throws IOException {
        File uploadDir = new File(filePathToSaveUserImages);
        if (!new File(filePathToSaveUserImages).exists()){
            uploadDir.mkdir();
        }
        User user = userRepository.findByUsername(jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(req)));
        String[] arr = file.getOriginalFilename().split("\\.(?=[^\\.]+$)");
        String fileName = String.format("%s_%d.%s", arr[0], user.getId(), arr[1]);
        File dest = new File(filePathToSaveUserImages + "/" + fileName);
        file.transferTo(dest);
        user.setUserAvatar(String.format("%s/users/images/%s", baseUrl, fileName));
        userRepository.save(user);
    }

    @Transactional
    public String loadImages(String username){
        return userRepository.findByUsername(username).getUserAvatar();
    }

    @Transactional
    @CacheEvict(cacheNames = "searchCash")
    public User updateUserData(HttpServletRequest req, String firstname, String lastname){
        User user = userRepository.findByUsername(jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(req)));
        user.setFirstName(firstname);
        user.setLastName(lastname);
        userRepository.save(user);
        return user;
    }

    @Transactional
    @CacheEvict(cacheNames = "searchCash")
    public User updateUserDataForAdmin(String username, String firstname, String lastname){
        User user = userRepository.findByUsername(username);
        user.setFirstName(firstname);
        user.setLastName(lastname);
        userRepository.save(user);
        return user;
    }

    @Transactional
    public String changeEmail(HttpServletRequest req, String email){
        if (userRepository.existsByEmail(email)){
            throw new CustomException("User with this email is already registered", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        User user = userRepository.findByUsername(jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(req)));
        String url = String.format("%s/users/activationNewEmail?id=%s&email=%s", baseUrl, user.getId(), email);
        emailService.sendMail(email, url, "Changing email");
        user.setUnconfirmedEmail(email);
        userRepository.save(user);
        return "Confirmation message sent to new email";
    }

    @Transactional
    public String activationNewEmail(String id, String email){
        if (userRepository.existsByEmail(email)){
            throw new CustomException("User with this email is already registered", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        User user = userRepository.findById(id);
        if (!user.getUnconfirmedEmail().equals(email)){
            throw new CustomException("Emails don't match", HttpStatus.NOT_FOUND);
        }
        user.setEmail(email);
        user.setUnconfirmedEmail(null);
        userRepository.save(user);
        return "Changing email is successful";
    }

    public byte[] exportToCsv() throws IOException{
        String[] users = userRepository.findAllExceptPassword();
        List<String[]> csvData = new ArrayList<>();
        for (String user : users){
            csvData.add(user.split(","));
        }
        try(CSVWriter writer = new CSVWriter(new FileWriter(pathToCsvFile + "/Users.csv"))){
            writer.writeAll(csvData);
        }

        InputStream in = new FileInputStream(pathToCsvFile + "/Users.csv");
        return IOUtils.toByteArray(in);
    }

    public String bulkUpload(HttpServletRequest req, MultipartFile file) throws IOException, CsvValidationException {
        if (!file.getOriginalFilename().split("\\.(?=[^\\.]+$)")[1].equals("csv")){
            throw new CustomException("Unsuitable file type/must be csv type", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        int existsUsers = 0;
        int inviteUsers = 0;
        File dest = new File(bulkFileUpload + "/Users.csv");
        file.transferTo(dest);
        try (CSVReader csvReader = new CSVReader(new FileReader(dest))){
            String[] values;
            EmailValidator emailValidator = new EmailValidator();
            while ((values = csvReader.readNext()) != null){
                for (String email : values){
                    if (!emailValidator.isValid(email, null)){
                        continue;
                    }
                    if (userRepository.existsByEmail(email)) {
                        existsUsers++;
                        continue;
                    }
                    inviteService.sendInvite(req, email);
                    inviteUsers++;
                }
            }
        }
        return String.format("Users who were able to invite: %d; Users who were already in the system: %d", inviteUsers, existsUsers);
    }

}
