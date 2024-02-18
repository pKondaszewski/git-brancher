package pl.przemek.gitbrancher.dto;

public record GithubApiErrorDTO(int status,
                                String message) {
}
