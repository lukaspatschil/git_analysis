package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.endpoints.DTOs.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.entity.SavedRepository;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.repository.RepositoryRepository;
import com.tuwien.gitanalyser.service.APICalls.GitHubAPI;
import com.tuwien.gitanalyser.service.APICalls.GitLabAPI;
import com.tuwien.gitanalyser.service.JGit;
import com.tuwien.gitanalyser.service.UserService;
import org.gitlab4j.api.GitLabApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Randoms;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RepositoryServiceImplTest {

    private RepositoryServiceImpl sut;
    private UserService userService;
    private GitHubAPI gitHubAPI;
    private GitLabAPI gitLabAPI;
    private String exceptionString;
    private RepositoryRepository repositoryRepository;
    private JGit jGit;

    @BeforeEach
    void setUp() {
        gitHubAPI = mock(GitHubAPI.class);
        gitLabAPI = mock(GitLabAPI.class);
        userService = mock(UserService.class);
        repositoryRepository = mock(RepositoryRepository.class);
        jGit = mock(JGit.class);
        sut = new RepositoryServiceImpl(userService, gitHubAPI, gitLabAPI, repositoryRepository, jGit);
        exceptionString = "testException";
    }

    @Test
    void getAllRepositories_GitLabAuthorization_shouldCallGitLabAPI()
        throws GitLabApiException, NotFoundException, IOException {
        // Given
        Long userId = Randoms.getLong();
        User user = mock(User.class);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITLAB);

        // When
        sut.getAllRepositories(userId);

        // Then
        verify(gitLabAPI).getAllRepositories(accessToken);
    }

    @Test
    void getAllRepositories_GitHubAuthorization_shouldCallGitHubAPI() throws IOException, NotFoundException {
        // Given
        Long userId = Randoms.getLong();
        User user = mock(User.class);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITHUB);

        // When
        sut.getAllRepositories(userId);

        // Then
        verify(gitHubAPI).getAllRepositories(accessToken);
    }

    @Test
    void getAllRepositories_GitLabAuthorizationThrowsException_shouldThrowRuntimeException()
        throws GitLabApiException, NotFoundException, IOException {
        // Given
        Long userId = Randoms.getLong();
        User user = mock(User.class);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITLAB);

        when(gitLabAPI.getAllRepositories(accessToken)).thenThrow(
            new GitLabApiException(exceptionString));

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getAllRepositories(userId), exceptionString);
    }

    @Test
    void getAllRepositories_GitHubAuthorizationThrowsException_shouldThrowException()
        throws IOException, NotFoundException {
        // Given
        Long userId = Randoms.getLong();
        User user = mock(User.class);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITHUB);

        when(gitHubAPI.getAllRepositories(accessToken)).thenThrow(
            new IOException(exceptionString));

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getAllRepositories(userId), exceptionString);
    }

    @Test
    void getAllRepositories_randomAuthorization_shouldThrowException() {
        // Given

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getAllRepositories(Randoms.getLong()));
    }

    @Test
    void getRepositoryById_GitLabAuthorization_shouldCallGitLabAPI()
        throws GitLabApiException, NotFoundException, IOException {
        // Given
        long repositoryId = Randoms.getLong();

        Long userId = Randoms.getLong();
        User user = mock(User.class);

        NotSavedRepositoryInternalDTO notSavedRepositoryInternalDTO =
            getRandomNotSavedRepositoryInternalDTO(repositoryId);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITLAB);
        when(gitLabAPI.getRepositoryById(accessToken, repositoryId)).thenReturn(notSavedRepositoryInternalDTO);
        when(repositoryRepository.save(any(SavedRepository.class))).thenReturn(mock(SavedRepository.class));

        // When
        sut.getRepositoryById(userId, repositoryId);

        // Then
        verify(gitLabAPI).getRepositoryById(accessToken, repositoryId);
    }

    @Test
    void getRepositoryById_GitHubAuthorization_shouldCallGitHubAPI() throws IOException, NotFoundException {
        // Given
        long repositoryId = Randoms.getLong();

        Long userId = Randoms.getLong();
        User user = mock(User.class);

        NotSavedRepositoryInternalDTO notSavedRepositoryInternalDTO =
            getRandomNotSavedRepositoryInternalDTO(repositoryId);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITHUB);
        when(gitHubAPI.getRepositoryById(accessToken, repositoryId)).thenReturn(notSavedRepositoryInternalDTO);
        when(repositoryRepository.save(any(SavedRepository.class))).thenReturn(mock(SavedRepository.class));

        // When
        sut.getRepositoryById(userId, repositoryId);

        // Then
        verify(gitHubAPI).getRepositoryById(accessToken, repositoryId);
    }

    @Test
    void getRepositoryById_randomAuthorization_shouldThrowException() throws NotFoundException {
        // Given
        long repositoryId = Randoms.getLong();

        Long userId = Randoms.getLong();
        User user = mock(User.class);

        String accessToken = Randoms.alpha();
        when(userService.getUser(userId)).thenReturn(user);
        when(user.getAccessToken()).thenReturn(accessToken);

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getRepositoryById(userId, repositoryId));
    }

    @Test
    void getRepositoryByID_GitHubAuthorizationAndRepositoryNotSavedYet_shouldSaveRepository() throws IOException {
        // Given
        long repositoryId = Randoms.getLong();

        Long userId = Randoms.getLong();
        User user = mock(User.class);

        NotSavedRepositoryInternalDTO notSavedRepositoryInternalDTO =
            new NotSavedRepositoryInternalDTO(repositoryId, Randoms.alpha(), Randoms.alpha());
        SavedRepository savedRepository = mock(SavedRepository.class);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITHUB);

        when(gitHubAPI.getRepositoryById(accessToken, repositoryId)).thenReturn(notSavedRepositoryInternalDTO);
        when(repositoryRepository.save(any(SavedRepository.class))).thenReturn(savedRepository);

        // When
        sut.getRepositoryById(userId, repositoryId);

        // Then
        verify(repositoryRepository).save(any(SavedRepository.class));
    }

    @Test
    void getRepositoryByID_GitLabAuthorizationAndRepositoryNotSavedYet_shouldSaveRepository()
        throws IOException, GitLabApiException {
        // Given
        long repositoryId = Randoms.getLong();

        Long userId = Randoms.getLong();
        User user = mock(User.class);

        NotSavedRepositoryInternalDTO notSavedRepositoryInternalDTO =
            new NotSavedRepositoryInternalDTO(repositoryId, Randoms.alpha(), Randoms.alpha());
        SavedRepository savedRepository = mock(SavedRepository.class);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITLAB);

        when(gitLabAPI.getRepositoryById(accessToken, repositoryId)).thenReturn(notSavedRepositoryInternalDTO);
        when(repositoryRepository.save(any(SavedRepository.class))).thenReturn(savedRepository);

        // When
        sut.getRepositoryById(userId, repositoryId);

        // Then
        verify(repositoryRepository).save(any(SavedRepository.class));
    }

    @Test
    void getRepositoryById_GitLabAuthorizationAndRepositoryAlreadySaved_shouldNotSaveRepository()
        throws GitLabApiException, NotFoundException, IOException {
        // Given
        long repositoryId = Randoms.getLong();

        Long userId = Randoms.getLong();
        User user = mock(User.class);

        NotSavedRepositoryInternalDTO notSavedRepositoryInternalDTO =
            getRandomNotSavedRepositoryInternalDTO(repositoryId);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITLAB);
        when(gitLabAPI.getRepositoryById(accessToken, repositoryId)).thenReturn(notSavedRepositoryInternalDTO);
        when(repositoryRepository.findByUserIdAndPlatformId(user.getId(),
                                                            repositoryId)).thenReturn(mock(SavedRepository.class));

        // When
        sut.getRepositoryById(userId, repositoryId);

        // Then
        verify(repositoryRepository, never()).save(any(SavedRepository.class));
    }

    @Test
    void getRepositoryById_GitHubAuthorizationAndRepositoryAlreadySaved_shouldNotSaveRepository()
        throws NotFoundException, IOException {
        // Given
        long repositoryId = Randoms.getLong();

        Long userId = Randoms.getLong();
        User user = mock(User.class);

        NotSavedRepositoryInternalDTO notSavedRepositoryInternalDTO =
            getRandomNotSavedRepositoryInternalDTO(repositoryId);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITHUB);
        when(gitHubAPI.getRepositoryById(accessToken, repositoryId)).thenReturn(notSavedRepositoryInternalDTO);
        when(repositoryRepository.findByUserIdAndPlatformId(user.getId(),
                                                            repositoryId)).thenReturn(mock(SavedRepository.class));

        // When
        sut.getRepositoryById(userId, repositoryId);

        // Then
        verify(repositoryRepository, never()).save(any(SavedRepository.class));
    }

    @Test
    void getRepositoryById_GitLabAuthorizationAndRepositoryNotSaved_shouldCloneRepository()
        throws GitLabApiException, IOException {
        // Given
        long repositoryId = Randoms.getLong();

        Long userId = Randoms.getLong();
        User user = mock(User.class);

        NotSavedRepositoryInternalDTO notSavedRepositoryInternalDTO =
            getRandomNotSavedRepositoryInternalDTO(repositoryId);

        SavedRepository savedRepository = mock(SavedRepository.class);
        long databaseId = Randoms.getLong();
        when(savedRepository.getId()).thenReturn(databaseId);
        when(savedRepository.getUrl()).thenReturn(notSavedRepositoryInternalDTO.getUrl());
        when(savedRepository.getName()).thenReturn(notSavedRepositoryInternalDTO.getName());
        when(savedRepository.getPlatformId()).thenReturn(notSavedRepositoryInternalDTO.getPlatformId());

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITLAB);
        when(gitLabAPI.getRepositoryById(accessToken, repositoryId)).thenReturn(notSavedRepositoryInternalDTO);
        when(repositoryRepository.findByUserIdAndPlatformId(user.getId(), repositoryId)).thenReturn(null);
        when(repositoryRepository.save(any(SavedRepository.class))).thenReturn(savedRepository);

        // When
        sut.getRepositoryById(userId, repositoryId);

        // Then
        verify(jGit).cloneRepository(notSavedRepositoryInternalDTO.getUrl(), databaseId, accessToken);
    }

    @Test
    void getRepositoryById_GitHubAuthorizationAndRepositoryNotSaved_shouldCloneRepository() throws IOException {
        // Given
        long repositoryId = Randoms.getLong();
        long databaseId = Randoms.getLong();

        Long userId = Randoms.getLong();
        User user = mock(User.class);

        NotSavedRepositoryInternalDTO notSavedRepositoryInternalDTO =
            getRandomNotSavedRepositoryInternalDTO(repositoryId);

        SavedRepository savedRepository = mock(SavedRepository.class);
        when(savedRepository.getId()).thenReturn(databaseId);
        when(savedRepository.getUrl()).thenReturn(notSavedRepositoryInternalDTO.getUrl());
        when(savedRepository.getName()).thenReturn(notSavedRepositoryInternalDTO.getName());
        when(savedRepository.getPlatformId()).thenReturn(notSavedRepositoryInternalDTO.getPlatformId());

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITHUB);
        when(gitHubAPI.getRepositoryById(accessToken, repositoryId)).thenReturn(notSavedRepositoryInternalDTO);
        when(repositoryRepository.findByUserIdAndPlatformId(user.getId(), repositoryId)).thenReturn(null);
        when(repositoryRepository.save(any(SavedRepository.class))).thenReturn(savedRepository);

        // When
        sut.getRepositoryById(userId, repositoryId);

        // Then
        verify(jGit).cloneRepository(notSavedRepositoryInternalDTO.getUrl(), databaseId, accessToken);
    }

    @Test
    void getRepositoryById_GitHubAuthorization_shouldReturnSavedRepository() throws IOException, NotFoundException {
        // Given
        long repositoryId = Randoms.getLong();

        Long userId = Randoms.getLong();
        User user = mock(User.class);

        NotSavedRepositoryInternalDTO notSavedRepositoryInternalDTO =
            getRandomNotSavedRepositoryInternalDTO(repositoryId);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITHUB);
        when(gitHubAPI.getRepositoryById(accessToken, repositoryId)).thenReturn(notSavedRepositoryInternalDTO);
        SavedRepository savedRepository = mock(SavedRepository.class);
        when(repositoryRepository.save(any(SavedRepository.class))).thenReturn(savedRepository);

        // When
        SavedRepository result = sut.getRepositoryById(userId, repositoryId);

        // Then
        assertThat(result, equalTo(savedRepository));
    }

    @Test
    void getAllBranches_GitLabAuthorization_shouldCallGitLabAPI()
        throws GitLabApiException, NotFoundException, IOException {
        // Given
        Long userId = Randoms.getLong();
        Long repositoryId = Randoms.getLong();

        User user = mock(User.class);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITLAB);

        // When
        sut.getAllBranches(userId, repositoryId);

        // Then
        verify(gitLabAPI).getAllBranches(accessToken, repositoryId);
    }

    @Test
    void getAllBranches_GitHubAuthorization_shouldCallGitHubAPI() throws IOException, NotFoundException {
        // Given
        Long userId = Randoms.getLong();
        Long repositoryId = Randoms.getLong();
        User user = mock(User.class);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITHUB);

        // When
        sut.getAllBranches(userId, repositoryId);

        // Then
        verify(gitHubAPI).getAllBranches(accessToken, repositoryId);
    }

    @Test
    void getAllBranches_GitLabAuthorizationThrowsException_shouldThrowRuntimeException()
        throws GitLabApiException, NotFoundException, IOException {
        // Given
        Long userId = Randoms.getLong();
        Long repositoryId = Randoms.getLong();
        User user = mock(User.class);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITLAB);

        when(gitLabAPI.getAllBranches(accessToken, repositoryId)).thenThrow(new GitLabApiException(exceptionString));

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getAllBranches(userId, repositoryId), exceptionString);
    }

    @Test
    void getAllBranches_GitHubAuthorizationThrowsException_shouldThrowException()
        throws IOException, NotFoundException {
        // Given
        Long userId = Randoms.getLong();
        Long repositoryId = Randoms.getLong();
        User user = mock(User.class);

        String accessToken = prepareUserService(userId, user, AuthenticationProvider.GITHUB);

        when(gitHubAPI.getAllBranches(accessToken, repositoryId)).thenThrow(new IOException(exceptionString));

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getAllBranches(userId, repositoryId), exceptionString);
    }

    @Test
    void getAllBranches_randomAuthorization_shouldThrowException() {
        // Given

        // When + Then
        assertThrows(RuntimeException.class, () -> sut.getAllBranches(Randoms.getLong(), Randoms.getLong()));
    }

    private String prepareUserService(Long userId, User user, AuthenticationProvider authenticationProvider)
        throws NotFoundException {
        String accessToken = Randoms.alpha();
        when(userService.getUser(userId)).thenReturn(user);
        when(user.getAccessToken()).thenReturn(accessToken);
        when(user.getAuthenticationProvider()).thenReturn(authenticationProvider);
        return accessToken;
    }

    private NotSavedRepositoryInternalDTO getRandomNotSavedRepositoryInternalDTO(long repositoryId) {
        return NotSavedRepositoryInternalDTO.builder()
                                            .platformId(repositoryId)
                                            .name(Randoms.alpha())
                                            .url(Randoms.alpha())
                                            .build();
    }
}
