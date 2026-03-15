package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.ToolCallRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 工具调用记录数据访问层接口
 * <p>
 * 基于 MyBatis Plus 的 BaseMapper，提供对工具调用记录表的 CRUD 操作
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface ToolCallRecordMapper extends BaseMapper<ToolCallRecord> {

    /**
     * 根据消息ID查询工具调用记录
     *
     * @param messageId 消息ID
     * @return 工具调用记录列表
     */
    default List<ToolCallRecord> selectByMessageId(Long messageId) {
        return selectList(new LambdaQueryWrapper<ToolCallRecord>()
                .eq(ToolCallRecord::getMessageId, messageId)
                .orderByAsc(ToolCallRecord::getCreatedAt));
    }
}
