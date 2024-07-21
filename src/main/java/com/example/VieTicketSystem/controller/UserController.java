package com.example.VieTicketSystem.controller;

import java.sql.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.example.VieTicketSystem.model.dto.SignUpRequest;
import com.example.VieTicketSystem.model.entity.Event;
import com.example.VieTicketSystem.model.entity.Organizer;
import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.repo.EventRepo;
import com.example.VieTicketSystem.repo.OrganizerRepo;
import com.example.VieTicketSystem.repo.UserRepo;
import com.example.VieTicketSystem.service.Oauth2Service;
import com.example.VieTicketSystem.service.VerifyEmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerifyEmailService verifyEmailService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EventRepo eventRepo;

    @Autowired
    private OrganizerRepo organizerRepo;
    @Autowired
    private HttpSession httpSession;

    @PostMapping(value = "/editUser") // Them thuoc tinh cho organizer
    public String editUser(@RequestParam("fullName") String nameInput,
                           @RequestParam("phone") String phoneInput,
                           @RequestParam("email") String emailInput,
                           @RequestParam("dob") Date dobInput,
                           @RequestParam("gender") Character genderInput,
                           @RequestParam(value = "foundedDate", required = false) Date foundedDateInput,
                           @RequestParam(value = "website", required = false) String websiteInput,
                           @RequestParam(value = "organizerAddr", required = false) String organizerAddrInput,
                           @RequestParam(value = "organizerType", required = false) String organizerTypeInput,
                           Model model,
                           HttpSession httpSession) throws Exception {
        User activeUser = (User) httpSession.getAttribute("activeUser");

        // Update common user attributes
        userRepo.editProfile(nameInput, emailInput, phoneInput, dobInput, genderInput, activeUser.getUserId());
        activeUser.setFullName(nameInput);
        activeUser.setPhone(phoneInput);
        activeUser.setEmail(emailInput);
        activeUser.setDob(dobInput);
        activeUser.setGender(genderInput);
        userRepo.save(activeUser);

        // Update organizer-specific attributes if the user is an organizer
        if (activeUser.getRole() == 'o') {

            Organizer activeOrganizer = organizerRepo.findById(activeUser.getUserId());
            if (foundedDateInput != null) {
                activeOrganizer.setFoundedDate(foundedDateInput);
            }
            if (websiteInput != null) {
                activeOrganizer.setWebsite(websiteInput);
            }
            if (organizerAddrInput != null) {
                activeOrganizer.setOrganizerAddr(organizerAddrInput);
            }
            if (organizerTypeInput != null) {
                activeOrganizer.setOrganizerType(organizerTypeInput);
            }

            // Call repository method to update organizer-specific attributes in the
            // database
            organizerRepo.save(activeOrganizer);
            httpSession.setAttribute("activeOrganizer", activeOrganizer);
        }

        activeUser = userRepo.findById(activeUser.getUserId());
        httpSession.setAttribute("activeUser", activeUser);

        return "redirect:/change";
    }

    @GetMapping("/auth/verify-email")
    public String showVerifyEmailPage(Model model, HttpSession session) throws Exception {
        User user = (User) session.getAttribute("activeUser");
        if (user == null || user.isVerified()) {
            return "redirect:/"; // Redirect if not applicable
        }
        return "auth/verify-email";
    }

    @PostMapping(value = "/auth/login")
    public String doLogin(@RequestParam("username") String usernameInput,
                          @RequestParam("password") String passwordInput,
                          Model model, HttpSession httpSession) throws Exception {
        User user = userRepo.findByUsername(usernameInput);
        if (user == null) {
            model.addAttribute("error", "Invalid login, please try again");
            return "auth/login";
        }

        if (!passwordEncoder.matches(passwordInput, user.getPassword())) {
            model.addAttribute("error", "Invalid login, please try again");
            return "auth/login";
        }

        httpSession.setAttribute("activeUser", user);

        if (user.getRole() == 'o') {
            Organizer organizer = organizerRepo.findById(user.getUserId());
            httpSession.setAttribute("activeOrganizer", organizer);
            // Redirect to Organizer's specific page
        }

        String redirect = (String) httpSession.getAttribute("redirect");
        if (redirect != null) {
            httpSession.removeAttribute("redirect");
            return "redirect:" + redirect;
        }
        if (user.getRole() == 'a') {
            return "redirect:/admin/dashboard";
        }
        if (user.getRole() == 'p' || user.getRole() == 'b') {
            return "auth/banned";
        }

        return "redirect:/";
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

    @GetMapping(value = {"", "/"})
    public String showLogin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            HttpSession session,
            Model model) throws Exception {

        String message = null;
        User user = (User) session.getAttribute("activeUser");
        if (user != null && user.getRole() == 'o') {
            Organizer organizer = organizerRepo.findById(user.getUserId());
            session.setAttribute("activeOrganizer", organizer);
            if (!organizer.isActive()) {
                message = "Your account is not active yet. Please wait for admin to approve. In order to be approved, you need to provide more information about your organization in 'My Profile' page.";
            }
        }

        List<Event> events = eventRepo.getEventsPaginated(page, size);
        List<Event> hotEvents = eventRepo.getTopHotEvents();
        int totalEvents = eventRepo.countApprovedEvents();
        int totalPages = (int) Math.ceil((double) totalEvents / size);

        model.addAttribute("message", message);
        session.setAttribute("hotevents", hotEvents);
        session.setAttribute("events", events);
        session.setAttribute("currentPage", page);
        session.setAttribute("totalPages", totalPages);
        session.setAttribute("eventCreated", false);
        System.out.println("gọi về index");
        return "public/index";
    }

    @GetMapping("/eventsListFragment")
    public String getEventsListFragment(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            HttpSession session,
            Model model) throws Exception {

        List<Event> events = eventRepo.getEventsPaginated(page, size);
        model.addAttribute("events", events);
        session.setAttribute("events", events);

        int totalEvents = eventRepo.countApprovedEvents();
        System.out.println(totalEvents);
        int totalPages = (int) Math.ceil((double) totalEvents / size);
        System.out.println("Received totalPages from server: " + totalPages);

        session.setAttribute("currentPage", page);
        session.setAttribute("totalPages", totalPages);

        System.out.println(events);
        System.out.println("gọi về eventsListFragment");
        return "eventsFragment :: eventsList";
    }

    @GetMapping("/auth/login")
    public String loginPage(Model model, HttpServletRequest request) {
        // Construct base URL from request
        String baseUrl = request.getScheme() + "://" + request.getServerName();
        if ((request.getScheme().equals("http") && request.getServerPort() != 80) ||
                (request.getScheme().equals("https") && request.getServerPort() != 443)) {
            baseUrl += ":" + request.getServerPort();
        }
        String redirectUri = baseUrl + "/auth/login/oauth2/google";

        String googleLoginUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=640078988681-kmojc9p5oqoc79flah45qhojn1q3l827.apps.googleusercontent.com"
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=openid%20email%20profile";

        model.addAttribute("googleLoginUrl", googleLoginUrl);

        return "auth/login"; // Return the name of the login page template
    }

    @GetMapping("/auth/login/oauth2/google")
    public String doLoginWithGoogle(@RequestParam("code") String authorizationCode,
                                    HttpSession httpSession)
            throws Exception {
        // System.out.println("hello");
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
        return "auth/reset-password";
    }

    @GetMapping("/change")
    public String changeProfile() {
        return "user-settings/change-profile"; // Trả về tên của trang changeProfile
    }

    @GetMapping("/change-password")
    public String changePassword() {
        return "user-settings/change-password"; // Trả về tên của trang changePassword
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

        if (!passwordEncoder.matches(oldPassword, activeUser.getPassword())) {
            model.addAttribute("error", "Wrong old password");
            return "change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "New passwords do not match");
            return "change-password";
        }
        if (!userRepo.isValidPassword(newPassword)) {
            model.addAttribute("error",
                    "Password must be at least 8 characters long and include at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character.");
            return "change-password";
        }

        userRepo.updatePassword(activeUser.getUserId(), passwordEncoder.encode(newPassword));
        model.addAttribute("message", "Password changed successfully");
        return "change-password";
    }

    @GetMapping("/profile")
    public String profilePage(Model model, HttpSession httpSession) {
        // Kiểm tra xem session "activeUser" có tồn tại hay không
        if (httpSession.getAttribute("activeUser") == null) {
            // Nếu không tồn tại session "activeUser", chuyển hướng người dùng đến trang
            // đăng nhập
            return "redirect:/auth/login";
        }

        // Nếu tồn tại, lấy thông tin người dùng từ session và truyền vào model
        User activeUser = (User) httpSession.getAttribute("activeUser");
        model.addAttribute("activeUser", activeUser);
        // Trả về trang home.html
        return "redirect:/change";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "auth/signup"; // Trả về trang signup.html
    }

    @PostMapping("/signup")
    public String signUp(@RequestBody SignUpRequest signUpRequest, Model model, HttpSession httpSession)
            throws Exception {
        String fullName = signUpRequest.getFullName();
        String email = signUpRequest.getEmail();
        String username = signUpRequest.getUsername();
        String password = signUpRequest.getPassword();
        char role = signUpRequest.getRole();

        // if (userRepo.existsByPhone(phone)) {
        // model.addAttribute("error", "Phone already exists.");
        // return "auth/signup";
        // }
        // Check if username already exists
        if (userRepo.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        // Check if email already exists
        if (userRepo.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists.");
        }

        // if (!userRepo.isValidPhone(phone)) {
        // model.addAttribute("error",
        // "Phone invalid");
        // return "auth/signup";
        // }
        if (!userRepo.isValidPassword(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must be at least 8 characters long and include at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character.");
        }

        // Hash the password
        String hashedPassword = passwordEncoder.encode(password);
        // System.out.println(hashedPassword);

        // Create new user and save to database
        // Convert LocalDate to java.sql.Date

        User newUser = new User();
        newUser.setFullName(fullName);

        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setPassword(hashedPassword);
        newUser.setRole(Character.toUpperCase(role));
        userRepo.saveNew(newUser);

        if (role == 'o') {
            organizerRepo.saveOnSignup(newUser);
        }

        httpSession.setAttribute("activeUser", newUser);

        verifyEmailService.sendOTP(email);

        return "redirect:/auth/verify-email";
    }

}
