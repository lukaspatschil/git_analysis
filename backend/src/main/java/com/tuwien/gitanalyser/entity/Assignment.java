package com.tuwien.gitanalyser.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "assignment", uniqueConstraints = {
    @UniqueConstraint(name = "repositoryId_key", columnNames = {"repository_id", "key"})
})
public class Assignment {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "repository_id")
    private Repository repository;

    @Column(name = "key")
    private String key;

    @OneToMany(mappedBy = "assignment", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<SubAssignment> subAssignments;
}
