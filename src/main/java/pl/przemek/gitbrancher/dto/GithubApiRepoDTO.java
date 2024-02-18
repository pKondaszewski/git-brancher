package pl.przemek.gitbrancher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubApiRepoDTO(String name,
                               @JsonProperty("owner") OwnerDTO ownerDTO,
                               @JsonProperty("branches_url") String branchesUrl,
                               boolean fork) {
}