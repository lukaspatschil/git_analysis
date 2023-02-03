package com.tuwien.gitanalyser.endpoints;

import com.tuwien.gitanalyser.endpoints.DTOs.BranchDTO;
import com.tuwien.gitanalyser.endpoints.DTOs.NotSavedRepositoryDTO;
import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;
import com.tuwien.gitanalyser.endpoints.DTOs.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.DTOs.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.entity.SavedRepository;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.entity.mapper.BranchMapper;
import com.tuwien.gitanalyser.entity.mapper.NotSavedRepositoryMapper;
import com.tuwien.gitanalyser.entity.mapper.RepositoryMapper;
import com.tuwien.gitanalyser.service.RepositoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import utils.Randoms;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RepositoryEndpointTest {

    private static final NotSavedRepositoryInternalDTO NOT_SAVED_REPOSITORY_INTERNAL_DTO_1 =
        new NotSavedRepositoryInternalDTO(Randoms.getLong(), Randoms.alpha(), Randoms.alpha());
    private static final NotSavedRepositoryInternalDTO NOT_SAVED_REPOSITORY_INTERNAL_DTO_2 =
        new NotSavedRepositoryInternalDTO(Randoms.getLong(), Randoms.alpha(), Randoms.alpha());
    private static final NotSavedRepositoryInternalDTO NOT_SAVED_REPOSITORY_INTERNAL_DTO_3 =
        new NotSavedRepositoryInternalDTO(Randoms.getLong(), Randoms.alpha(), Randoms.alpha());
    private static final NotSavedRepositoryDTO NOT_SAVED_REPOSITORY_DTO_1 =
        new NotSavedRepositoryDTO(NOT_SAVED_REPOSITORY_INTERNAL_DTO_1.getPlatformId(),
                                  NOT_SAVED_REPOSITORY_INTERNAL_DTO_1.getName(),
                                  NOT_SAVED_REPOSITORY_INTERNAL_DTO_1.getUrl());
    private static final NotSavedRepositoryDTO NOT_SAVED_REPOSITORY_DTO_2 =
        new NotSavedRepositoryDTO(NOT_SAVED_REPOSITORY_INTERNAL_DTO_2.getPlatformId(),
                                  NOT_SAVED_REPOSITORY_INTERNAL_DTO_2.getName(),
                                  NOT_SAVED_REPOSITORY_INTERNAL_DTO_2.getUrl());
    private static final NotSavedRepositoryDTO NOT_SAVED_REPOSITORY_DTO_3 =
        new NotSavedRepositoryDTO(NOT_SAVED_REPOSITORY_INTERNAL_DTO_3.getPlatformId(),
                                  NOT_SAVED_REPOSITORY_INTERNAL_DTO_3.getName(),
                                  NOT_SAVED_REPOSITORY_INTERNAL_DTO_3.getUrl());

    private static final SavedRepository SAVED_REPOSITORY_1 =
        new SavedRepository(Randoms.getLong(), Randoms.getLong(), Randoms.alpha(), Randoms.alpha(), new User());

    private static final RepositoryDTO REPOSITORY_DTO_1 =
        new RepositoryDTO(SAVED_REPOSITORY_1.getId(), SAVED_REPOSITORY_1.getName(), SAVED_REPOSITORY_1.getUrl());

    private static final BranchInternalDTO BRANCH_INTERNAL_DTO_1 =
        new BranchInternalDTO(Randoms.alpha());
    private static final BranchInternalDTO BRANCH_INTERNAL_DTO_2 =
        new BranchInternalDTO(Randoms.alpha());

    private static final BranchDTO BRANCH_DTO_1 =
        new BranchDTO(BRANCH_INTERNAL_DTO_1.getName());
    private static final BranchDTO BRANCH_DTO_2 =
        new BranchDTO(BRANCH_INTERNAL_DTO_2.getName());

    private RepositoryService repositoryService;
    private RepositoryMapper repositoryMapper;

    private NotSavedRepositoryMapper notSavedRepositoryMapper;

    private RepositoryEndpoint sut;
    private BranchMapper branchMapper;

    @BeforeEach
    void setUp() {
        repositoryService = mock(RepositoryService.class);
        repositoryMapper = mock(RepositoryMapper.class);
        notSavedRepositoryMapper = mock(NotSavedRepositoryMapper.class);
        branchMapper = mock(BranchMapper.class);
        sut = new RepositoryEndpoint(repositoryService, repositoryMapper, notSavedRepositoryMapper, branchMapper);
    }

    @Test
    void getAllRepositories_always_shouldCallService() {
        // Given
        Authentication authentication = mock(Authentication.class);

        long userId = Randoms.getLong();
        when(authentication.getName()).thenReturn(String.valueOf(userId));

        // When
        sut.getAllRepositories(authentication);

        // Then
        verify(repositoryService).getAllRepositories(userId);
    }

    @Test
    void getAllRepositories_serviceReturnsListOfRepositories_returnList() {
        // Given
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName()).thenReturn(String.valueOf(userId));
        when(repositoryService.getAllRepositories(userId)).thenReturn(List.of(
            NOT_SAVED_REPOSITORY_INTERNAL_DTO_1,
            NOT_SAVED_REPOSITORY_INTERNAL_DTO_2,
            NOT_SAVED_REPOSITORY_INTERNAL_DTO_3));

        when(notSavedRepositoryMapper.dtosToDTOs(List.of(
            NOT_SAVED_REPOSITORY_INTERNAL_DTO_1,
            NOT_SAVED_REPOSITORY_INTERNAL_DTO_2,
            NOT_SAVED_REPOSITORY_INTERNAL_DTO_3)))
            .thenReturn(List.of(
                NOT_SAVED_REPOSITORY_DTO_1,
                NOT_SAVED_REPOSITORY_DTO_2,
                NOT_SAVED_REPOSITORY_DTO_3));

        // When
        List<NotSavedRepositoryDTO> repositoryList = sut.getAllRepositories(authentication);

        // Then
        assertThat(repositoryList, containsInAnyOrder(NOT_SAVED_REPOSITORY_DTO_1,
                                                      NOT_SAVED_REPOSITORY_DTO_2,
                                                      NOT_SAVED_REPOSITORY_DTO_3));
    }

    @Test
    void getAllRepositories_serviceReturnsEmptyListOfRepositories_returnList() {
        // Given
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName()).thenReturn(String.valueOf(userId));
        when(repositoryService.getAllRepositories(userId)).thenReturn(List.of());

        // When
        List<NotSavedRepositoryDTO> repositoryList = sut.getAllRepositories(authentication);

        // Then
        assertThat(repositoryList, equalTo(List.of()));
    }

    @Test
    void getRepositoryById_always_shouldCallService() {
        // Given
        long repositoryId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName()).thenReturn(String.valueOf(Randoms.getLong()));

        // When
        sut.getRepositoryById(authentication, repositoryId);

        // Then
        verify(repositoryService).getRepositoryById(Long.parseLong(authentication.getName()), repositoryId);
    }

    @Test
    void getRepositoryById_givenOneRepository_returnsRepository() {
        // Given
        long repositoryId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName()).thenReturn(String.valueOf(userId));
        when(repositoryService.getRepositoryById(userId, repositoryId)).thenReturn(SAVED_REPOSITORY_1);
        when(repositoryMapper.entityToDTO(SAVED_REPOSITORY_1)).thenReturn(REPOSITORY_DTO_1);

        // When
        RepositoryDTO repository = sut.getRepositoryById(authentication, repositoryId);

        // Then
        assertThat(repository, equalTo(REPOSITORY_DTO_1));
    }

    @Test
    void getBranchesByRepositoryId_always_shouldCallService() {
        // Given
        long repositoryId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName()).thenReturn(String.valueOf(userId));

        // When
        sut.getBranchesByRepositoryId(authentication, repositoryId);

        // Then
        verify(repositoryService).getAllBranches(userId, repositoryId);
    }

    @Test
    void getBranchesByRepositoryId_givenOneBranch_returnsListWithOneItem() {
        // Given
        long repositoryId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName()).thenReturn(String.valueOf(userId));
        when(repositoryService.getAllBranches(userId, repositoryId)).thenReturn(List.of(BRANCH_INTERNAL_DTO_1));
        when(branchMapper.dtosToDTOs(List.of(BRANCH_INTERNAL_DTO_1))).thenReturn(List.of(BRANCH_DTO_1));

        // When
        List<BranchDTO> branches = sut.getBranchesByRepositoryId(authentication, repositoryId);

        // Then
        assertThat(branches, containsInAnyOrder(BRANCH_DTO_1));
    }

    @Test
    void getBranchesByRepositoryId_givenTwoBranches_returnsListWithTwoItems() {
        // Given
        long repositoryId = Randoms.getLong();
        long userId = Randoms.getLong();

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName()).thenReturn(String.valueOf(userId));
        when(repositoryService.getAllBranches(userId, repositoryId)).thenReturn(List.of(
            BRANCH_INTERNAL_DTO_1, BRANCH_INTERNAL_DTO_2));
        when(branchMapper.dtosToDTOs(List.of(
            BRANCH_INTERNAL_DTO_1,
            BRANCH_INTERNAL_DTO_2)
        )).thenReturn(List.of(
            BRANCH_DTO_1,
            BRANCH_DTO_2));

        // When
        List<BranchDTO> branches = sut.getBranchesByRepositoryId(authentication, repositoryId);

        // Then
        assertThat(branches, containsInAnyOrder(BRANCH_DTO_1, BRANCH_DTO_2));
    }
}
