package pl.przemek.gitbrancher.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.przemek.gitbrancher.dto.GithubApiRepoDTO;
import pl.przemek.gitbrancher.dto.out.OutputBranchDTO;
import pl.przemek.gitbrancher.dto.out.OutputRepoDTO;
import pl.przemek.gitbrancher.exception.GithubApiException;
import pl.przemek.gitbrancher.service.github.api.GithubApiClient;
import pl.przemek.gitbrancher.service.validator.GithubValidator;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GithubWrapperService {
    private final GithubApiClient githubApiClient;

    public List<OutputRepoDTO> getAllUserReposWithoutForks(String username) throws GithubApiException {
        GithubValidator.validateInput(username);
        ArrayList<OutputRepoDTO> allUserOutputRepoDTOsWithoutForks = new ArrayList<>();

        List<GithubApiRepoDTO> githubApiRepoDTOList = githubApiClient.fetchAllUserReposInfoWithoutForks(username);
        for (GithubApiRepoDTO repoDTO : githubApiRepoDTOList) {
            List<OutputBranchDTO> outputBranchDTOList = githubApiClient.fetchAllRepoBranchesMappedToOutputBranchDTOs(repoDTO);
            OutputRepoDTO outputRepoDTO = new OutputRepoDTO(repoDTO.name(), repoDTO.ownerDTO().login(), outputBranchDTOList);
            allUserOutputRepoDTOsWithoutForks.add(outputRepoDTO);
        }

        return allUserOutputRepoDTOsWithoutForks;
    }
}
