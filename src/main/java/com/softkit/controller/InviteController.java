package com.softkit.controller;

import com.softkit.model.Invite;
import com.softkit.service.InviteService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Email;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/invites")
@Api(tags = "invites")
@Slf4j
public class InviteController {

    private final InviteService inviteService;

    @PostMapping("/sendInvite")
    @ApiOperation(value = "${InviteController.sendInvite}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied")})
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
    public String sendInvite(HttpServletRequest req, @RequestParam @Email String email){
        inviteService.sendInvite(req, email);
        return "Invite is sent";
    }

    @GetMapping("/allInvites")
    @ApiOperation(value = "${InviteController.allInvites}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied")})
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
    public Page<Invite> allInvites(Pageable pageable){
        return inviteService.allInvites(pageable);
    }
}
