package com.softkit.controller;

import com.softkit.dto.UserDataDTO;
import com.softkit.dto.UserResponseDTO;
import com.softkit.mapper.UserMapper;
import com.softkit.service.UserService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Api(tags = "users")
@Slf4j
public class UserController {


    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/signin")
    @ApiOperation(value = "${UserController.signin}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 422, message = "Invalid username/password supplied")})
    public String login(
            @ApiParam("Username") @RequestParam String username,
            @ApiParam("Password") @RequestParam String password) {
        return userService.signin(username, password);
    }

    @PostMapping("/signup")
    @ApiOperation(value = "${UserController.signup}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 422, message = "Username is already in use")})
    public String signup(@ApiParam("Signup User") @Valid @RequestBody UserDataDTO user) {
        return userService.signup(userMapper.mapUserDataToUser(user));
    }

    @GetMapping(value = "/me")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
    @ApiOperation(value = "${UserController.me}", response = UserResponseDTO.class, authorizations = {@Authorization(value = "apiKey")})
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public UserResponseDTO whoami(HttpServletRequest req) {
        return userMapper.mapUserToResponse(userService.whoami(req));
    }


    @PostMapping("/delete")
    @ApiOperation(value = "${UserController.delete}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 422, message = "Invalid username")})
    public String delete(@RequestParam String userName){
        userService.delete(userName);
        return "Delete is successful";
    }

    @GetMapping("/search")
    @ApiOperation(value = "${UserController.search}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 422, message = "Invalid username")})
    public UserResponseDTO search(@RequestParam String userName){
        return userMapper.mapUserToResponse(userService.search(userName));
    }

    @PostMapping("/refresh")
    @ApiOperation(value = "${UserController.refresh}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 422, message = "Invalid username")})
    public String refresh(@RequestParam String username){
        return userService.refresh(username);
    }

    @GetMapping("/activation")
    @ApiOperation(value = "${UserController.refresh}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 422, message = "Account is not activated")
    })
    public String activation(@RequestParam String uuid){
        userService.activate(uuid);
        return "Activation is successful";
    }

    @PostMapping(value = "/images",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE} )
    @ApiOperation(value = "${UserController.uploadUserAvatar}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 422, message = "Invalid username")})
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
    public String uploadUserAvatar(
            HttpServletRequest req,
            @RequestBody MultipartFile file) throws IOException {
        userService.uploadImage(req, file);
        return "Images is successful uploaded";
    }

    @GetMapping("/images")
    @ApiOperation(value = "${UserController.loadUserAvatar}")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Something went wrong"),
        @ApiResponse(code = 403, message = "Access denied"),
        @ApiResponse(code = 422, message = "Invalid username")})
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
    public String loadUserAvatar(HttpServletResponse res, @RequestParam String username){
        try {
            res.sendRedirect(userService.loadImages(username));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userService.loadImages(username);
    }

    @PostMapping("/update")
    @ApiOperation(value = "${UserController.updateUserData}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 422, message = "Invalid username")})
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
    public UserResponseDTO updateUserData(HttpServletRequest req,
                                 @RequestParam String firstname,
                                 @RequestParam String lastname){
        return userMapper.mapUserToResponse(userService.updateUserData(req, firstname, lastname));
    }

    @PostMapping("/updateForAdmin")
    @ApiOperation(value = "${UserController.updateUserDataForAdmin}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 422, message = "Invalid username")})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public UserResponseDTO updateUserDataForAdmin(@RequestParam String username,
                                          @RequestParam String firstname,
                                          @RequestParam String lastname){
        return userMapper.mapUserToResponse(userService.updateUserDataForAdmin(username, firstname, lastname));
    }

}
