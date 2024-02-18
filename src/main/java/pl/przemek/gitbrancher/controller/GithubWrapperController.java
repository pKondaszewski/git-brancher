package pl.przemek.gitbrancher.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.przemek.gitbrancher.dto.out.OutputRepoDTO;
import pl.przemek.gitbrancher.exception.GithubApiException;
import pl.przemek.gitbrancher.service.GithubWrapperService;

import java.util.List;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GithubWrapperController {
    private final GithubWrapperService githubWrapperService;

    @GetMapping(path = "/users/repos-without-forks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OutputRepoDTO>> getAllUserReposWithoutForks(@RequestParam String username) throws GithubApiException {
        return ResponseEntity.ok(githubWrapperService.getAllUserReposWithoutForks(username));
    }
}
