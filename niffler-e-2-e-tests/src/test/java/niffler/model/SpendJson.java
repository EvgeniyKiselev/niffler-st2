package niffler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class SpendJson {
    @JsonProperty("id")
    private UUID id;
    @JsonProperty("spendDate")
    private Date spendDate;
    @JsonProperty("category")
    private String category;
    @JsonProperty("currency")
    private CurrencyValues currency;
    @JsonProperty("amount")
    private Double amount;
    @JsonProperty("description")
    private String description;
    @JsonProperty("username")
    private String username;

    public SpendJson() {
    }

}
