package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 聊天会话数据访问层接口
 * <p>
 * 基于 MyBatis Plus 的 BaseMapper，提供对会话表的 CRUD 操作
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

    /**
     * 根据会话标识查询会话
     *
     * @param sessionKey 会话标识
     * @return 会话对象
     */
    default ChatSession selectBySessionKey(String sessionKey) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getSessionKey, sessionKey));
    }

    /**
     * 增加消息计数
     *
     * @param sessionId 会话ID
     * @return 影响行数
     */
    @Update("UPDATE chat_sessions SET message_count = message_count + 1, updated_at = NOW() WHERE id = #{sessionId}")
    int incrementMessageCount(@Param("sessionId") Long sessionId);

    /**
     * 根据会话标识删除会话（逻辑删除）
     *
     * @param sessionKey 会话标识
     * @return 影响行数
     */
    default int deleteBySessionKey(String sessionKey) {
        return delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getSessionKey, sessionKey));
    }
}
