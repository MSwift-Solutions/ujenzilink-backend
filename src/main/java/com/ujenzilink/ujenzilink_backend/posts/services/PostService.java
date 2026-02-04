package com.ujenzilink.ujenzilink_backend.posts.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.images.dtos.CloudinaryUploadResponse;
import com.ujenzilink.ujenzilink_backend.images.dtos.ImageMetadata;
import com.ujenzilink.ujenzilink_backend.images.models.Image;
import com.ujenzilink.ujenzilink_backend.images.repositories.ImageRepository;
import com.ujenzilink.ujenzilink_backend.images.services.CloudinaryService;
import com.ujenzilink.ujenzilink_backend.images.services.ImageValidationService;
import com.ujenzilink.ujenzilink_backend.posts.dtos.CreatePostRequest;
import com.ujenzilink.ujenzilink_backend.posts.dtos.CreatePostResponse;
import com.ujenzilink.ujenzilink_backend.posts.models.Post;
import com.ujenzilink.ujenzilink_backend.posts.models.PostImage;
import com.ujenzilink.ujenzilink_backend.posts.repositories.PostImageRepository;
import com.ujenzilink.ujenzilink_backend.posts.repositories.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageValidationService imageValidationService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private SecurityUtil securityUtil;

    @Transactional(rollbackFor = Exception.class)
    public ApiCustomResponse<CreatePostResponse> createPost(CreatePostRequest request, List<MultipartFile> images) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        User user = userOpt.get();

        boolean hasContent = request.content() != null && !request.content().trim().isEmpty();
        boolean hasImages = images != null && !images.isEmpty();

        if (!hasContent && !hasImages) {
            return new ApiCustomResponse<>(null, "Post must have either content, images, or both.",
                    HttpStatus.BAD_REQUEST.value());
        }

        if (hasImages && images.size() > 10) {
            return new ApiCustomResponse<>(null, "Maximum 10 images allowed per post.", HttpStatus.BAD_REQUEST.value());
        }

        Post post = new Post();
        post.setContent(hasContent ? request.content().trim() : "");
        post.setCreator(user);

        Post savedPost = postRepository.save(post);

        if (hasImages) {
            int order = 0;
            for (MultipartFile file : images) {
                if (file.isEmpty())
                    continue;

                ImageMetadata metadata = imageValidationService.validateAndExtractMetadata(file);
                CloudinaryUploadResponse uploadResponse = cloudinaryService.uploadImage(file, "ujenzilink/post-images");

                Image image = new Image();
                image.setUrl(uploadResponse.secureUrl());
                image.setFilename(metadata.filename());
                image.setFileType(metadata.fileType());
                image.setFileSize(metadata.fileSize());
                image.setWidth(uploadResponse.width());
                image.setHeight(uploadResponse.height());
                image.setUser(user);
                image = imageRepository.save(image);

                PostImage postImage = new PostImage();
                postImage.setPost(savedPost);
                postImage.setImage(image);
                postImage.setImageOrder(order++);
                postImageRepository.save(postImage);
            }
        }

        CreatePostResponse response = new CreatePostResponse(savedPost.getId(), "Post created successfully");
        return new ApiCustomResponse<>(response, "Post created successfully.", HttpStatus.CREATED.value());
    }
}
