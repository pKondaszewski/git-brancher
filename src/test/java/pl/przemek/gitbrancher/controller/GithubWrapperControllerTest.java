package pl.przemek.gitbrancher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import pl.przemek.gitbrancher.dto.*;
import pl.przemek.gitbrancher.dto.out.OutputBranchDTO;
import pl.przemek.gitbrancher.dto.out.OutputRepoDTO;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class GithubWrapperControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @RegisterExtension
    static WireMockExtension wireMockServer =
            WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("github.api.path.base", wireMockServer::baseUrl);
    }

    private static final String getAllUserRepoWithoutForksUrl = "/api/github/users/repos-without-forks?username=%s";

    @Test
    public void shouldReturnCorrectCollectionOfReposWhenFetchAllUserReposInfoWithoutForks() {
        String username = "username";
        String repoName = "repoName";
        mockResponseFromGithubServerForGetAllUserReposRequest(username, repoName);

        BranchDTO branchDTO1 = new BranchDTO("branchName1", new CommitDTO("sha1"));
        BranchDTO branchDTO2 = new BranchDTO("branchName2", new CommitDTO("sha2"));
        List<BranchDTO> mockBranches = List.of(branchDTO1, branchDTO2);
        mockResponseFromGithubServerForGetAllRepoBranchesRequest(username, repoName, mockBranches);

        List<OutputBranchDTO> outputBranchDTOs = List.of(new OutputBranchDTO(branchDTO1.name(), branchDTO1.commitDTO().sha()),
                new OutputBranchDTO(branchDTO2.name(), branchDTO2.commitDTO().sha()));
        List<OutputRepoDTO> outputRepoDTOS = List.of(new OutputRepoDTO(repoName, username, outputBranchDTOs));

        webTestClient
                .get()
                .uri(getAllUserRepoWithoutForksUrl.formatted(username))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(OutputRepoDTO.class)
                .isEqualTo(outputRepoDTOS);
    }

    @Test
    @SneakyThrows
    public void shouldThrowExceptionWhenGithubApiThrowsException() {
        String username = "username";
        GithubApiErrorDTO githubApiErrorDTO = new GithubApiErrorDTO(404, "Not Found");

        wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/users/%s/repos".formatted(username)))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                        .withStatus(404)
                                        .withBody(objectMapper.writeValueAsBytes(githubApiErrorDTO))
                        )
        );

        webTestClient
                .get()
                .uri(getAllUserRepoWithoutForksUrl.formatted(username))
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBodyList(GithubApiErrorDTO.class);
    }

    @SneakyThrows
    private void mockResponseFromGithubServerForGetAllUserReposRequest(String username, String repoName) {
        String githubApiGetAllRepoBranchesUrl = "https://api.github.com/repos/%s/%s/branches{/branch}";
        List<GithubApiRepoDTO> mockRepos = new ArrayList<>();

        mockRepos.add(new GithubApiRepoDTO(repoName, new OwnerDTO(username),
                githubApiGetAllRepoBranchesUrl.formatted(username, repoName), false));
        mockRepos.add(new GithubApiRepoDTO("repoName2", new OwnerDTO(username),
                githubApiGetAllRepoBranchesUrl.formatted(username, "repoName2"), true));

        wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/users/%s/repos".formatted(username)))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                        .withStatus(200)
                                        .withBody(objectMapper.writeValueAsBytes(mockRepos))
                        )
        );
    }

    @SneakyThrows
    private void mockResponseFromGithubServerForGetAllRepoBranchesRequest(String username, String repoName,
                                                                          List<BranchDTO> mockBranches) {
        wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/repos/%s/%s/branches".formatted(username, repoName)))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                        .withStatus(200)
                                        .withBody(objectMapper.writeValueAsBytes(mockBranches))
                        )
        );
    }
}
