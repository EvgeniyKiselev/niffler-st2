package niffler.jupiter.extension;

import io.qameta.allure.AllureId;
import niffler.db.dao.NifflerUsersDAO;
import niffler.db.dao.NifflerUsersDAODB;
import niffler.db.entity.Authority;
import niffler.db.entity.AuthorityEntity;
import niffler.db.entity.UserEntity;
import niffler.jupiter.annotation.GenerateRandomUserEntity;
import org.junit.jupiter.api.extension.*;

import java.util.Arrays;

import static niffler.db.entity.RandomDataForUserEntity.randomPassword;
import static niffler.db.entity.RandomDataForUserEntity.randomUsername;
import static niffler.jupiter.extension.CreateUserViaDB.CREATE_USER_NAMESPACE;

public class GenerateRandomUserEntityExtension implements ParameterResolver, BeforeEachCallback, AfterEachCallback {
    public static ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
            .create(GenerateRandomUserEntityExtension.class);
    NifflerUsersDAO usersDAO = new NifflerUsersDAODB();

    @Override
    public void beforeEach(ExtensionContext context) {
        UserEntity ue = new UserEntity();
        GenerateRandomUserEntity annotation = context.getRequiredTestMethod()
                .getAnnotation(GenerateRandomUserEntity.class);

        if (annotation != null) {

            ue.setUsername(randomUsername);
            ue.setPassword(randomPassword);
            ue.setEnabled(true);
            ue.setAccountNonExpired(true);
            ue.setAccountNonLocked(true);
            ue.setCredentialsNonExpired(true);
            ue.setAuthorities(Arrays.stream(Authority.values()).map(
                    a -> {
                        AuthorityEntity ae = new AuthorityEntity();
                        ae.setAuthority(a);
                        return ae;
                    }
            ).toList());
            usersDAO.createUser(ue);
            context.getStore(NAMESPACE).put("userEntity", ue);
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(UserEntity.class);
    }

    @Override
    public UserEntity resolveParameter(ParameterContext parameterContext,
                                       ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE).get("userEntity", UserEntity.class);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        GenerateRandomUserEntity annotation = context.getRequiredTestMethod()
                .getAnnotation(GenerateRandomUserEntity.class);

        if (annotation != null) {
            context.getStore(CREATE_USER_NAMESPACE)
                    .remove(context.getRequiredTestMethod().getAnnotation(AllureId.class).value());
        }
    }
}