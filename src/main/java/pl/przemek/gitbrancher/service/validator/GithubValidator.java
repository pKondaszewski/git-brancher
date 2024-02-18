package pl.przemek.gitbrancher.service.validator;

import org.springframework.http.HttpStatus;
import pl.przemek.gitbrancher.exception.GithubApiException;

public class GithubValidator {
    public static void validateInput(String username) {
        if (username.isEmpty()) {
            throw new GithubApiException(HttpStatus.BAD_REQUEST.value(), "Username is blank");
        }
    }
}
