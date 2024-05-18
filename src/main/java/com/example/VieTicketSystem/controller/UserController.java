package com.example.VieTicketSystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller; // Import Controller annotation
import org.springframework.ui.Model; // Import Model class
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.repo.LoginRepo;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
    @Autowired
    LoginRepo loginRepo = new LoginRepo();

    @PostMapping(value = "/auth/sign-up")
    public String doLogin(@RequestParam("username") String usernameInput,
            @RequestParam("password") String passwordInput, Model model, HttpSession httpSession) throws Exception {
        User user = loginRepo.CheckLogin(usernameInput, passwordInput);
        if (user != null) {
            httpSession.setAttribute("activeUser", user);
            return "index.html";
        } else {
            return showLogin();
        }
    }

    @GetMapping(value = { "", "/" })
    public String showLogin() {
        return "index.html";
    }

    @GetMapping("/auth/login")
    public String loginPage() {
        
        return "login"; // Trả về tên của trang login.html
    }
    @GetMapping("/change")
    public String changeProfile() {
        return "changeProfile"; // Trả về tên của trang login.html
    }
    @GetMapping("/profile")
    public String profilePage(Model model, HttpSession httpSession) {
        // Kiểm tra xem session "activeUser" có tồn tại hay không
        if (httpSession.getAttribute("activeUser") != null) {
            // Nếu tồn tại, lấy thông tin người dùng từ session và truyền vào model
            User activeUser = (User) httpSession.getAttribute("activeUser");
            model.addAttribute("activeUser", activeUser);
            // Trả về trang home.html
            return "changeProfile.html";
        } else {
            // Nếu không tồn tại session "activeUser", chuyển hướng người dùng đến trang
            // đăng nhập
            return "redirect:/login";
        }
    }

}
