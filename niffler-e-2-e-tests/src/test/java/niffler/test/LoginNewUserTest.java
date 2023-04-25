package niffler.test;

import com.codeborne.selenide.Selenide;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureId;
import niffler.db.dao.NifflerUsersDAO;
import niffler.db.dao.NifflerUsersDAOJdbc;
import niffler.db.entity.Authority;
import niffler.db.entity.AuthorityEntity;
import niffler.db.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class LoginNewUserTest extends BaseWebTest {

  private NifflerUsersDAO usersDAO = new NifflerUsersDAOJdbc();
  private UserEntity ue;

  @BeforeEach
  void createUserForTest() {
    ue = new UserEntity();
    ue.setUsername("eva");
    ue.setPassword("12345");
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
  }

  @AllureId("97")
  @Test
  void loginTest() {
    Allure.step("open page", () -> Selenide.open("http://127.0.0.1:3000/main"));
    $("a[href*='redirect']").click();
    $("input[name='username']").setValue(ue.getUsername());
    $("input[name='password']").setValue(ue.getPassword());
    $("button[type='submit']").click();

    $("a[href*='friends']").click();
    $(".header").should(visible).shouldHave(text("Niffler. The coin keeper."));
  }

}
