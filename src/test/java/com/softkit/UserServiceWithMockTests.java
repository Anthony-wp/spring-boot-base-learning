package com.softkit;


import com.softkit.exception.CustomException;
import com.softkit.model.User;
import com.softkit.repository.InviteRepository;
import com.softkit.repository.UserRepository;
import com.softkit.service.EmailService;
import com.softkit.service.UserService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {StarterApplication.class})
@ImportAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        TransactionAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class})
public class UserServiceWithMockTests {

    @Autowired
    private UserService userService;

    //    mocking all repository calls
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private InviteRepository inviteRepository;
    @MockBean
    private EmailService emailService;


    /**
     * this method is mocking call to database, to simulate that user already exists
     * even if database is not set (so we don't have it up and running)
     * so you can change return value to false and then it will try to signup
     */
    @Test
    public void checkThatWithMockingWeCantDoEvenAFirstSignup() {

        String username = "username";
//        always exists
        Mockito.doReturn(true).when(this.userRepository).existsByUsername(Mockito.eq(username));

        try {
//        username must be the same, because of our above rule for mocking
            userService.signup(new User(1, "test", "test", "test",
                    new GregorianCalendar(2001, Calendar.JANUARY, 17) , "test", "test", UUID.randomUUID().toString(), false, null, ZonedDateTime.now(),
                    Lists.newArrayList(), null), "");
        } catch (CustomException e) {
            assertThat(e.getMessage()).isEqualTo("Username is already in use");
            assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        }

    }


}
