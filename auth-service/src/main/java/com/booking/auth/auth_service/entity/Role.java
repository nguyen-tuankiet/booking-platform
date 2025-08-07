package com.booking.auth.auth_service.entity;

import com.booking.auth.auth_service.utils.RoleName;
import com.booking.common_library.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Set;

@Entity
@Data
@Table(name = "roles")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Role extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private RoleName name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;
}
