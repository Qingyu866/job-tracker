package com.jobtracker.service;

import com.jobtracker.entity.InterviewMessage;
import com.jobtracker.mapper.InterviewMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试对话记录服务
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewMessageService {

    private final InterviewMessageMapper messageMapper;

    /**
     * 添加问题
     */
    @Transactional
    public InterviewMessage addQuestion(String sessionId, Integer roundNumber, String content, Long skillId, String skillName) {
        InterviewMessage message = InterviewMessage.builder()
                .sessionId(sessionId)
                .roundNumber(roundNumber)
                .sequenceInRound(getNextSequence(sessionId, roundNumber))
                .role(InterviewMessage.MessageRole.ASSISTANT.name())
                .content(content)
                .skillId(skillId)
                .skillName(skillName)
                .createdAt(LocalDateTime.now())
                .build();

        messageMapper.insert(message);
        return message;
    }

    /**
     * 添加回答
     */
    @Transactional
    public InterviewMessage addAnswer(String sessionId, Integer roundNumber, String content) {
        InterviewMessage message = InterviewMessage.builder()
                .sessionId(sessionId)
                .roundNumber(roundNumber)
                .sequenceInRound(getNextSequence(sessionId, roundNumber))
                .role(InterviewMessage.MessageRole.USER.name())
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        messageMapper.insert(message);
        return message;
    }

    /**
     * 获取会话的所有消息
     */
    public List<InterviewMessage> getSessionMessages(String sessionId) {
        return messageMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InterviewMessage>()
                        .eq(InterviewMessage::getSessionId, sessionId)
                        .orderByAsc(InterviewMessage::getRoundNumber)
                        .orderByAsc(InterviewMessage::getSequenceInRound)
        );
    }

    /**
     * 获取下一序号
     */
    private Integer getNextSequence(String sessionId, Integer roundNumber) {
        List<InterviewMessage> messages = messageMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InterviewMessage>()
                        .eq(InterviewMessage::getSessionId, sessionId)
                        .eq(InterviewMessage::getRoundNumber, roundNumber)
                        .orderByDesc(InterviewMessage::getSequenceInRound)
        );

        return messages.isEmpty() ? 1 : messages.get(0).getSequenceInRound() + 1;
    }
}
