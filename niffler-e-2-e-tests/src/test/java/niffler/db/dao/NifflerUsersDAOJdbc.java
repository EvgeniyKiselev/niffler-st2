package niffler.db.dao;

import niffler.db.DataSourceProvider;
import niffler.db.ServiceDB;
import niffler.db.entity.Authority;
import niffler.db.entity.AuthorityEntity;
import niffler.db.entity.UserEntity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NifflerUsersDAOJdbc implements NifflerUsersDAO {

    private static final DataSource ds = DataSourceProvider.INSTANCE.getDataSource(ServiceDB.NIFFLER_AUTH);
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Override
    public int createUser(UserEntity user) {
        int executeUpdate;

        final String CREATE_USER_QUERY = "INSERT INTO users "
                + "(username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired) "
                + " VALUES (?, ?, ?, ?, ?, ?)";
        final String INSERT_AUTHORITY_QUERY = "INSERT INTO authorities (user_id, authority) VALUES ('%s', '%s')";

        try (Connection conn = ds.getConnection()) {
            try (PreparedStatement userStmnt = conn.prepareStatement(CREATE_USER_QUERY);
                 PreparedStatement getUserIdStmnt = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
                 Statement authorityStmnt = conn.createStatement()) {

                conn.setAutoCommit(false);

                userStmnt.setString(1, user.getUsername());
                userStmnt.setString(2, pe.encode(user.getPassword()));
                userStmnt.setBoolean(3, user.getEnabled());
                userStmnt.setBoolean(4, user.getAccountNonExpired());
                userStmnt.setBoolean(5, user.getAccountNonLocked());
                userStmnt.setBoolean(6, user.getCredentialsNonExpired());
                executeUpdate = userStmnt.executeUpdate();

                String userId = null;

                getUserIdStmnt.setString(1, user.getUsername());
                ResultSet resultSet = getUserIdStmnt.executeQuery();
                if (resultSet.next()) {
                    userId = resultSet.getString(1);
                }

                final String USER_ID = userId;

                List<String> sqls = user.getAuthorities()
                        .stream()
                        .map(ae -> ae.getAuthority().name())
                        .map(a -> String.format(INSERT_AUTHORITY_QUERY, USER_ID, a))
                        .toList();

                for (String sql : sqls) {
                    authorityStmnt.executeUpdate(sql);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            } finally {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return executeUpdate;
    }

    @Override
    public String getUserId(String userName) {
        try (Connection conn = ds.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            st.setString(1, userName);
            ResultSet resultSet = st.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(1);
            } else {
                throw new IllegalArgumentException("Can`t find user by given username: " + userName);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserEntity selectUser(UUID uuid) {
        UserEntity userEntity = new UserEntity();

        try (Connection conn = ds.getConnection();
             PreparedStatement st1 = conn.prepareStatement("SELECT * FROM users WHERE id=(?)");
             PreparedStatement st2 = conn.prepareStatement("SELECT * FROM authorities WHERE user_id=(?)")
        ) {
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

            st2.setObject(1, uuid);
            ResultSet rs2 = st2.executeQuery();
            List<AuthorityEntity> listAuths = new ArrayList<>();

            while (rs2.next()) {
                AuthorityEntity authorityEntity = new AuthorityEntity();
                authorityEntity.setId(UUID.fromString(rs2.getString(1)));
                authorityEntity.setAuthority(Authority.valueOf(rs2.getString(3)));
                listAuths.add(authorityEntity);

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
        return executeUpdate;
    }

    @Override
    public int deleteUser(String userName) {

        int executeUpdate;

        final String user_id = getUserId(userName);

        try (Connection conn = ds.getConnection()) {
            try (PreparedStatement authoritiesStmnt = conn.prepareStatement("DELETE FROM public.authorities WHERE user_id = ?");
                 PreparedStatement usersStmnt = conn.prepareStatement("DELETE FROM public.users WHERE username = ?")) {

                conn.setAutoCommit(false);

                authoritiesStmnt.setObject(1, UUID.fromString(user_id));
                authoritiesStmnt.executeUpdate();

                usersStmnt.setString(1, userName);
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
        return executeUpdate;
    }
}