package pl.przemek.gitbrancher.service.github.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import pl.przemek.gitbrancher.config.GitBrancherConfig;
import pl.przemek.gitbrancher.dto.BranchDTO;
import pl.przemek.gitbrancher.dto.CommitDTO;
import pl.przemek.gitbrancher.dto.GithubApiRepoDTO;
import pl.przemek.gitbrancher.dto.OwnerDTO;
import pl.przemek.gitbrancher.dto.out.OutputBranchDTO;
import pl.przemek.gitbrancher.exception.GithubApiException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(GithubApiClient.class)
@Import(GitBrancherConfig.class)
public class GithubApiClientTest {
    @Value("${github.api.path.base}")
    private String baseGithubApiUrl;
    @Value("${github.api.path.routes.get-all-user-repos}")
    private String listRepositoriesForUserRoute;
    @Autowired
    private MockRestServiceServer mockServer;
    @Autowired
    private GithubApiClient githubApiClient;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldFetchAllUserReposInfoWithoutForksWhenApiReturnsSuccess() throws GithubApiException, JsonProcessingException {
        // given
        String username = "username";
        OwnerDTO ownerDTO = new OwnerDTO(username);
        GithubApiRepoDTO[] mockRepos = {
                new GithubApiRepoDTO("repo1", ownerDTO, null, false),
                new GithubApiRepoDTO("repo2", ownerDTO, null, true),
                new GithubApiRepoDTO("repo3", ownerDTO, null, true)
        };
        String expectedUrl = baseGithubApiUrl + listRepositoriesForUserRoute.formatted(username);
        mockServer.expect(requestTo(expectedUrl))
                .andRespond(withSuccess(objectMapper.writeValueAsBytes(mockRepos), MediaType.APPLICATION_JSON));

        // when
        List<GithubApiRepoDTO> result = githubApiClient.fetchAllUserReposInfoWithoutForks(username);

        // then
        assertEquals(1, result.size());
        assertEquals(mockRepos[0], result.getFirst());
    }

    @Test
    public void shouldFetchAllRepoBranchesMappedToOutputBranchDTOsWhenApiReturnsSuccess() throws GithubApiException, JsonProcessingException {
        // given
        GithubApiRepoDTO repoDTO = new GithubApiRepoDTO("repo", null, baseGithubApiUrl + "{/branch}", true);
        CommitDTO commitDTO = new CommitDTO("sha");
        BranchDTO[] mockBranches = {
                new BranchDTO("commit1", commitDTO),
                new BranchDTO("commit2", commitDTO)
        };
        mockServer.expect(requestTo(baseGithubApiUrl))
                .andRespond(withSuccess(objectMapper.writeValueAsBytes(mockBranches), MediaType.APPLICATION_JSON));

        // when
        List<OutputBranchDTO> result = githubApiClient.fetchAllRepoBranchesMappedToOutputBranchDTOs(repoDTO);

        // then
        assertEquals(mockBranches.length, result.size());
        assertEquals(mockBranches[0].name(), result.getFirst().name());
        assertEquals(mockBranches[1].commitDTO().sha(), result.getLast().lastCommitSha());
    }

    @Test
    public void shouldThrowExceptionWhenApiReturnsUserNotFound() throws GithubApiException, JsonProcessingException {
        // given
        String username = "username";
        String expectedUrl = baseGithubApiUrl + listRepositoriesForUserRoute.formatted(username);
        GithubApiException e = new GithubApiException(404, "User not found");
        mockServer.expect(requestTo(expectedUrl))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsBytes(e)));

        // when
        GithubApiException result = assertThrows(GithubApiException.class,
                () -> githubApiClient.fetchAllUserReposInfoWithoutForks(username));

        // then
        assertEquals(e.getStatus(), result.getStatus());
        assertEquals(e.getMessage(), result.getMessage());
    }
}