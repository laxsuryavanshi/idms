package com.turtleby.idms.web.common.dto;

import java.util.Map;

public record ErrorResponse(String code, int status, String message, Map<String, Object> details) {}
