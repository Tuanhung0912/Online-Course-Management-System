package com.example.LMS.service;

import com.example.LMS.model.Reply;
import com.example.LMS.repository.ReplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReplyService {
    @Autowired
    private ReplyRepository replyRepository;

    public Reply saveReply(Reply reply) {
        return replyRepository.save(reply);
    }

    public List<Reply> getRepliesByCommentId(Long commentId) {
        return replyRepository.findAll()
                .stream()
                .filter(reply -> reply.getComment().getId().equals(commentId))
                .toList();
    }
}
