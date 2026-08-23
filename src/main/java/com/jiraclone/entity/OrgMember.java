package com.jiraclone.entity;

import com.jiraclone.enums.OrgRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "org_member",
        uniqueConstraints = {
            @UniqueConstraint(
                name = "uk_org_member_user_org",
                columnNames = {"user_id", "org_id"}
            )
        }
)
public class OrgMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", referencedColumnName = "id", nullable = false)
    private Organization organization;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrgRole orgRole;
}
