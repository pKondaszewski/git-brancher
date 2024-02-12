package pl.przemek.gitbrancher.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.przemek.gitbrancher.dto.GithubApiErrorDTO;
import pl.przemek.gitbrancher.exception.GithubApiException;

import java.io.IOException;

@ControllerAdvice
public class GithubWrapperControllerExceptionHandler {

    @ExceptionHandler(value = {GithubApiException.class})
    protected ResponseEntity<GithubApiErrorDTO> handleGithubException(GithubApiException e) {
        int status = e.getStatus();
        String message = e.getMessage();
        return ResponseEntity.status(status).body(new GithubApiErrorDTO(status, message));
    }

    @ExceptionHandler(value = {InterruptedException.class, IOException.class})
    protected ResponseEntity<GithubApiErrorDTO> handleMultipleIOException(Exception e) {
        String message = e.getMessage();
        return ResponseEntity.status(500).body(new GithubApiErrorDTO(500, message));
    }
}