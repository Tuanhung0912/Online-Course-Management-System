package com.example.LMS.service;

import com.example.LMS.model.Comment;
import com.example.LMS.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    public List<Comment> getCommentsByCourse(Long courseId) {
        return commentRepository.findByCourseId(courseId);
    }

    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bình luận với ID: " + commentId));
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    public Map<String, Object> getAverageRatingAndCountByCourseId(Long courseId) {
        List<Comment> comments = commentRepository.findByCourseId(courseId);
        double averageRating = comments.stream()
                .mapToInt(Comment::getRating)
                .average()
                .orElse(0.0);
        int ratingCount = comments.size();

        Map<String, Object> result = new HashMap<>();
        result.put("averageRating", averageRating);
        result.put("ratingCount", ratingCount);

        return result;
    }

}
