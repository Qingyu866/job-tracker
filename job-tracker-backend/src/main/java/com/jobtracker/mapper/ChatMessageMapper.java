package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 聊天消息数据访问层接口
 * <p>
 * 基于 MyBatis Plus 的 BaseMapper，提供对消息表的 CRUD 操作
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 根据会话ID查询消息列表（按时间升序）
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    default List<ChatMessage> selectBySessionId(Long sessionId) {
        return selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getCreatedAt));
    }
}
