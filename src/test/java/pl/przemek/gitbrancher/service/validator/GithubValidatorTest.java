package pl.przemek.gitbrancher.service.validator;

import org.junit.jupiter.api.Test;
import pl.przemek.gitbrancher.exception.GithubApiException;

import static org.junit.jupiter.api.Assertions.*;

public class GithubValidatorTest {

    @Test
    void shouldThrowCorrectExceptionWhenUsernameIsNotGiven() {
        // when
        GithubApiException exception = assertThrows(GithubApiException.class,
                () -> GithubValidator.validateInput(""));

        // then
        assertEquals(400, exception.getStatus());
        assertEquals("Username is blank", exception.getMessage());
    }

    @Test
    void shouldNotThrowExceptionWhenUsernameIsGiven() {
        assertDoesNotThrow(() -> GithubValidator.validateInput("username"));
    }
}
