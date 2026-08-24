package com.jiraclone.repository;

import com.jiraclone.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    @Query(value = "SELECT EXISTS(SELECT 1 FROM organizations WHERE org_name = :orgName)", nativeQuery = true)
    Boolean checkExistByOrgName(String orgName);
}
