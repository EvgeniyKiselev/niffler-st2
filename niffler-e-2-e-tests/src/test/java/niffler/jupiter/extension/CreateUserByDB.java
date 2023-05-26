package niffler.jupiter.extension;

import io.qameta.allure.AllureId;
import niffler.db.dao.NifflerUsersDAO;
import niffler.db.dao.NifflerUsersDAOHibernate;
import niffler.db.entity.Authority;
import niffler.db.entity.AuthorityEntity;
import niffler.db.entity.UserEntity;
import niffler.jupiter.annotation.CreateUser;
import org.junit.jupiter.api.extension.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class CreateUserByDB implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace CREATE_USER_NAMESPACE = ExtensionContext.Namespace.create(CreateUserByDB.class);

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(UserEntity.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        final String testId = getTestId(extensionContext);
        return extensionContext.getStore(CREATE_USER_NAMESPACE).get(testId);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {

        final String testId = getTestId(context);

        CreateUser createUserAnno = context.getRequiredTestMethod().getAnnotation(CreateUser.class);

        UserEntity user;

        if(createUserAnno != null) {
            NifflerUsersDAO usersDAO = new NifflerUsersDAOHibernate();
            user = new UserEntity();
            user.setUsername(createUserAnno.username());
            user.setPassword(createUserAnno.password());
            user.setEnabled(createUserAnno.enabled());
            user.setAccountNonExpired(createUserAnno.accountNonExpired());
            user.setAccountNonLocked(createUserAnno.accountNonLocked());
            user.setCredentialsNonExpired(createUserAnno.credentialsNonExpired());
           // user.setDeleteAfterTest(createUserAnno.deleteAfterTest());
            user.setAuthorities(Arrays.stream(Authority.values()).map(
                    a -> {
                        AuthorityEntity ae = new AuthorityEntity();
                        ae.setAuthority(a);
                        return ae;
                    }
            ).toList());
            usersDAO.createUser(user);

            context.getStore(CREATE_USER_NAMESPACE).put(testId, user);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        final String testId = getTestId(context);
        final String testID = context.getRequiredTestClass() + String.valueOf(context.getTestMethod());
        int executeUpdate = 0;

        if (context.getRequiredTestMethod().getAnnotation(CreateUser.class).deleteAfterTest()) {
            NifflerUsersDAO usersDAO = new NifflerUsersDAOHibernate();
            executeUpdate = usersDAO.removeUser(new UserEntity(UUID.fromString(
                    usersDAO.getUserId(context.getTestMethod().get().getAnnotation(CreateUser.class).username()))));
            if (executeUpdate > 0) {
                context.getStore(CREATE_USER_NAMESPACE).remove(testId);
            }
        }
    }

    private String getTestId(ExtensionContext context) {
        return Objects
                .requireNonNull(context.getRequiredTestMethod().getAnnotation(AllureId.class))
                .value();
    }
}