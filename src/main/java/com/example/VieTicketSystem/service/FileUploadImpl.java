package com.example.VieTicketSystem.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadImpl implements FileUpload {

        private final Cloudinary cloudinary;

        @Override
        public String uploadFile(MultipartFile multipartFile) throws IOException {
                return cloudinary.uploader()
                                .upload(multipartFile.getBytes(),
                                                Map.of("public_id", UUID.randomUUID().toString()))
                                .get("url")
                                .toString();
        }

        public String uploadFileImgBannerAndPoster(MultipartFile multipartFile, int width, int height)
                        throws IOException {
                Map uploadResult = cloudinary.uploader().upload(multipartFile.getBytes(),
                                ObjectUtils.asMap(
                                                "transformation", new com.cloudinary.Transformation().width(width)
                                                                .height(height).crop("fill")));
                return uploadResult.get("url").toString();
        }

        public String uploadFileSeatMap(MultipartFile multipartFile) throws IOException {

                Map uploadResult = cloudinary.uploader().upload(multipartFile.getBytes(),
                                ObjectUtils.asMap(
                                                "transformation", new Transformation().effect("trim")));

                return uploadResult.get("url").toString();

        }
}