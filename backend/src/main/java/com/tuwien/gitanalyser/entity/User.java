package com.tuwien.gitanalyser.entity;

import com.sun.istack.NotNull;
import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString(exclude = "repositories")
@Table(uniqueConstraints = {@UniqueConstraint(name = "Provider_platformId", columnNames = {"authentication_provider",
    "platform_id"})})
public class User {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    @NotNull
    private String username;
    @Column
    private String email;
    @Column
    private String pictureUrl;
    @Enumerated(EnumType.STRING)
    @Column(name = "authentication_provider")
    private AuthenticationProvider authenticationProvider;
    @Column(name = "platform_id")
    private Integer platformId;
    @Column(name = "access_token")
    private String accessToken;
    @Column(name = "refresh_token")
    private String refreshToken;
    @Column(name = "finger_print_hash")
    private String fingerPrintHash;
    @OneToMany(mappedBy = "user")
    private List<Repository> repositories = new ArrayList<>();

    public User(final Long id, final String username, final String email, final String pictureUrl,
                final AuthenticationProvider authenticationProvider,
                final Integer platformId, final String accessToken, final String refreshToken) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.pictureUrl = pictureUrl;
        this.authenticationProvider = authenticationProvider;
        this.platformId = platformId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}

