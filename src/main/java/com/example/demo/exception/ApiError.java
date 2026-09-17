package com.example.demo.exception;

public record ApiError(
    String error,
    String message,
    Integer status
) {}
