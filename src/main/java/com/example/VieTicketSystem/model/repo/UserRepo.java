package com.example.VieTicketSystem.model.repo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.VieTicketSystem.model.entity.User;

@Repository
public class UserRepo {

    public void EditImgUser(String img, int id) throws Exception {

        Connection con = ConnectionPoolManager.getConnection();
        PreparedStatement ps = con.prepareStatement("UPDATE User SET avatar = ? WHERE user_id = ?");
        ps.setString(1, img);
        ps.setInt(2, id);
        ps.executeUpdate();
        ps.close();
        con.close();
    }

    public void editProfile(String name, String email, String phone, Date dob, char gender, int user_id)
            throws Exception {

        Connection con = ConnectionPoolManager.getConnection();
        PreparedStatement ps = con.prepareStatement("UPDATE User SET full_name = ?, phone = ?, dob= ?, gender = ?, email = ? WHERE user_id = ?");
        ps.setString(1, name);
        ps.setString(2, phone);
        ps.setDate(3, dob);
        ps.setString(4, String.valueOf(gender));
        ps.setString(5, email);
        ps.setInt(6, user_id);
        ps.executeUpdate();
        ps.close();
        con.close();
    }

    public List<User> findAll() throws Exception {
        Connection con = ConnectionPoolManager.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM User");
        ResultSet rs = ps.executeQuery();

        List<User> users = new ArrayList<>();
        while (rs.next()) {
            User user = new User();
            user.setUserId(rs.getInt("user_id"));
            user.setFullName(rs.getString("full_name"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setPhone(rs.getString("phone"));
            user.setDob(rs.getDate("dob"));
            user.setGender(rs.getString("gender").charAt(0));
            user.setAvatar(rs.getString("avatar"));
            user.setRole(rs.getString("role").charAt(0));
            user.setEmail(rs.getString("email"));
            users.add(user);
        }

        rs.close();
        ps.close();
        con.close();

        return users;
    }

    public User findById(int id) throws Exception {
        Connection con = ConnectionPoolManager.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM User WHERE user_id = ?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        User user = null;
        if (rs.next()) {
            user = new User();
            user.setUserId(rs.getInt("user_id"));
            user.setFullName(rs.getString("full_name"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setPhone(rs.getString("phone"));
            user.setDob(rs.getDate("dob"));
            user.setGender(rs.getString("gender").charAt(0));
            user.setAvatar(rs.getString("avatar"));
            user.setRole(rs.getString("role").charAt(0));
            user.setEmail(rs.getString("email"));
        }

        rs.close();
        ps.close();
        con.close();

        return user;
    }

    public User findByUsername(String email) throws Exception {
        Connection con = ConnectionPoolManager.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM User WHERE username = ?");
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        User user = null;
        if (rs.next()) {
            user = new User();
            user.setUserId(rs.getInt("user_id"));
            user.setFullName(rs.getString("full_name"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setPhone(rs.getString("phone"));
            user.setDob(rs.getDate("dob"));
            user.setGender(rs.getString("gender").charAt(0));
            user.setAvatar(rs.getString("avatar"));
            user.setRole(rs.getString("role").charAt(0));
            user.setEmail(rs.getString("email"));
        }

        rs.close();
        ps.close();
        con.close();

        return user;
    }

    public User findByEmail(String email) throws Exception {
        Connection con = ConnectionPoolManager.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM User WHERE email = ?");
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        User user = null;
        if (rs.next()) {
            user = new User();
            user.setUserId(rs.getInt("user_id"));
            user.setFullName(rs.getString("full_name"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setPhone(rs.getString("phone"));
            user.setDob(rs.getDate("dob"));
            user.setGender(rs.getString("gender").charAt(0));
            user.setAvatar(rs.getString("avatar"));
            user.setRole(rs.getString("role").charAt(0));
            user.setEmail(rs.getString("email"));
        }

        rs.close();
        ps.close();
        con.close();

        return user;
    }

    public void saveNew(User user) throws Exception {
        if (existsByUsername(user.getUsername())) {
            throw new Exception("Username already exists");
        }
        if (existsByEmail(user.getEmail())) {
            throw new Exception("Email already exists");
        }
        if (user.getPhone() != null && existsByPhone(user.getPhone())) {
            throw new Exception("Phone number already exists");
        }

        Connection con = ConnectionPoolManager.getConnection();
        PreparedStatement ps = con.prepareStatement(
                "INSERT INTO User (full_name, username, password, phone, dob, gender, avatar, role, email) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
        ps.setString(1, user.getFullName());
        ps.setString(2, user.getUsername());
        ps.setString(3, user.getPassword());
        ps.setString(4, user.getPhone());
        ps.setDate(5, user.getDob());
        ps.setString(6, String.valueOf(user.getGender()));
        ps.setString(7, user.getAvatar());
        ps.setString(8, String.valueOf(user.getRole()));
        ps.setString(9, user.getEmail());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        int userId = 0;
        if (rs.next()) {
            userId = rs.getInt(1);
            user.setUserId(userId);
        }

        ps.close();
        con.close();
    }

    public void save(User user) throws Exception {

        Connection con = ConnectionPoolManager.getConnection();
        PreparedStatement ps = con.prepareStatement(
                "UPDATE User SET full_name = ?, username = ?, password = ?, phone = ?, dob = ?, gender = ?, avatar = ?, role = ?, email = ? WHERE user_id = ?");
        ps.setString(1, user.getFullName());
        ps.setString(2, user.getUsername());
        ps.setString(3, user.getPassword());
        ps.setString(4, user.getPhone());
        ps.setDate(5, user.getDob());
        ps.setString(6, String.valueOf(user.getGender()));
        ps.setString(7, user.getAvatar());
        ps.setString(8, String.valueOf(user.getRole()));
        ps.setString(9, user.getEmail());
        ps.setInt(10, user.getUserId()); // Assuming that User has a getId() method that returns the user's ID
        ps.executeUpdate();
        ps.close();
        con.close();
    }

    public void updatePassword(int userId, String newPassword) throws Exception {

        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement("UPDATE User SET password = ? WHERE user_id = ?");
        ps.setString(1, newPassword);
        ps.setInt(2, userId);
        ps.executeUpdate();
        ps.close();
        connection.close();
    }

    public boolean existsByUsername(String username) throws Exception {

        Connection con = ConnectionPoolManager.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT 1 FROM User WHERE username = ?");
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        boolean exists = rs.next();
        rs.close();
        ps.close();
        con.close();
        return exists;
    }

    public boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false;
        }

        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }

        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
    }

    public boolean existsByEmail(String email) throws Exception {

        Connection con = ConnectionPoolManager.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT 1 FROM User WHERE email = ?");
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        boolean exists = rs.next();
        rs.close();
        ps.close();
        con.close();
        return exists;
    }

    public boolean existsByPhone(String phone) throws Exception {

        Connection con = ConnectionPoolManager.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT 1 FROM User WHERE phone = ?");
        ps.setString(1, phone);
        ResultSet rs = ps.executeQuery();
        boolean exists = rs.next();
        rs.close();
        ps.close();
        con.close();
        return exists;
    }
}
