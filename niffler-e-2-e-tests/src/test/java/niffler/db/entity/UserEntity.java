package niffler.db.entity;

import jakarta.persistence.*;
import lombok.Data;
import niffler.model.DBType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.FetchType.EAGER;

@Entity
@Table(name = "users")
@Data
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, columnDefinition = "UUID default gen_random_uuid()")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "account_non_expired", nullable = false)
    private Boolean accountNonExpired;

    @Column(name = "account_non_locked", nullable = false)
    private Boolean accountNonLocked;

    @Column(name = "credentials_non_expired", nullable = false)
    private Boolean credentialsNonExpired;

    private Boolean deleteAfterTest;

    DBType dbType;

    @OneToMany(fetch = EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user")
    private List<AuthorityEntity> authorities = new ArrayList<>();

    public UserEntity() {}

    public UserEntity(UUID id) {
        this.id = id;
    }

}