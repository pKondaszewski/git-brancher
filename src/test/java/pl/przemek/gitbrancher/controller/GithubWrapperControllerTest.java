package pl.przemek.gitbrancher.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import pl.przemek.gitbrancher.dto.OutputBranchDTO;
import pl.przemek.gitbrancher.dto.OutputRepoDTO;
import pl.przemek.gitbrancher.exception.GithubApiException;
import pl.przemek.gitbrancher.service.GithubWrapperService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = GithubWrapperController.class)
public class GithubWrapperControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private GithubWrapperService githubWrapperService;

    private String repoName1 = "repo1";
    private String repoName2 = "repo2";
    private String descriptionName1 = "description1";
    private String descriptionName2 = "description2";
    private String commitName1 = "commit1";
    private String shaName1 = "sha1";

    @Test
    public void shouldReturnCorrectCollectionOfReposWhenGetAllUserReposWithoutForks() throws Exception {
        // Should
        List<OutputRepoDTO> mockRepos = new ArrayList<>();
        mockRepos.add(new OutputRepoDTO(repoName1, descriptionName1, List.of(new OutputBranchDTO(commitName1, shaName1))));
        mockRepos.add(new OutputRepoDTO(repoName2, descriptionName2, List.of()));
        when(githubWrapperService.getAllUserReposWithoutForks(anyString())).thenReturn(mockRepos);
        // When & Then
        mockMvc.perform(get("/api/github/users/repos-without-forks?username=testuser")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].repositoryName").value(repoName1))
                .andExpect(jsonPath("$[0].ownerLogin").value(descriptionName1))
                .andExpect(jsonPath("$[0].branches[0].name").value(commitName1))
                .andExpect(jsonPath("$[0].branches[0].lastCommitSha").value(shaName1))
                .andExpect(jsonPath("$[1].repositoryName").value(repoName2))
                .andExpect(jsonPath("$[1].ownerLogin").value(descriptionName2))
                .andExpect(jsonPath("$[1].branches").isEmpty());
    }

    @Test
    public void shouldOutputExceptionWhenUserNotFoundInGithub() throws Exception {
        // Should
        when(githubWrapperService.getAllUserReposWithoutForks(anyString())).thenThrow(new GithubApiException(404, "Not Found"));
        // When & Then
        mockMvc.perform(get("/api/github/users/repos-without-forks?username=testuser")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    public void shouldOutputExceptionWhenIOExceptionHappens() throws Exception {
        // Should
        when(githubWrapperService.getAllUserReposWithoutForks(anyString())).thenThrow(new IOException());
        // When & Then
        mockMvc.perform(get("/api/github/users/repos-without-forks?username=testuser")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500));
    }
}
