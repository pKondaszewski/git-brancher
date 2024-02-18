package pl.przemek.gitbrancher.service.github.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pl.przemek.gitbrancher.dto.BranchDTO;
import pl.przemek.gitbrancher.dto.GithubApiRepoDTO;
import pl.przemek.gitbrancher.dto.out.OutputBranchDTO;
import pl.przemek.gitbrancher.exception.GithubApiException;

import java.util.Arrays;
import java.util.List;

@Component
public class GithubApiClient {
    private final String listRepositoriesForUserRoute;
    private final String baseClientUrl;
    private final RestClient restClient;

    public GithubApiClient(@Value("${github.api.path.routes.get-all-user-repos}") String listRepositoriesForUserRoute,
                           @Value("${github.api.path.base}") String baseClientUrl,
                           RestClient restClient) {
        this.listRepositoriesForUserRoute = listRepositoriesForUserRoute;
        this.baseClientUrl = baseClientUrl;
        this.restClient = restClient;
    }

    public List<GithubApiRepoDTO> fetchAllUserReposInfoWithoutForks(String username) throws GithubApiException {
        GithubApiRepoDTO[] allUserRepoDTOs = getAllUserRepos(username);
        return Arrays.stream(allUserRepoDTOs)
                .filter(githubApiRepoDTO -> !githubApiRepoDTO.fork())
                .toList();
    }

    public List<OutputBranchDTO> fetchAllRepoBranchesMappedToOutputBranchDTOs(GithubApiRepoDTO repoDTO) throws GithubApiException {
        BranchDTO[] allRepoBranchDTOs = getAllRepoBranches(repoDTO);
        return Arrays.stream(allRepoBranchDTOs)
                .map(branchDTO -> new OutputBranchDTO(branchDTO.name(), branchDTO.commitDTO().sha()))
                .toList();
    }

    private GithubApiRepoDTO[] getAllUserRepos(String username) throws GithubApiException {
        String getAllUserReposRoute = listRepositoriesForUserRoute.formatted(username);
        return restClient
                .get()
                .uri(getAllUserReposRoute)
                .retrieve()
                .body(GithubApiRepoDTO[].class);
    }

    private BranchDTO[] getAllRepoBranches(GithubApiRepoDTO repoDTO) throws GithubApiException {
        String branchesUrl = repoDTO.branchesUrl();
        String getAllRepoBranchesRoute = extractRouteFromBranchesUrl(branchesUrl);
        return restClient
                .get()
                .uri(getAllRepoBranchesRoute)
                .retrieve()
                .body(BranchDTO[].class);
    }

    private String extractRouteFromBranchesUrl(String branchesUrl) {
        return branchesUrl.substring(baseClientUrl.length(), branchesUrl.indexOf("{"));
    }
}
