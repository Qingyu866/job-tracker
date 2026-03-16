package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.OcrCallRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * OCR 调用记录 Mapper
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Mapper
public interface OcrCallRecordMapper extends BaseMapper<OcrCallRecord> {
}
