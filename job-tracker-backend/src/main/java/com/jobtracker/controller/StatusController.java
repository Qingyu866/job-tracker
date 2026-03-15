package com.jobtracker.controller;

import com.jobtracker.common.result.Result;
import com.jobtracker.constants.ApplicationStatus;
import com.jobtracker.constants.InterviewStatus;
import com.jobtracker.constants.TransitionRules;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 状态转换控制器
 * <p>
 * 提供状态转换规则查询 API
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/status")
@RequiredArgsConstructor
public class StatusController {

    /**
     * 获取所有申请状态的合法转换规则
     * GET /api/status/transitions
     */
    @GetMapping("/transitions")
    public Result<Map<String, List<String>>> getApplicationTransitions() {
        Map<String, List<String>> transitions = new HashMap<>();

        for (ApplicationStatus status : ApplicationStatus.values()) {
            Set<ApplicationStatus> nextStatuses = TransitionRules.getNextPossibleStatuses(status);
            List<String> codes = nextStatuses.stream()
                .map(ApplicationStatus::getCode)
                .collect(Collectors.toList());
            transitions.put(status.getCode(), codes);
        }

        return Result.success(transitions);
    }

    /**
     * 获取所有面试状态的合法转换规则
     * GET /api/status/interview/transitions
     */
    @GetMapping("/interview/transitions")
    public Result<Map<String, List<String>>> getInterviewTransitions() {
        Map<String, List<String>> transitions = new HashMap<>();

        for (InterviewStatus status : InterviewStatus.values()) {
            Set<InterviewStatus> nextStatuses = TransitionRules.getNextPossibleStatuses(status);
            List<String> codes = nextStatuses.stream()
                .map(InterviewStatus::getCode)
                .collect(Collectors.toList());
            transitions.put(status.getCode(), codes);
        }

        return Result.success(transitions);
    }

    /**
     * 获取申请的下一个可能状态
     * GET /api/status/applications/{status}/next
     */
    @GetMapping("/applications/{status}/next")
    public Result<List<String>> getNextApplicationStatuses(@PathVariable String status) {
        ApplicationStatus currentStatus = ApplicationStatus.fromCode(status);
        if (currentStatus == null) {
            return Result.error("无效的状态: " + status);
        }

        Set<ApplicationStatus> nextStatuses = TransitionRules.getNextPossibleStatuses(currentStatus);
        List<String> codes = nextStatuses.stream()
                .map(ApplicationStatus::getCode)
                .collect(Collectors.toList());

        return Result.success(codes);
    }

    /**
     * 获取面试的下一个可能状态
     * GET /api/status/interview/{status}/next
     */
    @GetMapping("/interview/{status}/next")
    public Result<List<String>> getNextInterviewStatuses(@PathVariable String status) {
        InterviewStatus currentStatus = InterviewStatus.fromCode(status);
        if (currentStatus == null) {
            return Result.error("无效的状态: " + status);
        }

        Set<InterviewStatus> nextStatuses = TransitionRules.getNextPossibleStatuses(currentStatus);
        List<String> codes = nextStatuses.stream()
                .map(InterviewStatus::getCode)
                .collect(Collectors.toList());

        return Result.success(codes);
    }

    /**
     * 验证申请状态转换是否合法
     * GET /api/status/applications/validate?from={from}&to={to}
     */
    @GetMapping("/applications/validate")
    public Result<Boolean> validateApplicationTransition(
            @RequestParam String from,
            @RequestParam String to) {
        ApplicationStatus fromStatus = ApplicationStatus.fromCode(from);
        ApplicationStatus toStatus = ApplicationStatus.fromCode(to);

        if (fromStatus == null) {
            return Result.error("无效的源状态: " + from);
        }
        if (toStatus == null) {
            return Result.error("无效的目标状态: " + to);
        }

        boolean canTransition = TransitionRules.canTransition(fromStatus, toStatus);
        return Result.success(canTransition);
    }

    /**
     * 验证面试状态转换是否合法
     * GET /api/status/interview/validate?from={from}&to={to}
     */
    @GetMapping("/interview/validate")
    public Result<Boolean> validateInterviewTransition(
            @RequestParam String from,
            @RequestParam String to) {
        InterviewStatus fromStatus = InterviewStatus.fromCode(from);
        InterviewStatus toStatus = InterviewStatus.fromCode(to);

        if (fromStatus == null) {
            return Result.error("无效的源状态: " + from);
        }
        if (toStatus == null) {
            return Result.error("无效的目标状态: " + to);
        }

        boolean canTransition = TransitionRules.canTransition(fromStatus, toStatus);
        return Result.success(canTransition);
    }

    /**
     * 获取所有申请状态列表
     * GET /api/status/applications
     */
    @GetMapping("/applications")
    public Result<List<Map<String, Object>>> getAllApplicationStatuses() {
        List<Map<String, Object>> statuses = new ArrayList<>();

        for (ApplicationStatus status : ApplicationStatus.values()) {
            Map<String, Object> info = new HashMap<>();
            info.put("code", status.getCode());
            info.put("description", status.getDescription());
            info.put("stage", status.getStage().name());
            info.put("isTerminal", status.isTerminal());
            info.put("canScheduleInterview", status.canScheduleInterview());

            List<String> nextCodes = TransitionRules.getNextPossibleStatuses(status).stream()
                .map(ApplicationStatus::getCode)
                .collect(Collectors.toList());
            info.put("nextStatuses", nextCodes);

            statuses.add(info);
        }

        return Result.success(statuses);
    }

    /**
     * 获取所有面试状态列表
     * GET /api/status/interview
     */
    @GetMapping("/interview")
    public Result<List<Map<String, Object>>> getAllInterviewStatuses() {
        List<Map<String, Object>> statuses = new ArrayList<>();

        for (InterviewStatus status : InterviewStatus.values()) {
            Map<String, Object> info = new HashMap<>();
            info.put("code", status.getCode());
            info.put("description", status.getDescription());
            info.put("isTerminal", status.isTerminal());

            List<String> nextCodes = TransitionRules.getNextPossibleStatuses(status).stream()
                .map(InterviewStatus::getCode)
                .collect(Collectors.toList());
            info.put("nextStatuses", nextCodes);

            statuses.add(info);
        }

        return Result.success(statuses);
    }
}
