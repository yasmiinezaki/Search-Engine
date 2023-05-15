package com.searchEngine.Search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "results")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Results {
    @Id
    private ObjectId id;
    private List<SingleResult> results;


}
