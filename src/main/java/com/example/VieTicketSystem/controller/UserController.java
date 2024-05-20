package com.example.VieTicketSystem.controller;

import java.sql.Date;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.entity.Organizer;
import com.example.VieTicketSystem.model.repo.LoginRepo;
import com.example.VieTicketSystem.model.repo.OrganizerRepo;
import com.example.VieTicketSystem.model.repo.UserRepo;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
    @Autowired
    private LoginRepo loginRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private OrganizerRepo organizerRepo;

    @PostMapping(value = "/editUser")
    public String editUser(@RequestParam("fullName") String nameInput,
            @RequestParam("phone") String phoneInput,
            @RequestParam("email") String emailInput,
            @RequestParam("dob") Date dobInput,
            @RequestParam("gender") Character genderInput, Model model,
            HttpSession httpSession) throws Exception {
        User activeUser = (User) httpSession.getAttribute("activeUser");
        userRepo.editProfile(nameInput, emailInput, phoneInput, dobInput, genderInput, activeUser.getUserId());
        activeUser.setFullName(nameInput);
        activeUser.setPhone(phoneInput);
        activeUser.setEmail(emailInput);
        activeUser.setDob(dobInput);
        activeUser.setGender(genderInput);
        httpSession.setAttribute("activeUser", activeUser);

        return "redirect:/change";
    }

    @PostMapping(value = "/auth/login")
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

    @GetMapping("/auth/reset-password")
    public String showPasswordResetForm() {
        return "reset-password";
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
            return "redirect:/auth/login";
        }
    }

    // khanh
    @GetMapping("/signup")
    public String signupPage() {
        return "signup"; // Trả về trang signup.html
    }
    // @PostMapping("/signup")
    // public String signUp() {
    // // Redirect về trang đăng nhập
    // return "redirect:/auth/login";
    // }

    @PostMapping("/signup")
    public String signUp(@RequestParam("fullName") String fullName,
            @RequestParam("phone") String phone,
            @RequestParam("dob") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dob,
            @RequestParam("gender") char gender,
            @RequestParam("email") String email,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam("role") char role,
            @RequestParam(value = "foundedDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate foundedDate,
            @RequestParam(value = "website", required = false) String website,
            @RequestParam(value = "organizerAddr", required = false) String organizerAddr,
            @RequestParam(value = "organizerType", required = false) String organizerType,
            Model model) throws Exception {
        // Validate passwords match
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Passwords do not match.");
            return "signup";
        }
        if (userRepo.existsByPhone(phone)) {
            model.addAttribute("errorMessage", "Phone already exists.");
            return "signup";
        }
        // Check if username already exists
        if (userRepo.existsByUsername(username)) {
            model.addAttribute("errorMessage", "Username already exists.");
            return "signup";
        }

        // Check if email already exists
        if (userRepo.existsByEmail(email)) {
            model.addAttribute("errorMessage", "Email already exists.");
            return "signup";
        }

        // Create new user and save to databas
        // Convert LocalDate to java.sql.Date
        Date sqlDob = Date.valueOf(dob);
        Date sqlFoundedDate = (foundedDate != null) ? Date.valueOf(foundedDate) : null;

        // Set role and additional organizer details if the role is "Organizer"
        if (role == 'o') {
            Organizer newUser = new Organizer();
            newUser.setFullName(fullName);
            newUser.setPhone(phone);
            newUser.setDob(sqlDob);
            newUser.setGender(gender);
            newUser.setEmail(email);
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setRole('o');

            newUser.setFoundedDate(sqlFoundedDate);
            newUser.setWebsite(website);
            newUser.setActive(false);
            newUser.setOrganizerAddr(organizerAddr);
            newUser.setOrganizerType(organizerType);
            organizerRepo.saveNew(newUser);

        } else {
            User newUser = new User();
            newUser.setFullName(fullName);
            newUser.setPhone(phone);
            newUser.setDob(sqlDob);
            newUser.setGender(gender);
            newUser.setEmail(email);
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setRole('u');
            userRepo.saveNew(newUser);
        }

        // Redirect to login page after successful registration
        return "redirect:/auth/login";
    }
}
