package niffler.test;


import com.codeborne.selenide.Configuration;
import niffler.jupiter.annotation.WebTest;

@WebTest
public abstract class BaseWebTest {

  static final String USER = "mrkiseleff";

  static final String PASSWORD = "qwer321";

  String nifflerTitle = "Niffler. The coin keeper.";

  static {
    Configuration.browserSize = "1920x1080";
  }

}
