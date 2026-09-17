package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(RecursoNoEncontradoException ex) {
        return new ApiError("No Encontrado", ex.getMessage(), 404);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationErrors(MethodArgumentNotValidException ex) {

        String errorMsg = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return new ApiError("Error de Validación", errorMsg, 400);
    }
}
