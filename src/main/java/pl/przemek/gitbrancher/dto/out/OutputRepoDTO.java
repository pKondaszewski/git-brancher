package pl.przemek.gitbrancher.dto.out;

import java.util.List;

public record OutputRepoDTO(String repositoryName,
                            String ownerLogin,
                            List<OutputBranchDTO> branches) {
}
