package niffler.test;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureId;
import niffler.jupiter.annotation.User;
import niffler.jupiter.extension.UsersQueueGroupExtension;
import niffler.model.UserJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static niffler.jupiter.annotation.User.UserType.INVITATION_SENT;
import static niffler.jupiter.annotation.User.UserType.WITH_FRIENDS;

@ExtendWith(UsersQueueGroupExtension.class)
public class FriendsGroupWebTest extends BaseWebTest{

    private void reLogin(UserJson userJson) {
        $("a[href*='redirect']").click();
        $("input[name='username']").setValue(userJson.getUsername());
        $("input[name='password']").setValue(userJson.getPassword());
        $("button[type='submit']").click();
    }

    @AllureId("106")
    @Test
    void friendsShouldBeVisible0(@User(userType = WITH_FRIENDS) UserJson user1,
                                 @User(userType = INVITATION_SENT) UserJson user2) {
        Allure.step("open page", () -> Selenide.open("http://127.0.0.1:3000/main"));
        reLogin(user1);
        $("a[href*='friends']").click();
        $$(".table tbody tr").shouldHave(sizeGreaterThan(0));
        $("[data-tooltip-id='logout']").click();

        reLogin(user2);
        $("a[href*='people']").click();
        $$(".table tbody tr").find(Condition.text("Pending invitation")).should(visible);
    }

    @AllureId("107")
    @Test
    void friendsShouldBeVisible1(@User(userType = WITH_FRIENDS) UserJson user1,
                                 @User(userType = INVITATION_SENT) UserJson user2) {
        Allure.step("open page", () -> Selenide.open("http://127.0.0.1:3000/main"));
        reLogin(user1);
        $("a[href*='friends']").click();
        $$(".table tbody tr").shouldHave(sizeGreaterThan(0));
        $("[data-tooltip-id='logout']").click();

        reLogin(user2);
        $("a[href*='people']").click();
        $$(".table tbody tr").find(Condition.text("Pending invitation"))
                .should(visible);
    }

    @AllureId("108")
    @Test
    void friendsShouldBeVisible2(@User(userType = WITH_FRIENDS) UserJson user1,
                                 @User(userType = INVITATION_SENT) UserJson user2) {
        Allure.step("open page", () -> Selenide.open("http://127.0.0.1:3000/main"));
        reLogin(user1);
        $("a[href*='friends']").click();
        $$(".table tbody tr").shouldHave(sizeGreaterThan(0));
        $("[data-tooltip-id='logout']").click();

        reLogin(user2);
        $("a[href*='people']").click();
        $$(".table tbody tr").find(Condition.text("Pending invitation"))
                .should(visible);
    }
}
