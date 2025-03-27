package com.example.licenta_v2.model;

import java.util.List;

public class PlantDetailsResponse {
    private String common_name;
    private String scientific_name;
    private Description description;
    private String family;
    private String image_url;
    private Taxonomy taxonomy;
    private List<String> synonyms;
    private List<String> edible_parts;
    private Watering watering;
    private List<String> propagation_methods;
    private String best_light_condition;
    private String imageUrl;

    public String getCommonName() {
        return common_name;
    }
    public String getBestLightCondition() {
        return best_light_condition;
    }
    public String getScientificName() {
        return scientific_name;
    }

    public Description getDescription() {
        return description;
    }

    public String getFamily() {
        return family;
    }

    public String getImageUrl() {
        return image_url;
    }

    public Taxonomy getTaxonomy() {
        return taxonomy;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public List<String> getEdibleParts() {
        return edible_parts;
    }

    public Watering getWatering() {
        return watering;
    }

    public List<String> getPropagationMethods() {
        return propagation_methods;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "PlantDetailsResponse{" +
                "common_name='" + common_name + '\'' +
                ", scientific_name='" + scientific_name + '\'' +
                ", description='" + (description != null ? description.getValue() : "null") + '\'' +
                ", family='" + family + '\'' +
                ", image_url='" + image_url + '\'' +
                ", taxonomy=" + taxonomy +
                ", synonyms=" + synonyms +
                ", edible_parts=" + edible_parts +
                ", watering=" + (watering != null ? watering.toString() : "null") +
                ", propagation_methods=" + propagation_methods +
                ", best_light_condition='" + best_light_condition + '\'' +
        '}';
    }

    public void setCommonName(String common_name) {
        this.common_name = common_name;
    }


    public static class Description {
        private String value;
        private String citation;
        private String license_name;
        private String license_url;

        public String getValue() { return value; }
        public String getCitation() { return citation; }
        public String getLicenseName() { return license_name; }
        public String getLicenseUrl() { return license_url; }

        @Override
        public String toString() {
            return value != null ? value : "";
        }
    }

    public static class Taxonomy {
        private String kingdom;
        private String phylum;
        private String className; // avoid "class" keyword
        private String order;
        private String family;
        private String genus;

        public String getKingdom() { return kingdom; }
        public String getPhylum() { return phylum; }
        public String getClassName() { return className; }
        public String getOrder() { return order; }
        public String getFamily() { return family; }
        public String getGenus() { return genus; }

        @Override
        public String toString() {
            return "Taxonomy{" +
                    "kingdom='" + kingdom + '\'' +
                    ", phylum='" + phylum + '\'' +
                    ", className='" + className + '\'' +
                    ", order='" + order + '\'' +
                    ", family='" + family + '\'' +
                    ", genus='" + genus + '\'' +
                    '}';
        }
    }

    public static class Watering {
        private String min;
        private String max;

        public String getMin() { return min; }
        public String getMax() { return max; }

        @Override
        public String toString() {
            return (min != null ? min : "") + " – " + (max != null ? max : "");
        }
    }
}
