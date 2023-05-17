package niffler.test;

import com.codeborne.selenide.Selenide;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureId;
import niffler.db.entity.UserEntity;
import niffler.jupiter.annotation.GenerateRandomUserEntity;
import niffler.jupiter.extension.GenerateRandomUserEntityExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

@ExtendWith(GenerateRandomUserEntityExtension.class)
public class LoginNewUserExtensionTest extends BaseWebTest {

    @GenerateRandomUserEntity
    @AllureId("265")
    @Test
    void loginTest(UserEntity userEntity) {
        Allure.step("open page", () -> Selenide.open("http://127.0.0.1:3000/main"));
        $("a[href*='redirect']").click();
        $("input[name='username']").setValue(userEntity.getUsername());
        $("input[name='password']").setValue(userEntity.getPassword());
        $("button[type='submit']").click();

        $("a[href*='friends']").click();
        $(".header").should(visible).shouldHave(text(nifflerTitle));
    }

}