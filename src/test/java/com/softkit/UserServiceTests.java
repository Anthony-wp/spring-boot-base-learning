package com.softkit;

import com.softkit.exception.CustomException;
import com.softkit.model.Invite;
import com.softkit.model.InviteStatus;
import com.softkit.model.User;
import com.softkit.repository.InviteRepository;
import com.softkit.service.EmailService;
import com.softkit.service.InviteService;
import com.softkit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


/**
 * This class is testing the service itself
 * and as you can see on the service level we don't have such strict rules (email, password validation, etc... )
 * so all interactions with the application must be through HTTP layer, that's very important to keep data clean
 */

@Slf4j
@SpringBootTest(classes = {StarterApplication.class})
public class UserServiceTests {

    @Autowired
    private UserService userService;
    @MockBean
    private EmailService emailService;
    @Autowired
    private InviteService inviteService;
    @Autowired
    private InviteRepository inviteRepository;
    @Value("${file.csv.path}")
    private String pathToCsvFile;


    @Test
    public void successUserSignupTest() {
        String signupToken1 = userService.signup(new User(null, "test", "test", "test",
                new GregorianCalendar(2001, Calendar.JANUARY, 17) , "test", "test", UUID.randomUUID().toString(), false, null, ZonedDateTime.now(),
                Lists.newArrayList(), null), "");

        assertThat(signupToken1).isNotBlank();

        try {
            User user = new User(null, "test", "test", "test",
                    new GregorianCalendar(2001, Calendar.JANUARY, 17) , "test", "test", UUID.randomUUID().toString(), false, null, ZonedDateTime.now(),
                    Lists.newArrayList(), null);
            userService.signup(user, "");
        } catch (CustomException e) {
            assertThat(e.getMessage()).isEqualTo("Username is already in use");
            assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Test
    public void successDeleteUserWhichIsNotFound(){
        try {
            userService.delete("test");
        } catch (CustomException e){
            assertThat(e.getMessage()).isEqualTo("User doesn't exists");
            assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Test
    public void successDeleteUserWhichIsSignup(){
        User user = new User(null, "test", "test", "test",
                new GregorianCalendar(2001, Calendar.JANUARY, 17) , "test", "test", UUID.randomUUID().toString(), false, null, ZonedDateTime.now(),
                Lists.newArrayList(), null);
        String token = userService.signup(user, "");
        assertThat(token).isNotBlank();
        userService.delete(user.getUsername());
    }

    @Test
    public void successSearchUserWhichIsNotYetFound(){
        try {
            userService.search("test");
        } catch (CustomException e){
            assertThat(e.getMessage()).isEqualTo("User doesn't exists");
            assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Test
    public void successSearchUserWhichIsSignup(){
        User user = new User(null, "test", "test", "test",
                new GregorianCalendar(2001, Calendar.JANUARY, 17) , "toxa17012001@gmail.com", "test", UUID.randomUUID().toString(), false, null, ZonedDateTime.now(),
                Lists.newArrayList(), null);
        String token = userService.signup(user, "");
        User searchUser = userService.search(user.getUsername());
        assertThat(searchUser.getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    public void successRefreshTokenUserWhichIsSignin(){
        User user = new User(null, "test", "test", "test",
                new GregorianCalendar(2001, Calendar.JANUARY, 17) , "test", "test", UUID.randomUUID().toString(), false, null, ZonedDateTime.now(),
                Lists.newArrayList(), null);
        userService.signup(user, "");
        String token = userService.refresh("test");

        assertThat(token).isNotBlank();
    }

    @Test
    @Transactional
    public void correctSortingOfInvitations(){
        User user = new User(1, "test", "test", "test",
                new GregorianCalendar(2001, Calendar.JANUARY, 17) , "test", "test", UUID.randomUUID().toString(), false, null, ZonedDateTime.now(),
                Lists.newArrayList(), null);
        userService.signup(user, "");
        inviteRepository.save(new Invite(1, "test1@gmial.com", user,  ZonedDateTime.now(), InviteStatus.PENDING));
        inviteRepository.save(new Invite(2, "test2@gmial.com", user,  ZonedDateTime.now(), InviteStatus.PENDING));
        inviteRepository.save(new Invite(3, "test3@gmial.com", user,  ZonedDateTime.now(), InviteStatus.PENDING));
        inviteRepository.save(new Invite(4, "test4@gmial.com", user,  ZonedDateTime.now(), InviteStatus.PENDING));

        Page<Invite> invites = inviteService.allInvites(Pageable.unpaged());
        List<Invite> list = invites.getContent();
        assertThat(list.get(0).getId()).isEqualTo(4);

        Invite invite = inviteRepository.getOne(2);
        invite.setStatus(InviteStatus.CLOSED);
        inviteRepository.save(invite);

        invites = inviteService.allInvites(Pageable.unpaged());
        list = invites.getContent();
        assertThat(list.get(0).getId()).isEqualTo(2);
    }

    @Test
    public void paginationTest(){
        User user = new User(1, "test", "test", "test",
                new GregorianCalendar(2001, Calendar.JANUARY, 17) , "test", "test", UUID.randomUUID().toString(), false, null, ZonedDateTime.now(),
                Lists.newArrayList(), null);
        userService.signup(user, "");
        inviteRepository.save(new Invite(1, "test1@gmial.com", user,  ZonedDateTime.now(), InviteStatus.PENDING));
        inviteRepository.save(new Invite(2, "test2@gmial.com", user,  ZonedDateTime.now(), InviteStatus.PENDING));
        inviteRepository.save(new Invite(3, "test3@gmial.com", user,  ZonedDateTime.now(), InviteStatus.PENDING));
        inviteRepository.save(new Invite(4, "test4@gmial.com", user,  ZonedDateTime.now(), InviteStatus.PENDING));

        Page<Invite> invites = inviteService.allInvites(PageRequest.of(3, 3));

        assertThat(invites.getTotalPages()).isEqualTo(2);
    }

    @Test
    public void testExportToCsv() throws IOException {
        User user1 = new User(1, "test1", "test", "test",
                new GregorianCalendar(2001, Calendar.JANUARY, 17) , "test1@gmail.com", "test", UUID.randomUUID().toString(), false, null, ZonedDateTime.now(),
                Lists.newArrayList(), null);
        User user2 = new User(2, "test2", "test", "test",
                new GregorianCalendar(2001, Calendar.JANUARY, 17) , "test2@gmaul.com", "test", UUID.randomUUID().toString(), false, null, ZonedDateTime.now(),
                Lists.newArrayList(), null);
        userService.signup(user1,"");
        userService.signup(user2,"");
        assertThat(userService.exportToCsv()).isNotEmpty();
    }


}
