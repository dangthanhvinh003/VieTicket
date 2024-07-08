package com.example.VieTicketSystem.service;

import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.repo.GoogleOauthRepo;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Oauth2Service {
        private Map<String, String> oauthDetails;

        public String getAccessToken(String authorizationCode) throws Exception {
                String grantType = "authorization_code";

                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                this.oauthDetails = GoogleOauthRepo.getOauthDetails();
                map.add("client_id", this.oauthDetails.get("clientId"));
                map.add("client_secret", this.oauthDetails.get("clientSecret"));
                map.add("code", authorizationCode);
                map.add("redirect_uri", this.oauthDetails.get("redirectUri"));
                map.add("grant_type", grantType);
                // Log the parameters
                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

                try {
                        ResponseEntity<String> response = restTemplate
                                        .postForEntity("https://oauth2.googleapis.com/token", request, String.class);

                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> responseBody = mapper.readValue(response.getBody(), Map.class);
                        return (String) responseBody.get("access_token");
                } catch (Exception e) {
                        System.err.println("Error during token exchange: " + e.getMessage());
                        throw e;
                }
        }

        public User getUserInfo(String accessToken) throws Exception {
                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", "Bearer " + accessToken);

                HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

                ResponseEntity<Map> response = restTemplate.exchange("https://openidconnect.googleapis.com/v1/userinfo",
                                HttpMethod.GET, entity, Map.class);

                User user = new User();
                user.setFullName((String) response.getBody().get("name"));
                user.setEmail((String) response.getBody().get("email"));
                user.setAvatar((String) response.getBody().get("picture"));
                user.setRole('u');
                return user;
        }
}
