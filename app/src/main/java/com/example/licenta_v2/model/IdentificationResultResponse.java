package com.example.licenta_v2.model;

import java.util.List;

public class IdentificationResultResponse {
    private Result result;

    public Result getResult() {
        return result;
    }

    public static class Result {
        private Classification classification;

        public Classification getClassification() {
            return classification;
        }
    }

    public static class Classification {
        private List<Suggestion> suggestions;

        public List<Suggestion> getSuggestions() {
            return suggestions;
        }
    }

    public static class Suggestion {
        private PlantDetailsResponse details;
        private String id; // <- PLANT ID pentru KB
        private String name;

        public PlantDetailsResponse getDetails() {
            return details;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

}
