package com.example.VieTicketSystem.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileUpload {
    String uploadFile(MultipartFile multipartFile) throws IOException;
    String uploadFileImgBannerAndPoster(MultipartFile multipartFile, int w, int h)throws IOException;
    String uploadFileSeatMap(MultipartFile multipartFile) throws IOException;
}
