package pl.przemek.gitbrancher.service.github.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.przemek.gitbrancher.dto.*;
import pl.przemek.gitbrancher.exception.GithubApiException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class GithubApiClient {
    private final HttpClient httpClient;
    private final String listRepositoriesForUserUrl;
    private final ObjectMapper objectMapper;

    public GithubApiClient(HttpClient httpClient, @Value("${github.api.path.get-all-user-repos}") String listRepositoriesForUserUrl) {
        this.httpClient = httpClient;
        this.listRepositoriesForUserUrl = listRepositoriesForUserUrl;
        this.objectMapper = new ObjectMapper();
    }

    public List<OutputRepoDTO> getAllUserReposWithoutForks(String username) throws GithubApiException, IOException,
            InterruptedException {
        List<GithubApiRepoDTO> allUserRepoDTOs = getAllUserRepos(username);
        List<GithubApiRepoDTO> allUserRepoDTOsWithoutForks = allUserRepoDTOs.stream()
                .filter(githubApiRepoDTO -> !githubApiRepoDTO.fork())
                .toList();

        ArrayList<OutputRepoDTO> allUserOutputRepoDTOsWithoutForks = new ArrayList<>();
        for (GithubApiRepoDTO repoDTO : allUserRepoDTOsWithoutForks) {
            List<BranchDTO> allRepoBranchDTOs = getAllRepoBranches(repoDTO);
            List<OutputBranchDTO> outputBranchDTOs = allRepoBranchDTOs.stream()
                    .map(branchDTO -> new OutputBranchDTO(branchDTO.name(), branchDTO.commitDTO().sha()))
                    .toList();

            OutputRepoDTO outputRepoDTO = new OutputRepoDTO(repoDTO.name(), repoDTO.ownerDTO().login(), outputBranchDTOs);
            allUserOutputRepoDTOsWithoutForks.add(outputRepoDTO);
        }
        return allUserOutputRepoDTOsWithoutForks;
    }

    private List<GithubApiRepoDTO> getAllUserRepos(String username) throws IOException, InterruptedException, GithubApiException {
        HttpRequest getAllUserReposRequest = getAllUserReposRequest(username);
        HttpResponse<String> getAllUserReposResponse = httpClient.send(getAllUserReposRequest , HttpResponse.BodyHandlers.ofString());
        int statusCode = getAllUserReposResponse.statusCode();
        if (statusCode == 404) {
            throw new GithubApiException(statusCode, "Github user not found");
        }
        return Arrays.stream(objectMapper.readValue(getAllUserReposResponse.body(), GithubApiRepoDTO[].class)).toList();
    }

    private List<BranchDTO> getAllRepoBranches(GithubApiRepoDTO repoDTO) throws IOException, InterruptedException {
        HttpRequest getAllRepoBranchesRequest = getAllRepoBranchesRequest(repoDTO);
        HttpResponse<String> getAllRepoBranchesResponse = httpClient.send(getAllRepoBranchesRequest, HttpResponse.BodyHandlers.ofString());
        return Arrays.stream(objectMapper.readValue(getAllRepoBranchesResponse.body(), BranchDTO[].class)).toList();
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
}
