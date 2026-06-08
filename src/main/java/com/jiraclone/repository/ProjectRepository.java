package com.jiraclone.repository;

import com.jiraclone.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByOrganizationId(UUID orgId);

    @Query(value = "SELECT * FROM projects", nativeQuery = true)
    List<Project> findAll();
}
