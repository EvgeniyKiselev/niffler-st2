package niffler.test;

import io.qameta.allure.AllureId;
import niffler.api.UserService;
import niffler.jupiter.annotation.ClasspathUser;
import niffler.model.UserJson;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class UserDataTest {

    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .build();

    public static final Retrofit retrofit = new Retrofit.Builder()
            .client(httpClient)
            .baseUrl("http://127.0.0.1:8089")
            .addConverterFactory(JacksonConverterFactory.create())
            .build();

    private final UserService userService = retrofit.create(UserService.class);

    @ValueSource(strings = {
            "testdata/torvald.json",
            "testdata/jimmy.json"
    })
    @AllureId("105")
    @ParameterizedTest
    void userDataShouldBeUpdated(@ClasspathUser UserJson user) throws IOException {
        userService.updateUserInfo(user)
                .execute();
        UserJson afterUpdate = userService.currentUser(user.getUsername())
                .execute()
                .body();
        assert afterUpdate != null;
        Assertions.assertAll(
                () -> {
                    assertEquals(user.getUsername(), afterUpdate.getUsername());
                },
                () -> {
                    assertEquals(user.getFirstname(), afterUpdate.getFirstname());
                },
                () -> {
                    assertEquals(user.getSurname(), afterUpdate.getSurname());
                },
                () -> {
                    assertEquals(user.getCurrency(), afterUpdate.getCurrency());
                }
        );
    }
}