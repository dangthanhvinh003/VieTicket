package com.example.VieTicketSystem.controller;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.entity.Event;
import com.example.VieTicketSystem.model.entity.Organizer;
import com.example.VieTicketSystem.model.repo.EventRepo;
import com.example.VieTicketSystem.model.repo.LoginRepo;
import com.example.VieTicketSystem.model.repo.OrganizerRepo;
import com.example.VieTicketSystem.model.repo.UnverifiedUserRepo;
import com.example.VieTicketSystem.model.repo.UserRepo;

import com.example.VieTicketSystem.model.service.EmailService;
import com.example.VieTicketSystem.model.service.VerifyEmailService;

import com.example.VieTicketSystem.model.service.Oauth2Service;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    @Autowired
    private PasswordEncoder passwordEncoder;
    EmailService emailService;

    @Autowired
    private VerifyEmailService verifyEmailService;

    @Autowired
    private UnverifiedUserRepo unverifiedUserRepo;

    @Autowired
    private LoginRepo loginRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EventRepo eventRepo;

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

    @GetMapping("/auth/verify-email")
    public String showVerifyEmailPage(Model model, HttpSession session) throws Exception {
        User user = (User) session.getAttribute("activeUser");
        if (user == null || !unverifiedUserRepo.isUnverified(user.getUserId())) {
            return "redirect:/"; // Redirect if not applicable
        }
        return "verify-email";
    }

    @PostMapping(value = "/auth/login")
    public String doLogin(@RequestParam("username") String usernameInput,
            @RequestParam("password") String passwordInput, Model model, HttpSession httpSession) throws Exception {
        User user = userRepo.findByUsername(usernameInput);
        if (user != null && passwordEncoder.matches(passwordInput, user.getPassword())) {
            model.addAttribute("error", "Username does not exist");
            return "login";
        } else if (!loginRepo.checkPassword(usernameInput, passwordInput)) {
            model.addAttribute("error", "Incorrect password");
            return "login";
        } else {
            if (user instanceof Organizer) {
                httpSession.setAttribute("activeOrganizer", user);
                // Redirect to Organizer's specific page
                return "redirect:/";
            } else {
                httpSession.setAttribute("activeUser", user);
                // Redirect to User's specific page
                return "redirect:/";
            }
        }
    }

    @GetMapping(value = "/auth/log-out")
    public String doLogout(HttpSession httpSession) {
        // Xóa thuộc tính activeUser khỏi session
        httpSession.removeAttribute("activeUser");

        // Vô hiệu hóa session hiện tại
        httpSession.invalidate();

        // Chuyển hướng người dùng đến trang chủ
        return "redirect:/";
    }

    @GetMapping(value = { "", "/" })
    public String showLogin(HttpSession session) {
        List<Event> events = eventRepo.getAllEvents();
        System.out.println(events);
        session.setAttribute("events", events);

        return "index";
    }

    @GetMapping("/auth/login")
    public String loginPage() {

        return "login"; // Trả về tên của trang login.html
    }

    @GetMapping("/auth/login/oauth2/google")
    public String doLoginWithGoogle(@RequestParam("code") String authorizationCode,
            HttpSession httpSession)
            throws Exception {
        System.out.println("hello");
        Oauth2Service oauth2 = new Oauth2Service();
        // Exchange the authorization code for an access token
        String accessToken = oauth2.getAccessToken(authorizationCode);
        // Get user info
        User client = oauth2.getUserInfo(accessToken);
        User user = userRepo.findByEmail(client.getEmail());
        if (user == null) {
            userRepo.saveNew(client);
            httpSession.setAttribute("activeUser", client);
        } else {
            httpSession.setAttribute("activeUser", user);
        }
        return "redirect:/";
    }

    @GetMapping("/auth/reset-password")
    public String showPasswordResetForm() {
        return "reset-password";
    }

    @GetMapping("/change")
    public String changeProfile() {
        return "changeProfile"; // Trả về tên của trang changeProfile
    }

    @GetMapping("/change-password")
    public String changePassword() {
        return "change-password"; // Trả về tên của trang changePassword
    }

    @PostMapping(value = "/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model,
            HttpSession httpSession) throws Exception {
        User activeUser = (User) httpSession.getAttribute("activeUser");

        if (activeUser == null) {
            return "redirect:/auth/login";
        }

        if (!loginRepo.checkPassword(activeUser.getUsername(), oldPassword)) {
            model.addAttribute("error", "Wrong old password");
            return "change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "New passwords do not match");
            return "change-password";
        }

        userRepo.updatePassword(activeUser.getUserId(), newPassword);
        model.addAttribute("message", "Password changed successfully");
        return "change-password";
    }

    @GetMapping("/profile")
    public String profilePage(Model model, HttpSession httpSession) {
        // Kiểm tra xem session "activeUser" có tồn tại hay không
        if (httpSession.getAttribute("activeUser") != null) {
            // Nếu tồn tại, lấy thông tin người dùng từ session và truyền vào model
            User activeUser = (User) httpSession.getAttribute("activeUser");
            model.addAttribute("activeUser", activeUser);
            // Trả về trang home.html
            return "redirect:/change";
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

        if (userRepo.existsByPhone(phone)) {
            model.addAttribute("errorMessage", "Phone already exists.");
            return "redirect:/signup";
        }
        // Check if username already exists
        if (userRepo.existsByUsername(username)) {
            model.addAttribute("errorMessage", "Username already exists.");
            return "redirect:/signup";
        }

        // Check if email already exists
        if (userRepo.existsByEmail(email)) {
            model.addAttribute("errorMessage", "Email already exists.");
            return "signup";
        }

        // Hash the password
        String hashedPassword = passwordEncoder.encode(password);

        // Create new user and save to database
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
            newUser.setPassword(hashedPassword); // Use hashed password
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
            newUser.setPassword(hashedPassword); // Use hashed password
            newUser.setRole('u');
            userRepo.saveNew(newUser);
        }

        try {
            verifyEmailService.sendOTP(email);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Redirect to login page after successful registration
        return "redirect:/auth/login";
    }

}
