package niffler.test;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Configuration;
import niffler.jupiter.annotation.GenerateCategory;
import niffler.jupiter.annotation.GenerateSpend;
import niffler.jupiter.extension.GenerateSpendWithCategoryExtension;
import niffler.model.CurrencyValues;
import niffler.model.SpendJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;
import static niffler.test.BaseWebTest.PASSWORD;
import static niffler.test.BaseWebTest.USER;

@ExtendWith(GenerateSpendWithCategoryExtension.class)
public class SpendWithCategoryWebTest {
    @BeforeEach
    void doLogin() {
        Configuration.baseUrl = "http://127.0.0.1:3000";

        open("/main");
        $("a[href*='redirect']").click();
        $("input[name='username']").setValue(USER);
        $("input[name='password']").setValue(PASSWORD);
        $("button[type='submit']").click();
    }

    @GenerateSpend(
            username = USER,
            description = "QA GURU ADVANCED VOL 2",
            currency = CurrencyValues.RUB,
            amount = 50000.00,
            category = "Learning"
    )
    @GenerateCategory(
            username = USER,
            category = "Learning"
    )
    @Test
    public void spendShouldBeDeletedByActionInTable(SpendJson spend) {
        open("/profile");
        $(".main-content__section-categories ul")
                .shouldHave(text(spend.getCategory()));

        open("/main");
        $(".spendings-table tbody").$$("tr")
                .find(text(spend.getDescription()))
                .$$("td").first()
                .scrollTo()
                .click();

        $$(".button_type_small").find(text("Delete selected"))
                .click();

        $(".spendings-table tbody")
                .$$("tr")
                .shouldHave(CollectionCondition.size(0));
    }
}
