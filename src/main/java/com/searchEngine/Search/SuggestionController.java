package com.searchEngine.Search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {
    @Autowired
    private SuggestionsService suggestionsService;

    @GetMapping
    public ResponseEntity<List<SingleSuggestion>> getAllSuggestions(){
        return new ResponseEntity<List<SingleSuggestion>>(suggestionsService.getAllSuggestionsSortedByPopularity(), HttpStatus.OK);
    }

}
