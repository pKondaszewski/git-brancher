package pl.przemek.gitbrancher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BranchDTO(String name,
                        @JsonProperty("commit") CommitDTO commitDTO) {
}
