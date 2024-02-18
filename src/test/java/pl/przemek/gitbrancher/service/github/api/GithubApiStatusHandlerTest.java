package pl.przemek.gitbrancher.service.github.api;

import org.junit.jupiter.api.Test;
import pl.przemek.gitbrancher.exception.GithubApiException;
import wiremock.org.eclipse.jetty.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GithubApiStatusHandlerTest {

    @Test
    void shouldThrowCorrectExceptionWhenApiReturnsUserNotFoundException() {
        // when
        GithubApiException exception = assertThrows(GithubApiException.class,
                () -> GithubApiStatusHandler.handle(HttpStatus.NOT_FOUND_404, "random response message"));

        // then
        assertEquals(HttpStatus.NOT_FOUND_404, exception.getStatus());
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldThrowCorrectExceptionWhenApiReturnsUnexpectedError() {
        // given
        String exceptionMessage = "rate limit exceeded";

        // when
        GithubApiException exception = assertThrows(GithubApiException.class,
                () -> GithubApiStatusHandler.handle(HttpStatus.FORBIDDEN_403, exceptionMessage));

        // then
        assertEquals(HttpStatus.FORBIDDEN_403, exception.getStatus());
        assertEquals(exceptionMessage, exception.getMessage());
    }
}
