package pl.przemek.gitbrancher.exception;

import lombok.Getter;

@Getter
public class GithubApiException extends RuntimeException {
    private final int status;
    private final String message;

    public GithubApiException(int status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }
}
