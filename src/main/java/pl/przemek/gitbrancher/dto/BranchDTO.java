package pl.przemek.gitbrancher.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BranchDTO(String name,
                        @JsonProperty("commit") CommitDTO commitDTO) {
}
