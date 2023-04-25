package niffler.jupiter.extension;

import niffler.api.UserService;
import niffler.jupiter.annotation.GenerateUser;
import niffler.model.UserJson;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.extension.*;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class GenerateUserExtension implements ParameterResolver, BeforeEachCallback {

    public static ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
            .create(GenerateUserExtension.class);

    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .build();

    public static final Retrofit retrofit = new Retrofit.Builder()
            .client(httpClient)
            .baseUrl("http://127.0.0.1:8089")
            .addConverterFactory(JacksonConverterFactory.create())
            .build();

    private final UserService userService = retrofit.create(UserService.class);

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        GenerateUser annotation = context.getRequiredTestMethod()
                .getAnnotation(GenerateUser.class);

        if (annotation != null) {
            UserJson userJson = new UserJson();
            userJson.setUsername(annotation.username());
            userJson.setFirstname(annotation.firstname());
            userJson.setSurname(annotation.surname());
            userJson.setCurrency(annotation.currency());

            UserJson created = userService.updateUserInfo(userJson)
                    .execute()
                    .body();
            context.getStore(NAMESPACE).put("userJson", created);
            System.out.println(userJson);
        }
    }


    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(UserJson.class);
    }

    @Override
    public UserJson resolveParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE).get("userJson", UserJson.class);
    }
}