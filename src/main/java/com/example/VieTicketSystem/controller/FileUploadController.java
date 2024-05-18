package com.example.VieTicketSystem.controller;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.example.VieTicketSystem.model.entity.User;

import com.example.VieTicketSystem.model.repo.UserRepo;
import com.example.VieTicketSystem.model.service.FileUpload;

import jakarta.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class FileUploadController {
    @Autowired
    UserRepo userRepo = new UserRepo();
    private final FileUpload fileUpload;
    private final Cloudinary cloudinary;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("image") MultipartFile multipartFile,
            Model model, HttpSession httpSession) throws Exception {

        Map uploadResult = cloudinary.uploader().upload(multipartFile.getBytes(),
                ObjectUtils.asMap(
                        "transformation", new Transformation()
                                .aspectRatio("1.0")
                                .gravity("face")
                                .width(200)
                                .crop("thumb")));

        // Lấy URL của ảnh đã upload
        String imageURL = uploadResult.get("url").toString();

        // Thêm URL ảnh vào model
        model.addAttribute("imageURL", imageURL);

        // Cập nhật thông tin người dùng
        User user = (User) httpSession.getAttribute("activeUser");
        userRepo.EditImgUser(imageURL, user.getUserId());
        user.setAvatar(imageURL);
        httpSession.setAttribute("activeUser", user);

        return "redirect:/home";
    }
}