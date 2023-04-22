package niffler.jupiter.extension;

import niffler.api.SpendService;
import niffler.jupiter.annotation.GenerateSpend;
import niffler.model.CategoryJson;
import niffler.model.SpendJson;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.extension.*;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.Date;

public class GenerateSpendWithCategoryExtension implements ParameterResolver, BeforeEachCallback {

    public static ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
            .create(GenerateSpendWithCategoryExtension.class);

    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .build();

    private static final Retrofit retrofit = new Retrofit.Builder()
            .client(httpClient)
            .baseUrl("http://127.0.0.1:8093")
            .addConverterFactory(JacksonConverterFactory.create())
            .build();

    private final SpendService spendService = retrofit.create(SpendService.class);

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        GenerateSpend generateSpend = context.getRequiredTestMethod()
                .getAnnotation(GenerateSpend.class);

        if (generateSpend != null) {
            SpendJson spend = new SpendJson();
            spend.setUsername(generateSpend.username());
            spend.setAmount(generateSpend.amount());
            spend.setDescription(generateSpend.description());
            spend.setCategory(generateSpend.category());
            spend.setSpendDate(new Date());
            spend.setCurrency(generateSpend.currency());

            CategoryJson category = new CategoryJson();
            category.setUsername(generateSpend.username());
            category.setCategory(generateSpend.category());
            spendService.addCategory(category).execute().body();

            SpendJson created = spendService.addSpend(spend)
                    .execute()
                    .body();
            context.getStore(NAMESPACE).put("spend", created);
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(SpendJson.class);
    }

    @Override
    public SpendJson resolveParameter(ParameterContext parameterContext,
                                      ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE).get("spend", SpendJson.class);
    }
}