package com.softkit.configuration;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
@Getter
public class EmailConfiguration {
    private final String host;
    private final int port;
    private final String username;
    private final String password;

    public EmailConfiguration(@Value("${email.service.host}") final String host,
                              @Value("${email.service.port}") final int port,
                              @Value("${email.service.username}") final String username,
                              @Value("${email.service.password}") final String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }


}
