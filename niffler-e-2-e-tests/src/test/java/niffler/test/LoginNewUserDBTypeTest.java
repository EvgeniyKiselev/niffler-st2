package niffler.test;

import com.codeborne.selenide.Selenide;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureId;
import niffler.db.dao.NifflerUsersDAO;
import niffler.db.dao.NifflerUsersDAOHibernate;
import niffler.db.dao.NifflerUsersDAOJdbc;
import niffler.db.dao.NifflerUsersDAOSpringJdbc;
import niffler.db.entity.UserEntity;
import niffler.jupiter.annotation.GenerateRandomUserEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static niffler.model.DBType.*;

public class LoginNewUserDBTypeTest extends BaseWebTest {
    NifflerUsersDAO usersDAO;

    @AllureId("266")
    @GenerateRandomUserEntity(dbType = SPRING)
    @Test
    void loginTest(UserEntity user) {
        Allure.step("open page", () -> Selenide.open("http://127.0.0.1:3000/main"));
        $("a[href*='redirect']").click();
        $("input[name='username']").setValue(user.getUsername());
        $("input[name='password']").setValue(user.getPassword());
        $("button[type='submit']").click();

        $("a[href*='friends']").click();
        $(".header").should(visible).shouldHave(text(nifflerTitle));
    }

    @AllureId("268")
    @GenerateRandomUserEntity()
    @Test
    void checkUpdateUser(UserEntity user) {
        NifflerUsersDAO usersDAO;
        switch (user.getDbType()) {
            case SPRING ->  usersDAO = new NifflerUsersDAOSpringJdbc();
            case HIBERNATE -> usersDAO = new NifflerUsersDAOHibernate();
            case JDBC -> usersDAO = new NifflerUsersDAOJdbc();
            default -> throw new IllegalStateException("Unexpected value: " + user.getDbType());
        }
        UserEntity updUserEntity = new UserEntity();
        updUserEntity.setId(user.getId());
        updUserEntity.setUsername(user.getUsername() + "-updated");
        updUserEntity.setPassword("123456");
        updUserEntity.setEnabled(false);
        updUserEntity.setAccountNonExpired(false);
        updUserEntity.setAccountNonLocked(false);
        updUserEntity.setCredentialsNonExpired(false);

        usersDAO.updateUser(updUserEntity);

        Allure.step("open page", () -> Selenide.open("http://127.0.0.1:3000/main"));
        $("a[href*='redirect']").click();
        $("input[name='username']").setValue(updUserEntity.getUsername());
        $("input[name='password']").setValue(updUserEntity.getPassword());
        $("button[type='submit']").click();

        $(byText("User account is locked")).should(visible);
    }

    @AllureId("269")
    @GenerateRandomUserEntity(dbType = JDBC)
    @Test
    void checkReadUser(UserEntity user) {
        switch (user.getDbType()) {
            case SPRING ->  usersDAO = new NifflerUsersDAOSpringJdbc();
            case HIBERNATE -> usersDAO = new NifflerUsersDAOHibernate();
            case JDBC -> usersDAO = new NifflerUsersDAOJdbc();
            default -> throw new IllegalStateException("Unexpected value: " + user.getDbType());
        }
        Assertions.assertEquals(user.getId(), UUID.fromString(usersDAO.getUserId(user.getUsername())));
    }
}