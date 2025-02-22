package com.example.villagerservice.post.service.impl;

import com.example.villagerservice.member.domain.Member;
import com.example.villagerservice.member.domain.MemberRepository;
import com.example.villagerservice.member.exception.MemberException;
import com.example.villagerservice.post.domain.*;
import com.example.villagerservice.post.dto.CategoryDto;
import com.example.villagerservice.post.dto.CreatePost;
import com.example.villagerservice.post.dto.UpdatePost;
import com.example.villagerservice.post.exception.CategoryException;
import com.example.villagerservice.post.exception.PostException;
import com.example.villagerservice.post.service.FileUploadService;
import com.example.villagerservice.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.villagerservice.member.exception.MemberErrorCode.MEMBER_NOT_FOUND;
import static com.example.villagerservice.post.exception.CategoryErrorCode.CATEGORY_NOT_FOUND;
import static com.example.villagerservice.post.exception.PostErrorCode.POST_NOT_FOUND;
import static com.example.villagerservice.post.exception.PostErrorCode.POST_VALID_NOT;


@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final FileUploadService fileUploadService;

    @Override
    public void createPost(Long memberId, CreatePost.Request request, List<MultipartFile> images) {
        Member member = findByMemberId(memberId);
        Category category = findByCategoryId(request.getCategoryId());

        Post post = new Post(member, category, request.getTitle(), request.getContents());

        List<String> imagePaths = new ArrayList<>();
        if (images != null) {
            for (MultipartFile image : images) {
                String imagePath = UUID.randomUUID() + "-" + image.getOriginalFilename();
                post.addImages(PostImage.createPostImage(image.getSize(), imagePath));
                imagePaths.add(imagePath);
            }
        }

        postRepository.save(post);
        if (!imagePaths.isEmpty()) {
            fileUploadService.fileUpload(images, imagePaths);
        }
    }

    @Transactional
    @Override
    public void updatePost(Long memberId, Long postId, UpdatePost.Request request) {
        Member member = findByMemberId(memberId);
        Post post = findByPostIdAndMember(postId, member);  // 맴버아이디+게시글id 일치하는지
        Category category = findByCategoryId(request.getCategoryId());
        post.updatePost(category, request.getTitle(), request.getContents());
    }

    @Transactional
    @Override
    public void deletePost(Long memberId, Long postId) {
        Member member = findByMemberId(memberId);
        Post post = findByPostIdAndMember(postId, member);
        post.deletePost();
    }

    @Override
    @Transactional
    public void postViewCountUp(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(POST_NOT_FOUND));
        post.postViewUp();
    }

    @Override
    public List<CategoryDto.Response> getCategoryList() {
        List<Category> categorys = categoryRepository.getCategoryList();
        return categorys.stream()
                .map(category -> new CategoryDto.Response(category.getId(), category.getName()))
                .collect(Collectors.toList());
    }

    private Member findByMemberId(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));
    }

    private Category findByCategoryId(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryException(CATEGORY_NOT_FOUND));
    }

    private Post findByPostIdAndMember(Long postId, Member member) {
        return postRepository.findByIdAndMember(postId, member).orElseThrow(() -> new PostException(POST_VALID_NOT));
    }
}
