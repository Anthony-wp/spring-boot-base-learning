package com.softkit;

import com.softkit.exception.CustomException;
import com.softkit.model.User;
import com.softkit.service.EmailService;
import com.softkit.service.UserService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


/**
 * This class is testing the service itself
 * and as you can see on the service level we don't have such strict rules (email, password validation, etc... )
 * so all interactions with the application must be through HTTP layer, that's very important to keep data clean
 */

@SpringBootTest(classes = {StarterApplication.class})
public class UserServiceTests {

    @Autowired
    private UserService userService;
    @MockBean
    private EmailService emailService;

    @Test
    public void successUserSignupTest() {
        String signupToken1 = userService.signup(new User(null, "test", "test", "test",
                new GregorianCalendar(2001, Calendar.JANUARY, 17) , "test", "test", UUID.randomUUID().toString(), false, null, ZonedDateTime.now(),
                Lists.newArrayList()));

        assertThat(signupToken1).isNotBlank();

        try {
            User user = new User(null, "test", "test", "test",
                    new GregorianCalendar(2001, Calendar.JANUARY, 17) , "test", "test", UUID.randomUUID().toString(), false, null, ZonedDateTime.now(),
                    Lists.newArrayList());
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
                Lists.newArrayList());
        String token = userService.signup(user);
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
                new GregorianCalendar(2001, Calendar.JANUARY, 17) , "test", "test", UUID.randomUUID().toString(), false, null, ZonedDateTime.now(),
                Lists.newArrayList());
        String token = userService.signup(user);
        User searchUser = userService.search(user.getUsername());
        assertThat(searchUser.getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    public void successRefreshTokenUserWhichIsSignin(){
        User user = new User(null, "test", "test", "test",
                new GregorianCalendar(2001, Calendar.JANUARY, 17) , "test", "test", UUID.randomUUID().toString(), false, null, ZonedDateTime.now(),
                Lists.newArrayList());
        userService.signup(user);
        String token = userService.refresh("test");

        assertThat(token).isNotBlank();
    }

//    @Test
//    public void successUpdateUserData(){
//        User user = new User(null, "test", "test", "test",
//                new GregorianCalendar(2001, Calendar.JANUARY, 17) , "test", "test", UUID.randomUUID().toString(), true, null, ZonedDateTime.now(),
//                Lists.newArrayList());
//        System.out.println(userService.signup(user));
//        String token = userService.signin("test", "test");
//        User newUser = userService.updateUserData(token, "newFirstName", "newLastName");
//
//        assertThat(newUser.getFirstName()).isEqualTo("newFirstName");
//        assertThat(newUser.getLastName()).isEqualTo("newLastName");
//    }



}
