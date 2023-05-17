package niffler.db.dao;

import niffler.db.DataSourceProvider;
import niffler.db.ServiceDB;
import niffler.db.entity.Authority;
import niffler.db.entity.AuthorityEntity;
import niffler.db.entity.UserEntity;
import niffler.db.jpa.EmfProvider;
import niffler.db.jpa.JpaTransactionManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class NifflerUsersDAODB extends JpaTransactionManager implements NifflerUsersDAO {

    private static final DataSource ds = DataSourceProvider.INSTANCE.getDataSource(ServiceDB.NIFFLER_AUTH);
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final TransactionTemplate transactionTemplate;
    private final JdbcTemplate jdbcTemplate;

    public NifflerUsersDAODB() {
        super(EmfProvider.INSTANCE.getEmf(ServiceDB.NIFFLER_AUTH).createEntityManager());
        DataSourceTransactionManager transactionManager = new JdbcTransactionManager(
                DataSourceProvider.INSTANCE.getDataSource(ServiceDB.NIFFLER_AUTH));
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.jdbcTemplate = new JdbcTemplate(Objects.requireNonNull(transactionManager.getDataSource()));
    }

    @Override
    public int createUser(UserEntity user) {
        int executeUpdate;
        switch (user.getDbType()) {
            case HIBERNATE -> {
                user.setPassword(pe.encode(user.getPassword()));
                persist(user);
                executeUpdate = 0;
            }
            case SPRING -> {
                transactionTemplate.execute(ts -> {
                    user.setId(UUID.randomUUID());
                    jdbcTemplate.update("INSERT INTO users " +
                                    " (id,username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired) " +
                                    " VALUES (?, ?, ?, ?, ?, ?, ?)",
                            user.getId(), user.getUsername(), pe.encode(user.getPassword()), user.getEnabled()
                            , user.getAccountNonExpired(), user.getAccountNonLocked(), user.getCredentialsNonExpired());
                    for (AuthorityEntity authority : user.getAuthorities()) {
                        jdbcTemplate.update("INSERT INTO authorities (user_id, authority) VALUES (?, ?)", user.getId(), authority.getAuthority().name());
                    }
                    return 1;
                });
                executeUpdate = 1;
            }
            default -> {
                try (Connection conn = ds.getConnection()) {

                    conn.setAutoCommit(false);

                    try (PreparedStatement st1 = conn.prepareStatement("INSERT INTO users "
                            + "(username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired) "
                            + " VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                        st1.setString(1, user.getUsername());
                        st1.setString(2, pe.encode(user.getPassword()));
                        st1.setBoolean(3, user.getEnabled());
                        st1.setBoolean(4, user.getAccountNonExpired());
                        st1.setBoolean(5, user.getAccountNonLocked());
                        st1.setBoolean(6, user.getCredentialsNonExpired());

                        executeUpdate = st1.executeUpdate();

                        final UUID createdUserId;

                        try (ResultSet keys = st1.getGeneratedKeys()) {
                            if (keys.next()) {
                                createdUserId = UUID.fromString(keys.getString(1));
                                user.setId(createdUserId);
                            } else {
                                throw new IllegalArgumentException("Unable to create user, no uuid");
                            }
                        }

                        String insertAuthoritiesSql = "INSERT INTO authorities (user_id, authority) VALUES ('%s', '%s')";

                        List<String> sqls = user.getAuthorities()
                                .stream()
                                .map(ae -> ae.getAuthority().name())
                                .map(a -> String.format(insertAuthoritiesSql, createdUserId, a))
                                .toList();

                        for (String sql : sqls) {
                            try (Statement st2 = conn.createStatement()) {
                                st2.executeUpdate(sql);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } catch (SQLException e) {
                        conn.rollback();
                        conn.setAutoCommit(true);
                        throw new RuntimeException(e);
                    }

                    conn.commit();
                    conn.setAutoCommit(true);

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return executeUpdate;
    }

    @Override
    public UUID getUserId(UserEntity user) {
        switch (user.getDbType()) {
            case HIBERNATE -> {
                return UUID.fromString(em.createQuery("select u from UserEntity u where username=:username", UserEntity.class)
                        .setParameter("username", user.getUsername())
                        .getSingleResult()
                        .getId()
                        .toString());
            }
            case SPRING -> {
                return jdbcTemplate.query("SELECT * FROM users WHERE username = ?",
                        rs -> {
                            return UUID.fromString(rs.getString(1));
                        },
                        user.getUsername()
                );
            }
            default -> {
                try (Connection conn = ds.getConnection();
                     PreparedStatement st = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
                    st.setString(1, user.getUsername());
                    ResultSet resultSet = st.executeQuery();
                    if (resultSet.next()) {
                        return UUID.fromString(resultSet.getString(1));
                    } else {
                        throw new IllegalArgumentException("Can`t find user by given username: " + user.getUsername());
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public UserEntity selectUser(String userName) {
        UserEntity user;
        try (Connection conn = ds.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            st.setString(1, userName);
            ResultSet resultSet = st.executeQuery();
            if (resultSet.next()) {
                user = new UserEntity();
            } else {
                throw new IllegalArgumentException("Can`t find user by given username: " + userName);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    @Override
    public UserEntity readUser(UUID uuid) {

        UserEntity userEntity = new UserEntity();

        try (Connection conn = ds.getConnection();
             PreparedStatement st1 = conn.prepareStatement("SELECT * FROM users WHERE id=(?)")) {
            st1.setObject(1, uuid);
            ResultSet rs = st1.executeQuery();
            if (rs.next()) {
                userEntity.setId(UUID.fromString(rs.getString(1)));
                userEntity.setUsername(rs.getString(2));
                userEntity.setPassword(rs.getString(3));
                userEntity.setEnabled(rs.getBoolean(4));
                userEntity.setAccountNonExpired(rs.getBoolean(5));
                userEntity.setAccountNonLocked(rs.getBoolean(6));
                userEntity.setCredentialsNonExpired(rs.getBoolean(7));
            } else {
                throw new IllegalArgumentException("Can`t find user by given uuid: " + uuid);
            }

            try (PreparedStatement st2 = conn.prepareStatement("SELECT * FROM authorities WHERE user_id=(?)")) {
                st2.setObject(1, uuid);
                ResultSet rs2 = st2.executeQuery();
                List<AuthorityEntity> listAuths = new ArrayList<>();

                while (rs2.next()) {
                    AuthorityEntity authorityEntity = new AuthorityEntity();
                    authorityEntity.setId(UUID.fromString(rs2.getString(1)));
                    authorityEntity.setAuthority(Authority.valueOf(rs2.getString(3)));
                    listAuths.add(authorityEntity);
                }

                userEntity.setAuthorities(listAuths);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return userEntity;
    }

    @Override
    public int updateUser(String userName, UserEntity user) {
        int executeUpdate;
        switch (user.getDbType()) {
            case HIBERNATE -> {
                merge(user);
                executeUpdate = 0;
            }
            case SPRING -> {
                transactionTemplate.execute(ts -> {
                    jdbcTemplate.update("update users set password=?,enabled=?" +
                                    ",account_non_expired=?, account_non_locked=?, credentials_non_expired=? " +
                                    " where username=?",
                            pe.encode(user.getPassword()), user.getEnabled()
                            , user.getAccountNonExpired(), user.getAccountNonLocked(), user.getCredentialsNonExpired(), user.getUsername());
                    jdbcTemplate.update("delete from authorities where user_id=?", user.getId());
                    for (AuthorityEntity authority : user.getAuthorities()) {
                        jdbcTemplate.update("INSERT INTO authorities (user_id, authority) VALUES (?, ?)", user.getId(), authority.getAuthority().name());
                    }
                    return 1;
                });
                executeUpdate = 1;
            }
            default -> {
                try (Connection conn = ds.getConnection();
                     PreparedStatement st = conn.prepareStatement("UPDATE public.users " +
                             "SET username=?, password=?, enabled=?, account_non_expired=?, " +
                             "account_non_locked=?, credentials_non_expired=? " +
                             "WHERE username=?")) {

                    st.setString(1, user.getUsername());
                    st.setString(2, pe.encode(user.getPassword()));
                    st.setBoolean(3, user.getEnabled());
                    st.setBoolean(4, user.getAccountNonExpired());
                    st.setBoolean(5, user.getAccountNonLocked());
                    st.setBoolean(6, user.getCredentialsNonExpired());
                    st.setString(7, userName);

                    executeUpdate = st.executeUpdate();

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return executeUpdate;
    }

    @Override
    public int deleteUser(UserEntity user) {
        int executeUpdate;

        switch (user.getDbType()) {
            case HIBERNATE -> {
                remove(user);
                executeUpdate = 0;
            }
            case SPRING -> {
                transactionTemplate.execute(st -> {
                    jdbcTemplate.update("DELETE FROM authorities WHERE user_id = ?", user.getId());
                    jdbcTemplate.update("DELETE FROM users WHERE id = ?", user.getId());
                    return 1;
                });
                executeUpdate = 1;
            }
            default -> {
                final UUID user_id = getUserId(user);

                try (Connection conn = ds.getConnection()) {
                    try (PreparedStatement authoritiesStmnt = conn.prepareStatement("DELETE FROM public.authorities WHERE user_id = ?");
                         PreparedStatement usersStmnt = conn.prepareStatement("DELETE FROM public.users WHERE username = ?")) {

                        conn.setAutoCommit(false);

                        authoritiesStmnt.setObject(1, user_id);
                        authoritiesStmnt.executeUpdate();

                        usersStmnt.setString(1, user.getUsername());
                        executeUpdate = usersStmnt.executeUpdate();

                        conn.commit();
                    } catch (SQLException e) {
                        try {
                            conn.rollback();
                        } catch (SQLException exception) {
                            throw new RuntimeException(exception);
                        }
                        throw new RuntimeException(e);
                    } finally {
                        if (conn != null) {
                            conn.setAutoCommit(true);
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return executeUpdate;
    }
}