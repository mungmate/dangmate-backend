package com.example.mungmatebackend.domain.post.service;

import com.example.mungmatebackend.api.post.dto.PostDto;
import com.example.mungmatebackend.api.posts.dto.PostsDto;
import com.example.mungmatebackend.domain.common.CreatedAt;
import com.example.mungmatebackend.domain.gallery.service.GalleryService;
import com.example.mungmatebackend.domain.likeUser.entity.LikeUser;
import com.example.mungmatebackend.domain.likeUser.repository.LikeUserRepository;
import com.example.mungmatebackend.domain.post.entity.Post;
import com.example.mungmatebackend.domain.post.repository.PostRepository;
import com.example.mungmatebackend.domain.user.entity.User;
import com.example.mungmatebackend.domain.user.repository.UserRepository;
import com.example.mungmatebackend.global.error.ErrorCode;
import com.example.mungmatebackend.global.error.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService extends CreatedAt {

  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final LikeUserRepository likeUserRepository;
  private final GalleryService galleryService;

  private PostsDto.PostsResponse getAllList(PostsDto.PostsRequest request) {
    List<Post> posts = postRepository.findAll(Sort.by(Direction.DESC, "id"));

    for (Post post : posts) {
      User user = post.getUser();
      String profile = user.getProfile();
      String fullName = user.getFullName();
      String category = post.getCategory();
      String thumbnail = post.getThumbnail();
      String content = post.getContent();
      String location = post.getLocation();
      LocalDateTime createdAt = post.getCreatedAt();
      Integer views = post.getViews();

    }

//    PostDto.builder()
//        .profile()
//        .build();

//    return PostsDto.PostsResponse.builder()
//        .location(request.getLocation())
//        .posts(
//            List < PostDto > PostDto.builder().build();
//        )
//        .total(posts.size())
//        .build();

    return PostsDto.PostsResponse.builder().build();
  }

  public PostsDto.PostsResponse getPosts(PostsDto.PostsRequest request) {

    String category = request.getCategory();
    String location = request.getLocation();

    if (category.equals("all")) {
      return getAllList(request);
    }

    List<Post> posts = postRepository.findByCategory(category);

    return PostsDto.PostsResponse.builder().build();
  }

  public com.example.mungmatebackend.api.post.dto.PostDto.PostUploadResponse uploadPost(
      com.example.mungmatebackend.api.post.dto.PostDto.PostUploadRequest request) {

    Post post = postRepository.save(Post.builder()
        .location(request.getLocation())
        .category(request.getCategory())
        .thumbnail(request.getThumbnail())
        .content(request.getContent())
        .user(User.builder()
            .id(request.getUserId())
            .build())
        .build());

    return com.example.mungmatebackend.api.post.dto.PostDto.PostUploadResponse.builder()
        .statusCode("200")
        .postId(post.getId())
        .build();
  }

  public PostDto.PostGetResponse getPost(Long postId, Long userId) {
    Optional<Post> post = postRepository.findById(postId);

    if (post.isEmpty()) {
      throw new BusinessException(ErrorCode.POST_NOT_FOUND);
    }

    Optional<User> user = userRepository.findById(userId);

    String createdAt = getCreatedAt(post.get().getCreatedAt());

    Optional<LikeUser> likeUser = likeUserRepository.findByPostIdAndUserId(post.get().getId(),
        user.get().getId());

    int views = post.get().getViews();

    post.get().setViews(post.get().getViews() + 1);
    postRepository.save(post.get());

    return PostDto.PostGetResponse.builder()
        .profile(user.get().getProfile())
        .fullName(user.get().getFullName())
        .category(post.get().getCategory())
        .thumbnail(post.get().getThumbnail())
        .content(post.get().getContent())
        .location(post.get().getLocation())
        .createdAt(createdAt)
        .comments(post.get().getComments())
        .likes(post.get().getLikes())
        .isLike(likeUser.isPresent())
        .isPost(Objects.equals(user.get().getId(), post.get().getUser().getId()))
        .views(views)
        .build();
  }

  public PostDto.PostPutResponse putPost(
      Long postId,
      Long userId,
      PostDto.PostPutRequest request
  ) {
    Optional<Post> post = postRepository.findById(postId);

    if (post.isEmpty()) {
      throw new BusinessException(ErrorCode.POST_NOT_FOUND);
    }

    if (!Objects.equals(post.get().getUser().getId(), userId)) {
      throw new BusinessException(ErrorCode.POST_USER_NOT_MATCH);
    }

    if (!Objects.equals(request.getThumbnail(), post.get().getThumbnail())) {
      galleryService.deleteImage(post.get().getThumbnail());
    }

    post.get().setLocation(request.getLocation());
    post.get().setCategory(request.getCategory());
    post.get().setThumbnail(request.getThumbnail());
    post.get().setContent(request.getContent());

    postRepository.save(post.get());

    return PostDto.PostPutResponse.builder()
        .postId(postId)
        .location(post.get().getLocation())
        .category(post.get().getCategory())
        .thumbnail(post.get().getThumbnail())
        .content(post.get().getContent())
        .build();
  }

  public PostDto.PostDeleteResponse deletePost(Long postId, Long userId) {
    Optional<Post> post = postRepository.findById(postId);

    if (post.isEmpty()) {
      throw new BusinessException(ErrorCode.POST_NOT_FOUND);
    }

    if (!Objects.equals(post.get().getUser().getId(), userId)) {
      throw new BusinessException(ErrorCode.POST_USER_NOT_MATCH);
    }

    post.get().setIsActive(false);
    postRepository.save(post.get());

    return PostDto.PostDeleteResponse.builder()
        .statusCode("200")
        .postId(postId)
        .build();
  }

}
