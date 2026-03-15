package com.jobtracker.agent.tools.shared;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

/**
 * 时间工具类
 * <p>
 * 提供 LangChain4j Agent 可调用的时间相关方法
 * 解决 AI 模型不知道当前时间的问题
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-14
 */
@Slf4j
@Component
public class TimeTools {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter FRIENDLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日 EEEE HH:mm");

    /**
     * 获取当前时间
     */
    @Tool("""
        [时间] 获取当前日期和时间

        适用场景：
        - 用户说"今天"、"明天"、"后天"等相对时间
        - 需要知道当前是哪一天
        - 计算面试还有几天

        返回：完整的当前时间信息，包括日期、星期、时间
        """)
    public ToolResult getCurrentTime() {
        log.info("AI调用：获取当前时间");

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        StringBuilder info = new StringBuilder();
        info.append("**当前时间信息**\n\n");
        info.append(String.format("- 日期：%s\n", now.format(DATE_FORMATTER)));
        info.append(String.format("- 时间：%s\n", now.format(DateTimeFormatter.ofPattern("HH:mm"))));
        info.append(String.format("- 星期：%s\n", getChineseDayOfWeek(today.getDayOfWeek())));
        info.append(String.format("- 友好格式：%s\n", now.format(FRIENDLY_FORMATTER)));

        // 添加相对日期参考
        info.append("\n**相对日期参考**\n");
        info.append(String.format("- 今天：%s\n", today.format(DATE_FORMATTER)));
        info.append(String.format("- 明天：%s\n", today.plusDays(1).format(DATE_FORMATTER)));
        info.append(String.format("- 后天：%s\n", today.plusDays(2).format(DATE_FORMATTER)));
        info.append(String.format("- 本周末：%s\n",
            today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).format(DATE_FORMATTER)));
        info.append(String.format("- 下周一：%s\n",
            today.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).format(DATE_FORMATTER)));

        return ToolResult.success(info.toString(), Map.of(
            "date", today.format(DATE_FORMATTER),
            "time", now.format(DateTimeFormatter.ofPattern("HH:mm")),
            "datetime", now.format(DATETIME_FORMATTER),
            "dayOfWeek", getChineseDayOfWeek(today.getDayOfWeek()),
            "today", today.format(DATE_FORMATTER),
            "tomorrow", today.plusDays(1).format(DATE_FORMATTER)
        ));
    }

    /**
     * 计算日期差
     */
    @Tool("""
        [时间] 计算两个日期之间的天数

        适用场景：
        - 用户问"面试还有几天"
        - 计算"离入职还有多少天"

        参数：
        - targetDate: 目标日期（格式：yyyy-MM-dd）

        返回：距离目标日期的天数
        """)
    public ToolResult calculateDaysUntil(String targetDate) {
        log.info("AI调用：计算日期差 targetDate={}", targetDate);

        if (targetDate == null || targetDate.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "请提供目标日期");
        }

        try {
            LocalDate target = LocalDate.parse(targetDate, DATE_FORMATTER);
            LocalDate today = LocalDate.now();

            long days = java.time.temporal.ChronoUnit.DAYS.between(today, target);

            String direction = days >= 0 ? "之后" : "之前";
            long absDays = Math.abs(days);

            String message;
            if (days == 0) {
                message = "就是今天！";
            } else if (days > 0) {
                message = String.format("距离 %s 还有 %d 天", targetDate, absDays);
            } else {
                message = String.format("%s 已经是 %d 天前的事了", targetDate, absDays);
            }

            return ToolResult.success(message, Map.of(
                "targetDate", targetDate,
                "days", days,
                "isPast", days < 0,
                "isToday", days == 0
            ));
        } catch (Exception e) {
            return ToolResult.error(ToolConstants.ERR_INVALID_FORMAT,
                "日期格式错误，请使用 yyyy-MM-dd 格式");
        }
    }

    /**
     * 解析相对时间为具体日期
     */
    @Tool("""
        [时间] 将相对时间转换为具体日期

        适用场景：
        - 用户说"下周三"、"大后天"、"下个月1号"

        参数：
        - relativeTime: 相对时间描述
          支持：今天、明天、后天、大后天、昨天、前天
          支持：下周X、本周X、下个月X号

        返回：具体的日期（yyyy-MM-dd格式）
        """)
    public ToolResult parseRelativeTime(String relativeTime) {
        log.info("AI调用：解析相对时间 relativeTime={}", relativeTime);

        if (relativeTime == null || relativeTime.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "请提供相对时间描述");
        }

        LocalDate today = LocalDate.now();
        String input = relativeTime.toLowerCase().trim();
        LocalDate result = null;

        // 基本相对时间
        if (input.contains("今天") || input.equals("today")) {
            result = today;
        } else if (input.contains("明天") || input.equals("tomorrow")) {
            result = today.plusDays(1);
        } else if (input.contains("后天")) {
            result = today.plusDays(2);
        } else if (input.contains("大后天")) {
            result = today.plusDays(3);
        } else if (input.contains("昨天") || input.equals("yesterday")) {
            result = today.minusDays(1);
        } else if (input.contains("前天")) {
            result = today.minusDays(2);
        }
        // 下周X
        else if (input.contains("下周")) {
            result = parseNextWeekDay(input, today);
        }
        // 本周X
        else if (input.contains("本周") || input.contains("这周")) {
            result = parseThisWeekDay(input, today);
        }
        // N天后
        else if (input.matches(".*\\d+天后.*")) {
            int days = extractNumber(input);
            result = today.plusDays(days);
        }
        // N天前
        else if (input.matches(".*\\d+天前.*")) {
            int days = extractNumber(input);
            result = today.minusDays(days);
        }

        if (result == null) {
            return ToolResult.error("无法解析",
                String.format("无法理解时间描述：\"%s\"，请使用更明确的表达", relativeTime));
        }

        return ToolResult.success(
            String.format("\"%s\" 对应的日期是：%s（%s）",
                relativeTime,
                result.format(DATE_FORMATTER),
                getChineseDayOfWeek(result.getDayOfWeek())),
            result.format(DATE_FORMATTER)
        );
    }

    // ========== 辅助方法 ==========

    private String getChineseDayOfWeek(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "星期一";
            case TUESDAY -> "星期二";
            case WEDNESDAY -> "星期三";
            case THURSDAY -> "星期四";
            case FRIDAY -> "星期五";
            case SATURDAY -> "星期六";
            case SUNDAY -> "星期日";
        };
    }

    private LocalDate parseNextWeekDay(String input, LocalDate today) {
        DayOfWeek targetDay = extractDayOfWeek(input);
        if (targetDay == null) return null;
        return today.with(TemporalAdjusters.next(targetDay));
    }

    private LocalDate parseThisWeekDay(String input, LocalDate today) {
        DayOfWeek targetDay = extractDayOfWeek(input);
        if (targetDay == null) return null;
        return today.with(TemporalAdjusters.nextOrSame(targetDay));
    }

    private DayOfWeek extractDayOfWeek(String input) {
        if (input.contains("一") || input.contains("1")) return DayOfWeek.MONDAY;
        if (input.contains("二") || input.contains("2")) return DayOfWeek.TUESDAY;
        if (input.contains("三") || input.contains("3")) return DayOfWeek.WEDNESDAY;
        if (input.contains("四") || input.contains("4")) return DayOfWeek.THURSDAY;
        if (input.contains("五") || input.contains("5")) return DayOfWeek.FRIDAY;
        if (input.contains("六") || input.contains("6")) return DayOfWeek.SATURDAY;
        if (input.contains("日") || input.contains("天") || input.contains("7")) return DayOfWeek.SUNDAY;
        return null;
    }

    private int extractNumber(String input) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)").matcher(input);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }
}
