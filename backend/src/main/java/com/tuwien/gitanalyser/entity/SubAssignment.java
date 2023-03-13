package com.tuwien.gitanalyser.entity;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sub_assignment", uniqueConstraints = {
    @UniqueConstraint(name = "assignmentId_assignedName", columnNames = {"assignment_id", "assigned_name"})
})
public class SubAssignment {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assigned_name")
    @NotNull
    private String assignedName;

    @ManyToOne
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;
}


