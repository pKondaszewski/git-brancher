package pl.przemek.gitbrancher.service.github.api;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.przemek.gitbrancher.dto.GithubApiRepoDTO;
import pl.przemek.gitbrancher.dto.OutputBranchDTO;
import pl.przemek.gitbrancher.dto.OwnerDTO;
import pl.przemek.gitbrancher.exception.GithubApiException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GithubApiClientTest {
    @Mock
    private HttpClient httpClient;
    @Mock
    private HttpResponse<String> httpResponse;
    @InjectMocks
    private GithubApiClient githubApiClient;

    private static String sampleGetAllRepoInfoJsonResponse;
    private static String sampleGetAllBranchesInfoJsonResponse;
    private static String sampleApiErrorJsonResponse;
    private static GithubApiRepoDTO sampleGetAllRepoInfoDTOResponse;
    private static OutputBranchDTO sampleGetAllBranchesInfoDTOResponse;

    @BeforeAll
    static void initVars() {
        sampleGetAllRepoInfoJsonResponse =
                "[{\"name\":\"usernameRepo1\",\"owner\":{\"login\":\"username\"},\"branches_url\":\"https://test.test/repos/username/usernameRepo1/branches{/branch}\",\"fork\":false}," +
                "{\"name\":\"usernameRepo2\",\"owner\":{\"login\":\"username\"},\"branches_url\":\"https://test.test/repos/username/usernameRepo2/branches{/branch}\",\"fork\":true}," +
                "{\"name\":\"usernameRepo3\",\"owner\":{\"login\":\"username\"},\"branches_url\":\"https://test.test/repos/username/usernameRepo3/branches{/branch}\",\"fork\":false}]";
        sampleGetAllBranchesInfoJsonResponse =
                "[{\"name\":\"main\",\"commit\":{\"sha\":\"123\"}}," +
                "{\"name\":\"develop\",\"commit\":{\"sha\":\"456\"}}," +
                "{\"name\":\"feature\",\"commit\":{\"sha\":\"789\"}}]";
        sampleApiErrorJsonResponse = "{\"message\":\"Not Found\"}";

        sampleGetAllRepoInfoDTOResponse = new GithubApiRepoDTO("usernameRepo1", new OwnerDTO("username"),
                "https://test.test/repos/username/usernameRepo1/branches{/branch}",false);
        sampleGetAllBranchesInfoDTOResponse = new OutputBranchDTO("main", "123");
    }

    @BeforeEach
    void setUpProps() {
        ReflectionTestUtils.setField(githubApiClient, "listRepositoriesForUserUrl", "https://test.test/users/username/repos");
    }

    @Test
    void shouldReturnCorrectCollectionOfReposWhenFetchAllUserReposInfoWithoutForks() throws Exception {
        // Should
        HttpRequest getAllReposHttpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://test.test/users/username/repos"))
                .build();
        when(httpClient.send(getAllReposHttpRequest, HttpResponse.BodyHandlers.ofString())).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(sampleGetAllRepoInfoJsonResponse);
        // When
        List<GithubApiRepoDTO> githubApiRepoDTOList = githubApiClient.fetchAllUserReposInfoWithoutForks("username");
        // Then
        assertEquals(2, githubApiRepoDTOList.size());
        assertEquals(sampleGetAllRepoInfoDTOResponse, githubApiRepoDTOList.getFirst());
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() throws Exception {
        // Should
        when(httpClient.send(any(), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(404);
        when(httpResponse.body()).thenReturn(sampleApiErrorJsonResponse);
        // When
        GithubApiException exception = assertThrows(GithubApiException.class, () ->
                githubApiClient.fetchAllUserReposInfoWithoutForks("")
        );
        // Then
        assertEquals(404, exception.getStatus());
        assertEquals("Not Found", exception.getMessage());
    }

    @Test
    void shouldReturnCorrectCollectionOfBranchesWhenFetchAllRepoBranchesMappedToOutputBranchDTOs() throws Exception {
        // Should
        HttpRequest getAllReposHttpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://test.test/repos/username/usernameRepo1/branches"))
                .build();
        when(httpClient.send(getAllReposHttpRequest, HttpResponse.BodyHandlers.ofString())).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(sampleGetAllBranchesInfoJsonResponse);
        // When
        List<OutputBranchDTO> outputBranchDTOS = githubApiClient.fetchAllRepoBranchesMappedToOutputBranchDTOs(sampleGetAllRepoInfoDTOResponse);
        // Then
        assertEquals(3, outputBranchDTOS.size());
        assertEquals(sampleGetAllBranchesInfoDTOResponse, outputBranchDTOS.getFirst());
    }
}
