package com.example.villagerservice.post.service;

import com.example.villagerservice.post.dto.CreatePost;
import com.example.villagerservice.post.dto.ListPost;
import com.example.villagerservice.post.dto.UpdatePost;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import java.util.List;


public interface PostService {
    void createPost(Long memberId, CreatePost.Request request, List<MultipartFile> images);  // 게시글 작성
    void updatePost(Long memberId, Long postId,UpdatePost.Request request);  // 게시글 수정
    void deletePost(Long memberId, Long postId);

    List<ListPost.Response> getList();




}
