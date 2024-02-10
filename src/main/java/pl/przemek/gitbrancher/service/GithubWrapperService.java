package pl.przemek.gitbrancher.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.przemek.gitbrancher.dto.OutputRepoDTO;
import pl.przemek.gitbrancher.exception.GithubApiException;
import pl.przemek.gitbrancher.service.github.api.GithubApiClient;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GithubWrapperService {

    private final GithubApiClient githubApiClient;
    public List<OutputRepoDTO> getAllUserReposWithoutForks(String username) throws GithubApiException, IOException,
            InterruptedException {
        return githubApiClient.getAllUserReposWithoutForks(username);
    }
}
