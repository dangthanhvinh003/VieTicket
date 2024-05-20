// package com.example.VieTicketSystem.fileSever.config;

// import java.io.IOException;

// import org.springframework.stereotype.Component;
// import com.example.VieTicketSystem.model.entity.User;

// import jakarta.servlet.Filter;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.ServletRequest;
// import jakarta.servlet.ServletResponse;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import jakarta.servlet.http.HttpSession;

// @Component
// public class AuthFilter implements Filter {

//     @Override
//     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//             throws IOException, ServletException {
//         HttpServletRequest httpRequest = (HttpServletRequest) request;
//         HttpServletResponse httpResponse = (HttpServletResponse) response;
//         HttpSession session = httpRequest.getSession();
//         String requestURI = httpRequest.getRequestURI();
//         if (requestURI.equals("/auth/login") || requestURI.equals("/") || requestURI.equals("")) {
//             chain.doFilter(request, response);
//             return;
//         }
    
//         // Bỏ qua filter cho các yêu cầu đến các tệp tĩnh (ví dụ: CSS, JavaScript)
       
//         User user = (User) session.getAttribute("activeUser");
//         System.out.println(user.toString());
//         // Kiểm tra role của người dùng và cho phép truy cập tài nguyên tương ứng
//         if (isAdmin(user)) {
//             // Người dùng có role ADMIN được truy cập tất cả các trang
//             chain.doFilter(request, response);
//         } else if (isUser(user) && requestURI.startsWith("/change")) {
//             // Người dùng có role USER chỉ được truy cập trang search
//             chain.doFilter(request, response);
//         } else {
//             // Không có quyền truy cập
//             httpResponse.sendRedirect("/auth/login");
//         }
        
//     }

//     private boolean isAdmin(User user) {
//         if (user != null && user.getRole() == 'a') {
//             return true;
//         }
//         return false;
//     }

//     private boolean isUser(User user) {
//         if (user != null) {
//             char role = user.getRole();
//             if (role == 'u' || role == 'o') {
//                 return true;
//             }
//         }
//         return false;
//     }

// }