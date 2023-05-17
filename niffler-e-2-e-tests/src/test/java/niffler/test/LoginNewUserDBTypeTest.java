package niffler.test;

import com.codeborne.selenide.Selenide;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureId;
import niffler.db.dao.NifflerUsersDAO;
import niffler.db.dao.NifflerUsersDAODB;
import niffler.db.entity.UserEntity;
import niffler.jupiter.annotation.CreateUser;
import niffler.model.DBType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

public class LoginNewUserDBTypeTest extends BaseWebTest {
    private final NifflerUsersDAO usersDAO = new NifflerUsersDAODB();

    @AllureId("266")
    @CreateUser(username = "testuser00", password = "12345", enabled = true, dbType = DBType.JDBC)
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

    @AllureId("267")
    @CreateUser(username = "TestUser0", password = "12345", enabled = true)
    @Test
    void checkUpdateUser(UserEntity user) {
        UserEntity updUserEntity = new UserEntity();
        updUserEntity.setId(usersDAO.getUserId(user));
        updUserEntity.setUsername(user.getUsername() + "-updated");
        updUserEntity.setPassword("123456");
        updUserEntity.setEnabled(false);
        updUserEntity.setAccountNonExpired(false);
        updUserEntity.setAccountNonLocked(false);
        updUserEntity.setCredentialsNonExpired(false);

        usersDAO.updateUser(user.getUsername() + "-updated", updUserEntity);

        Allure.step("open page", () -> Selenide.open("http://127.0.0.1:3000/main"));
        $("a[href*='redirect']").click();
        $("input[name='username']").setValue(updUserEntity.getUsername());
        $("input[name='password']").setValue(updUserEntity.getPassword());
        $("button[type='submit']").click();

        $(byText("User account is locked")).should(visible);
    }

    @AllureId("268")
    @CreateUser(username = "TestUser1", password = "12345", enabled = true)
    @Test
    void checkDeleteUser(UserEntity user) {
        usersDAO.deleteUser(user);

        Allure.step("open page", () -> Selenide.open("http://127.0.0.1:3000/main"));
        $("a[href*='redirect']").click();
        $("input[name='username']").setValue(user.getUsername());
        $("input[name='password']").setValue(user.getPassword());
        $("button[type='submit']").click();

        $(byText("Bad credentials")).should(visible);
    }

    @AllureId("269")
    @CreateUser(username = "TestUser2", password = "12345", enabled = true)
    @Test
    void checkReadUser(UserEntity user) {
        Assertions.assertEquals(user, usersDAO.readUser(usersDAO.getUserId(user)));
    }
}