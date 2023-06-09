package DataBase;

import CostumDataStructures.WordData;
import CostumDataStructures.WordToSearch;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.*;

public class WordsDataCollection {

    MongoDatabase SearchEngineDB;
    MongoCollection<Document> wordsDataCollection;

    //           {
//        "word": "string",
//            "dataFrequency": "number",
//            "documents": [   // nested documents?? list
//            {
//                "count": "number",
//                "url": "string",
//                "popularity": "number",
//                "lengthOfDocument": "number",
//                "filePath": "string",
//                "position": "number"
//        },
//        {
//            "count": "number",
//                "url": "string",
//                "popularity": "number",
//                "lengthOfDocument": "number",
//                "filePath": "string",
//                "position": "number"
//        },
//    ...
//  ]
//    }
    protected WordsDataCollection(MongoDatabase db) {
        this.SearchEngineDB = db;
        this.wordsDataCollection = this.SearchEngineDB.getCollection("Words");
    }

    /////////////////////////////////ANY QUERY ON WORDS COLLECTION SHOULD BE WRITTEN HERE/////////////////////////////////

    public void updateWordToSearchData(String word, WordData data) {
        Document newDocument = new Document()
                .append("count", data.count)
                .append("url", data.url)
                .append("lengthOfDocument", data.lengthOfDoc)
                .append("filePath", data.filepath)
                .append("position", data.position)
                .append("popularity", data.popularity);
        Document query = new Document("word", word);
        //get the word needed for update
        Document result = wordsDataCollection.find(query).first();
        //if the word is already in the database
        if (result != null) {
            //Increment the dataFrequency by 1
            int dataFrequency = result.getInteger("dataFrequency", 0);
            int updatedDataFrequency = dataFrequency + 1;

            Document update = new Document("$set", new Document("dataFrequency", updatedDataFrequency))
                    .append("$push", new Document("documents", newDocument));
            wordsDataCollection.updateOne(query, update);
        }
        //if the word is not in the database, add one
        else {
            Document newWord = new Document("word", word)
                    .append("dataFrequency", 1)
                    .append("documents", new ArrayList<>(List.of(newDocument)));
            wordsDataCollection.insertOne(newWord);
        }
    }

    public void insertWordHashMap(Map<String, WordToSearch> mapToInsert) {
        List<Document> documentsList = new ArrayList<>();
        for (Map.Entry<String, WordToSearch> entry : mapToInsert.entrySet()) {
            WordToSearch wordToSearch = entry.getValue();
            Document document = new Document();
            document.append("word", wordToSearch.word)
                    .append("dataFrequency", wordToSearch.df);

            // Convert WordData objects to Document and add to the list
            List<Document> wordDataDocuments = new ArrayList<>();
            for (WordData wordData : wordToSearch.dataMap.values()) {
                Document wordDataDocument = new Document();
                wordDataDocument.append("count", wordData.count)
                        .append("url", wordData.url)
                        .append("popularity", wordData.popularity)
                        .append("lengthOfDocument", wordData.lengthOfDoc)
                        .append("filePath", wordData.filepath)
                        .append("position", wordData.position);

                wordDataDocuments.add(wordDataDocument);
            }

            document.append("documents", wordDataDocuments);

            documentsList.add(document);
        }
        this.wordsDataCollection.insertMany(documentsList);
    }

    // fill WordToSearch object
    // TODO: change this to be used in query processor to get words in search query
    public Map<String, WordToSearch> getWordMapToSearch() {
        Map<String, WordToSearch> map = new HashMap();
        try (MongoCursor<Document> cur = this.wordsDataCollection.find().cursor()) {
            while (cur.hasNext()) {
                Document doc = cur.next();
                WordToSearch word = new WordToSearch();
                word.word = doc.getString("word");
                word.df = doc.getInteger("dataFrequency");
                List<Document> nestedDocs = doc.getList("documents", Document.class);
                for (Document nestedDoc : nestedDocs) {
                    WordData wd = new WordData(nestedDoc);
                    word.data.add(wd);
                }
                map.put(word.word, word);
            }
        }
        return map;
    }

    public Map<String, WordToSearch> getWordToSearch(List<String> words) {
        Bson filter = Filters.in("word", words);
        Bson projection = Projections.include("word", "dataFrequency", "documents");
        Map<String, WordToSearch> map = new HashMap<>();
        try (MongoCursor<Document> cursor = this.wordsDataCollection.find(filter).projection(projection).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                WordToSearch word = new WordToSearch();
                word.word = doc.getString("word");
                word.df = doc.getInteger("dataFrequency");
                List<Document> nestedDocs = doc.getList("documents", Document.class);
                for (Document nestedDoc : nestedDocs) {
                    WordData wd = new WordData(nestedDoc);
                    word.data.add(wd);
                }
                map.put(word.word, word);
            }
        }
        return map;
    }
}
