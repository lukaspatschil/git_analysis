package com.tuwien.gitanalyser.entity;

import com.sun.istack.NotNull;
import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(uniqueConstraints = {@UniqueConstraint(name = "Provider_platformId", columnNames = {"authenticationProvider",
    "platformId"})})
public class User {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column
    @NotNull
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "authenticationProvider")
    private AuthenticationProvider authenticationProvider;

    @Column(name = "platformId")
    private Integer platformId;

    @Column(name = "accessToken")
    private String accessToken;

    @Column(name = "refreshToken")
    private String refreshToken;

}

