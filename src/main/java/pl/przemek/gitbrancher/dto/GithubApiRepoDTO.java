package pl.przemek.gitbrancher.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubApiRepoDTO(String name,
                               @JsonProperty("owner") OwnerDTO ownerDTO,
                               @JsonProperty("branches_url") String branchesUrl,
                               boolean fork) {
}