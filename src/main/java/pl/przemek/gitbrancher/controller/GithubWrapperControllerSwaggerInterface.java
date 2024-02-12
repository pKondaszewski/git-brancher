package pl.przemek.gitbrancher.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import pl.przemek.gitbrancher.dto.OutputRepoDTO;
import pl.przemek.gitbrancher.exception.GithubApiException;

import java.io.IOException;
import java.util.List;

public interface GithubWrapperControllerSwaggerInterface {
    @Operation(summary = "Get all user repositories without forks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Repositories found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")})
    ResponseEntity<List<OutputRepoDTO>> getAllUserReposWithoutForks (
            @Parameter(description = "Username of the Github user") @RequestParam String username) throws IOException,
            InterruptedException, GithubApiException;
}
