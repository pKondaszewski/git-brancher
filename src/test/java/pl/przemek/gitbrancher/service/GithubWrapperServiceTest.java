package pl.przemek.gitbrancher.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.przemek.gitbrancher.dto.GithubApiRepoDTO;
import pl.przemek.gitbrancher.dto.OwnerDTO;
import pl.przemek.gitbrancher.dto.out.OutputBranchDTO;
import pl.przemek.gitbrancher.dto.out.OutputRepoDTO;
import pl.przemek.gitbrancher.exception.GithubApiException;
import pl.przemek.gitbrancher.service.github.api.GithubApiClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GithubWrapperServiceTest {
    @Mock
    private GithubApiClient githubApiClient;
    @InjectMocks
    private GithubWrapperService githubWrapperService;

    private static String username;
    private static GithubApiRepoDTO repoDTO1;
    private static List<GithubApiRepoDTO> githubApiRepoDTOList;
    private static OutputBranchDTO branchDTO1;
    private static List<OutputBranchDTO> outputBranchDTOList;

    @BeforeAll
    static void initVars() {
        username = "username";
        repoDTO1 = new GithubApiRepoDTO("repoName", new OwnerDTO(username), "branchesUrl", false);
        githubApiRepoDTOList = List.of(repoDTO1);

        branchDTO1 = new OutputBranchDTO("branchName1", "sha_abc_1");
        outputBranchDTOList = List.of(branchDTO1);
    }

    @Test
    void shouldReturnCorrectCollectionOfReposWhenFetchAllUserReposInfoWithoutForks() {
        // given
        when(githubApiClient.fetchAllUserReposInfoWithoutForks(username)).thenReturn(githubApiRepoDTOList);
        when(githubApiClient.fetchAllRepoBranchesMappedToOutputBranchDTOs(repoDTO1)).thenReturn(outputBranchDTOList);

        // when
        List<OutputRepoDTO> result = githubWrapperService.getAllUserReposWithoutForks(username);

        // then
        assertEquals(githubApiRepoDTOList.size(), result.size());
        assertEquals(repoDTO1.name(), result.getFirst().repositoryName());
        assertEquals(repoDTO1.ownerDTO().login(), result.getFirst().ownerLogin());
        OutputRepoDTO first = result.getFirst();
        assertEquals(branchDTO1, first.branches().getFirst());
    }

    @Test
    void shouldThrowExceptionWhenGithubApiThrowsException() {
        // given
        GithubApiException userNotFoundException = new GithubApiException(404, "Not found");
        when(githubApiClient.fetchAllUserReposInfoWithoutForks(username)).thenThrow(userNotFoundException);

        // when
        GithubApiException exception = assertThrows(GithubApiException.class, () ->
                githubWrapperService.getAllUserReposWithoutForks(username)
        );

        // then
        assertEquals(userNotFoundException, exception);
    }
}
