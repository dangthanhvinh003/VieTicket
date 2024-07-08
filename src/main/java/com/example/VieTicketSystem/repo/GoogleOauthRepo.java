package com.example.VieTicketSystem.repo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public class GoogleOauthRepo {
    public static Map<String, String> getOauthDetails() throws Exception {
        Map<String, String> oauthDetails = null;
        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM googleOauth");
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            String clientId = rs.getString("client_id");
            String clientSecret = rs.getString("client_secret");
            String redirectUri = rs.getString("redirect_uri");
            oauthDetails = new HashMap<>();
            oauthDetails.put("clientId", clientId);
            oauthDetails.put("clientSecret", clientSecret);
            oauthDetails.put("redirectUri", redirectUri);
        }

        rs.close();
        ps.close();
        connection.close();

        return oauthDetails;
    }
}
