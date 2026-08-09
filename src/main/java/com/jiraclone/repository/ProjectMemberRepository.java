package com.jiraclone.repository;

import com.jiraclone.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    @Query(value = "SELECT * from project_member WHERE project_id = :projectId",nativeQuery = true)
    List<ProjectMember> findAllMemberByProjectId(UUID projectId);
}
