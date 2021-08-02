package com.softkit;

import com.softkit.dto.UserDataDTO;
import com.softkit.dto.UserResponseDTO;
import com.softkit.model.Role;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserIntegrationControllerTests extends AbstractControllerTest {

    private final String signupUrl = "/users/signup";
    private final String signinUrl = "/users/signin";
    private final String whoamiUrl = "/users/me";
    private final String deleteUrl = "/users/delete";
    private final String searchUrl = "/users/search";
    private final String refreshUrl = "/users/refresh";

    @Test
    public void simpleSignupSuccessTest() {
        String token = this.restTemplate.postForObject(
                getBaseUrl() + signupUrl,
                getValidUserForSignup(),
                String.class);

//        checking that token is ok
        assertThat(token).isNotBlank();
    }

    @Test
    public void signupAgainErrorTest() {
        UserDataDTO userForSignup = getValidUserForSignup();
        String token = this.restTemplate.postForObject(
                getBaseUrl() + signupUrl,
                userForSignup,
                String.class);

//        checking that token is ok
        assertThat(token).isNotBlank();

//        signup same user second time
        ResponseEntity<HashMap<String, Object>> response = this.restTemplate.exchange(
                getBaseUrl() + signupUrl,
                HttpMethod.POST,
                new HttpEntity<>(userForSignup),
                new ParameterizedTypeReference<HashMap<String, Object>>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().get("error")).isEqualTo("Unprocessable Entity");

    }


    @Test
    public void noSuchUserForLogin() {
        ResponseEntity<HashMap<String, Object>> response = this.restTemplate.exchange(
                UriComponentsBuilder.fromHttpUrl(getBaseUrl() + signinUrl)
                        .queryParam("username", "fakeusername")
                        .queryParam("password", "fakepass")
                        .build().encode().toUri(),
                HttpMethod.POST,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<HashMap<String, Object>>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().get("error")).isEqualTo("Unprocessable Entity");
    }


    @Test
    public void sucessSignupAndSignin() {
        UserDataDTO user = getValidUserForSignup();

        String signupToken = this.restTemplate.postForObject(
                getBaseUrl() + signupUrl,
                user,
                String.class);

//        checking that signup token is ok
        assertThat(signupToken).isNotBlank();

        String token = this.restTemplate.postForObject(
                UriComponentsBuilder.fromHttpUrl(getBaseUrl() + signinUrl)
                        .queryParam("username", user.getUsername())
                        .queryParam("password", user.getPassword())
                        .build().encode().toUri(),
                HttpEntity.EMPTY,
                String.class);

//        checking that signin token is ok
        assertThat(token).isNotBlank();

//        set auth headers based on login response
        HttpHeaders headers = new HttpHeaders();
        headers.put("Authorization", Collections.singletonList("Bearer " + token));

//        call /me endpoint to check that user is really authorized  
        ResponseEntity<UserResponseDTO> whoAmIResponse = this.restTemplate.exchange(
                UriComponentsBuilder.fromHttpUrl(getBaseUrl() + whoamiUrl)
                        .build().encode().toUri(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                UserResponseDTO.class);

//        check status code 
        assertThat(whoAmIResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserResponseDTO userDetails = whoAmIResponse.getBody();

//        check that all fields match and id has been set properly
        assertThat(userDetails.getUsername()).isEqualTo(user.getUsername());
        assertThat(userDetails.getEmail()).isEqualTo(user.getEmail());
        assertThat(userDetails.getRoles()).isEqualTo(user.getRoles());
        assertThat(userDetails.getId()).isNotNull();

    }

    @Test
    public void whoAmIWithIncorrectAuthToken() {

//        set auth headers based on login response
        HttpHeaders headers = new HttpHeaders();
        headers.put("Authorization", Collections.singletonList("Bearer " + "Secure (no) token"));

//        call /me endpoint to check that user is really authorized  
        ResponseEntity<UserResponseDTO> whoAmIResponse = this.restTemplate.exchange(
                UriComponentsBuilder.fromHttpUrl(getBaseUrl() + whoamiUrl)
                        .build().encode().toUri(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                UserResponseDTO.class);

//        check status code 
        assertThat(whoAmIResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(whoAmIResponse.getBody()).isNull();
    }

    @Test
    public void deleteUserWhichIsNotYet(){
        UserDataDTO user = getValidUserForSignup();
        String token = this.restTemplate.postForObject(
                getBaseUrl() + signupUrl,
                user,
                String.class);

        HttpHeaders headers = new HttpHeaders();
        headers.put("Authorization", Collections.singletonList("Bearer " + token));


        ResponseEntity<String> response = this.restTemplate.exchange(
                UriComponentsBuilder.fromHttpUrl(getBaseUrl() + deleteUrl)
                        .queryParam("userName", "fakeusername")
                        .build().encode().toUri(),
                HttpMethod.POST,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void deleteRegisteredUser(){
        UserDataDTO user1 = getValidUserForSignup();
        String token1 = this.restTemplate.postForObject(
                getBaseUrl() + signupUrl,
                user1,
                String.class);

        UserDataDTO user2 = getValidUserForSignup();
        String token2 = this.restTemplate.postForObject(
                getBaseUrl() + signupUrl,
                user2,
                String.class);

        HttpHeaders headers = new HttpHeaders();
        headers.put("Authorization", Collections.singletonList("Bearer " + token1));


        ResponseEntity<String> response = this.restTemplate.exchange(
                UriComponentsBuilder.fromHttpUrl(getBaseUrl() + deleteUrl)
                        .queryParam("userName", user2.getUsername())
                        .build().encode().toUri(),
                HttpMethod.POST,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void searchUserWhichIsNotYet(){
        UserDataDTO user = getValidUserForSignup();
        String token = this.restTemplate.postForObject(
                getBaseUrl() + signupUrl,
                user,
                String.class);

        HttpHeaders headers = new HttpHeaders();
        headers.put("Authorization", Collections.singletonList("Bearer " + token));


        ResponseEntity<String> response = this.restTemplate.exchange(
                UriComponentsBuilder.fromHttpUrl(getBaseUrl() + searchUrl)
                        .queryParam("userName", "fakeusername")
                        .build().encode().toUri(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void searchRegisteredUser(){
        UserDataDTO user1 = getValidUserForSignup();
        String token1 = this.restTemplate.postForObject(
                getBaseUrl() + signupUrl,
                user1,
                String.class);

        UserDataDTO user2 = getValidUserForSignup();
        String token2 = this.restTemplate.postForObject(
                getBaseUrl() + signupUrl,
                user2,
                String.class);

        HttpHeaders headers = new HttpHeaders();
        headers.put("Authorization", Collections.singletonList("Bearer " + token1));


        ResponseEntity<String> response = this.restTemplate.exchange(
                UriComponentsBuilder.fromHttpUrl(getBaseUrl() + searchUrl)
                        .queryParam("userName", user2.getUsername())
                        .build().encode().toUri(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void refreshTokenUserWhichIsSignin(){
        UserDataDTO user = getValidUserForSignup();
        String token1 = this.restTemplate.postForObject(
                getBaseUrl() + signupUrl,
                user,
                String.class);

        String token2 = this.restTemplate.postForObject(
                UriComponentsBuilder.fromHttpUrl(getBaseUrl() + signinUrl)
                        .queryParam("username", user.getUsername())
                        .queryParam("password", user.getPassword())
                        .build().encode().toUri(),
                HttpEntity.EMPTY,
                String.class);

        HttpHeaders headers = new HttpHeaders();
        headers.put("Authorization", Collections.singletonList("Bearer " + token2));

        ResponseEntity<String> response = this.restTemplate.exchange(
                UriComponentsBuilder.fromHttpUrl(getBaseUrl() + refreshUrl)
                        .queryParam("username", user.getUsername())
                        .build().encode().toUri(),
                HttpMethod.POST,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void refreshWithoutToken(){

        UserDataDTO user = getValidUserForSignup();

        String token1 = this.restTemplate.postForObject(
                getBaseUrl() + signupUrl,
                user,
                String.class);

        HttpHeaders headers = new HttpHeaders();
        headers.put("Authorization", Collections.singletonList("Bearer " + token1));

        ResponseEntity<String> response = this.restTemplate.exchange(
                UriComponentsBuilder.fromHttpUrl(getBaseUrl() + refreshUrl)
                        .queryParam("username", "fakename")
                        .build().encode().toUri(),
                HttpMethod.POST,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

    }




    private UserDataDTO getValidUserForSignup() {
        UUID randomUUID = UUID.randomUUID();
        return new UserDataDTO(
                randomUUID + "softkit",
                randomUUID + "youremail@softkitit.com",
                randomUUID + "HeisenbuG1!",
                Lists.newArrayList(Role.ROLE_ADMIN, Role.ROLE_CLIENT));
    }



}
