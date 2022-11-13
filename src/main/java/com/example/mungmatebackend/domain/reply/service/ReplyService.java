package com.example.mungmatebackend.domain.reply.service;

import com.example.mungmatebackend.api.post.dto.ReplyDto;
import com.example.mungmatebackend.domain.comment.entity.Comment;
import com.example.mungmatebackend.domain.comment.repository.CommentRepository;
import com.example.mungmatebackend.domain.post.entity.Post;
import com.example.mungmatebackend.domain.post.repository.PostRepository;
import com.example.mungmatebackend.domain.reply.entity.Reply;
import com.example.mungmatebackend.domain.reply.repository.ReplyRepository;
import com.example.mungmatebackend.domain.user.entity.User;
import com.example.mungmatebackend.domain.user.repository.UserRepository;
import com.example.mungmatebackend.global.error.ErrorCode;
import com.example.mungmatebackend.global.error.exception.BusinessException;
import java.util.Optional;
import javax.swing.text.html.Option;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReplyService {
  private final ReplyRepository replyRepository;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  public ReplyDto.ReplyPostResponse postReply(Long id, ReplyDto.ReplyPostRequest request){

    Optional<Post> post = postRepository.findById(id);
    Optional<Comment> comment = commentRepository.findById(request.getCommentId());
    Optional<User> user = userRepository.findById(request.getUserId());

    if(post.isEmpty()){
      throw new BusinessException(ErrorCode.POST_NOT_FOUND);
    }

    if(comment.isEmpty()){
      throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
    }

    if(user.isEmpty()){
      throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }

    replyRepository.save(
        Reply.builder()
            .comment(comment.get())
            .user(user.get())
            .post(post.get())
            .content(request.getContent())
            .build()
    );

    post.get().setComments(post.get().getComments() + 1);
    postRepository.save(post.get());

    comment.get().setReply(comment.get().getReply() + 1);
    commentRepository.save(comment.get());

    return ReplyDto.ReplyPostResponse.builder()
        .statusCode("200")
        .postId(id)
        .userId(request.getUserId())
        .content(request.getContent())
        .build();
  }
}