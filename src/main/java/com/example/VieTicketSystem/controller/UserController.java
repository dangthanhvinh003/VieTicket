package com.example.VieTicketSystem.controller;

import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.repo.LoginRepo;
import com.example.VieTicketSystem.model.repo.UserRepo;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class UserController {
    @Autowired
    private LoginRepo loginRepo;

    @Autowired
    private UserRepo userRepo;
   

    @PostMapping(value = "/editUser")
    public String editUser(@RequestParam("fullName") String nameInput,
            @RequestParam("phone") String phoneInput,
            @RequestParam("email") String emailInput, @RequestParam("dob") Date dobInput,
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

    //khanh
    @GetMapping("/signup")
    public String signupPage() {
        return "signup"; // Trả về trang signup.html
    }
    @PostMapping("/signup")
    public String signUp() {
    // Redirect về trang đăng nhập
    return "redirect:/auth/login";
}
    


    // khanh
//     @PostMapping("/signup")
// public String registerUser(@RequestParam("fullName") String fullName,
//                            @RequestParam("phone") String phone,
//                            @RequestParam("dob") Date dob,
//                            @RequestParam("gender") Character gender,
//                            @RequestParam("email") String email,
//                  .          @RequestParam("username") String username,
//                            @RequestParam("password") String password,
//                            @RequestParam("confirmPassword") String confirmPassword,
//                            Model model) {

//     if (!password.equals(confirmPassword)) {
//         model.addAttribute("error", "Passwords do not match");
//         return "signup";
//     }

//     if (userRepo.existsByUsername(username)) {
//         model.addAttribute("error", "Username is already taken");
//         return "signup";
//     }

//     User newUser = new User(fullName, phone, dob, gender, email, username, password);
//     userRepo.save(newUser);

//     return "redirect:/auth/login";
// }
//     @RestController
//     public class UserApiController {

//         @Autowired
//         private UserRepo userRepo;

//         @GetMapping("/api/check-username")
//         public Map<String, Boolean> checkUsername(@RequestParam String username) {
//             boolean exists = userRepo.existsByUsername(username);
//             Map<String, Boolean> response = new HashMap<>();
//             response.put("exists", exists);
//             return response;
//         }
//     }
// @PostMapping("/signup")
    // public String signUp(@RequestParam("username") String username, @RequestParam("password") String password) {
    //     // Kiểm tra xem username đã tồn tại trong cơ sở dữ liệu chưa
    //     if (userRepo.existsByUsername(username)) {
    //         return "Username already exists";
    //     } else {
    //         // Nếu username chưa tồn tại, thêm username mới vào cơ sở dữ liệu
    //         User newUser = new User(username, password);
    //         userRepo.save(newUser);
    //         return "User created successfully";
    //     }
    // }
}

