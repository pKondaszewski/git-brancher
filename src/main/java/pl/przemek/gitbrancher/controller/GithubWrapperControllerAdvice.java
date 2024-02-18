package pl.przemek.gitbrancher.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.przemek.gitbrancher.dto.GithubApiErrorDTO;
import pl.przemek.gitbrancher.exception.GithubApiException;

@ControllerAdvice
public class GithubWrapperControllerAdvice {

    @ExceptionHandler(value = {GithubApiException.class})
    protected ResponseEntity<GithubApiErrorDTO> handleGithubException(GithubApiException e) {
        int status = e.getStatus();
        String message = e.getMessage();
        return ResponseEntity.status(status).body(new GithubApiErrorDTO(status, message));
    }
}