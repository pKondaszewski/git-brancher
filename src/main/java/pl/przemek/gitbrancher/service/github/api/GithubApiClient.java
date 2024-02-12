package pl.przemek.gitbrancher.service.github.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import pl.przemek.gitbrancher.dto.BranchDTO;
import pl.przemek.gitbrancher.dto.GithubApiErrorDTO;
import pl.przemek.gitbrancher.dto.GithubApiRepoDTO;
import pl.przemek.gitbrancher.dto.OutputBranchDTO;
import pl.przemek.gitbrancher.exception.GithubApiException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GithubApiClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${github.api.path.get-all-user-repos}")
    private String listRepositoriesForUserUrl;

    public List<GithubApiRepoDTO> fetchAllUserReposInfoWithoutForks(String username) throws GithubApiException, IOException, InterruptedException {
        List<GithubApiRepoDTO> allUserRepoDTOs = getAllUserRepos(username);
        return allUserRepoDTOs.stream()
                .filter(githubApiRepoDTO -> !githubApiRepoDTO.fork())
                .toList();
    }

    public List<OutputBranchDTO> fetchAllRepoBranchesMappedToOutputBranchDTOs(GithubApiRepoDTO repoDTO) throws IOException, InterruptedException, GithubApiException {
        List<BranchDTO> allRepoBranchDTOs = getAllRepoBranches(repoDTO);
        return allRepoBranchDTOs.stream()
                .map(branchDTO -> new OutputBranchDTO(branchDTO.name(), branchDTO.commitDTO().sha()))
                .toList();
    }

    private List<GithubApiRepoDTO> getAllUserRepos(String username) throws IOException, InterruptedException, GithubApiException {
        HttpRequest getAllUserReposRequest = getAllUserReposRequest(username);
        HttpResponse<String> getAllUserReposResponse = httpClient.send(getAllUserReposRequest , HttpResponse.BodyHandlers.ofString());

        int statusCode = getAllUserReposResponse.statusCode();
        String responseBody = getAllUserReposResponse.body();

        checkFor4xxOr5xxGithubResponse(statusCode, responseBody);
        return Arrays.stream(objectMapper.readValue(responseBody, GithubApiRepoDTO[].class)).toList();
    }

    private List<BranchDTO> getAllRepoBranches(GithubApiRepoDTO repoDTO) throws IOException, InterruptedException, GithubApiException {
        HttpRequest getAllRepoBranchesRequest = getAllRepoBranchesRequest(repoDTO);
        HttpResponse<String> getAllRepoBranchesResponse = httpClient.send(getAllRepoBranchesRequest, HttpResponse.BodyHandlers.ofString());

        int statusCode = getAllRepoBranchesResponse.statusCode();
        String responseBody = getAllRepoBranchesResponse.body();

        checkFor4xxOr5xxGithubResponse(statusCode, responseBody);
        return Arrays.stream(objectMapper.readValue(responseBody, BranchDTO[].class)).toList();
    }

    private HttpRequest getAllUserReposRequest(String username) {
        String getAllUserReposUrl = listRepositoriesForUserUrl.formatted(username);
        return HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(getAllUserReposUrl))
                .build();
    }

    private HttpRequest getAllRepoBranchesRequest(GithubApiRepoDTO repoDTO) {
        String getAllRepoBranchesUrl = repoDTO.branchesUrl().replaceAll("\\{/branch}", "");
        return HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(getAllRepoBranchesUrl))
                .build();
    }

    private void checkFor4xxOr5xxGithubResponse(int statusCode, String responseBody) throws GithubApiException, JsonProcessingException {
        HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
        if (httpStatus.is4xxClientError() || httpStatus.is5xxServerError()) {
            GithubApiErrorDTO githubApiErrorDTO = objectMapper.readValue(responseBody, GithubApiErrorDTO.class);
            throw new GithubApiException(statusCode, githubApiErrorDTO.message());
        }
    }
}
