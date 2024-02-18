package pl.przemek.gitbrancher.service.github.api;

import pl.przemek.gitbrancher.exception.GithubApiException;

public class GithubApiStatusHandler {
    public static void handle(int statusCode, String responseMessage) {
        if (statusCode == 404) {
            throw new GithubApiException(statusCode, "User not found");
        }
        throw new GithubApiException(statusCode, responseMessage);
    }
}
