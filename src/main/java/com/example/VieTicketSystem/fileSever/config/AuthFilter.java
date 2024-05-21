package com.example.VieTicketSystem.fileSever.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.VieTicketSystem.model.entity.Organizer;
import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.repo.OrganizerRepo;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthFilter implements Filter {
    @Autowired
    OrganizerRepo organizerRepo = new OrganizerRepo();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession();
        String requestURI = httpRequest.getRequestURI();

        // tất cả các role đều có thể vào
        if (requestURI.equals("/auth/login") || requestURI.equals("/")
                || requestURI.equals("/auth/login/oauth2/google") || requestURI.equals("")
                || requestURI.equals("/auth/reset-password") || requestURI.equals("/auth/password-reset")
                || requestURI.equals("/auth/verify-otp") || requestURI.equals("/signup")
                || requestURI.equals("/auth/log-out")) {
            chain.doFilter(request, response);
            return;
        }

        // Bỏ qua filter cho các yêu cầu đến các tệp tĩnh (ví dụ: CSS, JavaScript)
        String[] staticResources = { ".css", ".js", ".jpg", ".jpeg", ".png", ".gif" };
        for (String resource : staticResources) {
            if (requestURI.endsWith(resource)) {
                chain.doFilter(request, response);
                return;
            }
        }
        User user = (User) session.getAttribute("activeUser");

        // Kiểm tra role của người dùng và cho phép truy cập tài nguyên tương ứng
        if (isAdmin(user)) {
            // Người dùng có role ADMIN được truy cập tất cả các trang
            chain.doFilter(request, response);
        } else if (isUser(user) && (requestURI.startsWith("/change") || requestURI.startsWith("/editUser")
                || requestURI.startsWith("/upload") || requestURI.startsWith("/tickets"))) {
            // Người dùng có role USER chỉ được truy cập trang search
            chain.doFilter(request, response);
        } else if (isOrganizer(user)) {
            // Tìm thông tin Organizer dựa trên userId
            Organizer organizer = organizerRepo.getOrganizerByUserId(user.getUserId());

            if (organizer != null) {
                if (organizer.isActive() && requestURI.startsWith("/createEvent") || requestURI.startsWith("/inactive-account")||(requestURI.startsWith("/change") || requestURI.startsWith("/editUser")
                || requestURI.startsWith("/upload") ) ) {
                    // Người dùng có role ORGANIZER chỉ được truy cập các trang cho phép khi isActive
                    chain.doFilter(request, response);
                } else {
                    httpResponse.sendRedirect("/inactive-account");
                    return;
                }
            }
        } else {
            // Không có quyền truy cập
            httpResponse.sendRedirect("/");
        }

    }

    private boolean isAdmin(User user) {
        if (user != null && user.getRole() == 'a') {
            return true;
        }
        return false;
    }

    private boolean isUser(User user) {
        if (user != null) {
            char role = user.getRole();
            if (role == 'u') {
                return true;
            }
        }
        return false;
    }

    private boolean isOrganizer(User user) {
        if (user != null) {
            char role = user.getRole();
            if (role == 'o') {
                return true;
            }
        }
        return false;
    }

}