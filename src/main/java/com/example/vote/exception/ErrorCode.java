package com.example.vote.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    
    VOTE_NOT_FOUND(1001, "投票不存在"),
    VOTE_CLOSED(1002, "投票已关闭"),
    VOTE_DEADLINE_PASSED(1003, "投票已截止"),
    DUPLICATE_VOTE(1004, "您已经投过票了"),
    INVALID_OPTION(1005, "无效的选项"),
    EXCEED_MAX_CHOICES(1006, "超过最大选择数量"),
    SINGLE_CHOICE_ONLY(1007, "该投票仅支持单选");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
