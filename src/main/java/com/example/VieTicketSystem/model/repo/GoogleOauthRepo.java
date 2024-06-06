package com.example.VieTicketSystem.model.repo;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public class GoogleOauthRepo {
  public static Map<String, String> getOauthDetails() throws Exception {
    Class.forName(Baseconnection.nameClass);
    Connection connection = ConnectionPoolManager.getConnection();
    PreparedStatement ps = connection.prepareStatement("Select * from googleOauth");
    ResultSet rs = ps.executeQuery();
    if (rs.next()) {
      String clientId = rs.getString("client_id");
      String clientSecret = rs.getString("client_secret");
      String redirectUri = rs.getString("redirect_uri");
      Map<String, String> oauthDetails = new HashMap<>();
      oauthDetails.put("clientId", clientId);
      oauthDetails.put("clientSecret", clientSecret);
      oauthDetails.put("redirectUri", redirectUri);
      ps.close();
      return oauthDetails;
    }
    return null;
  }
}
