package niffler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class CategoryJson {
    @JsonProperty("id")
    private UUID id;
    @JsonProperty("category")
    private String category;
    @JsonProperty("username")
    private String username;
}