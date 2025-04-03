package com.example.licenta_v2.model;

import java.io.Serializable;
import java.util.List;

public class PlantDetailsResponse implements Serializable {
    private String id;
    private String common_name;
    private String scientificName;
    private Description description;
    private String family;
    private Taxonomy taxonomy;
    private List<String> synonyms;
    private List<String> edible_parts;
    private Watering watering;
    private List<String> propagation_methods;
    private String best_light_condition;
    private String imageUrl;
    private boolean isFavorite = false;
    private String wateringInterval;
    private String best_watering;
    private String best_soil_type;
    private String toxicity;
    private String cultural_significance;
    private List<String> gpt;
    private int toxic;


    public boolean isFavorite() {
        return isFavorite;
    }

    public void setToxic(int toxic) { this.toxic = toxic; }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public void setTaxonomy(Taxonomy taxonomy) {
        this.taxonomy = taxonomy;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    public void setEdibleParts(List<String> edible_parts) {
        this.edible_parts = edible_parts;
    }

    public void setWatering(Watering watering) {
        this.watering = watering;
    }

    public void setPropagationMethods(List<String> propagation_methods) {
        this.propagation_methods = propagation_methods;
    }

    public void setBestLightCondition(String best_light_condition) {
        this.best_light_condition = best_light_condition;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setWateringInterval(String wateringInterval){
        this.wateringInterval = wateringInterval;
    }
    public void setToxicity(String toxicity){
        this.toxicity = toxicity;
    }

    public String getId() {
        return id;
    }
    public String getWateringInterval() { return wateringInterval; }
    public int getToxic() { return toxic; }
    public String getCommonName() {
        return common_name;
    }
    public String getBestLightCondition() {
        return best_light_condition;
    }
    public String getScientificName() {
        return scientificName;
    }

    public Description getDescription() {
        return description;
    }

    public String getFamily() {
        return family;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Setter
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public String getBest_watering() { return best_watering; }
    public String getBest_soil_type() { return best_soil_type; }
    public String getToxicity() { return toxicity; }
    public String getCultural_significance() { return cultural_significance; }

    public List<String> getGpt() {
        return gpt;
    }

    public void setGpt(List<String> gpt) {
        this.gpt = gpt;
    }

    public List<String> getPropagationMethods() {
        return propagation_methods;
    }
    private String lightSummary;
    private String careLevel;


    @Override
    public String toString() {
        return "PlantDetailsResponse{" +
                "common_name='" + common_name + '\'' +
                ", scientific_name='" + scientificName + '\'' +
                ", description='" + (description != null ? description.getValue() : "null") + '\'' +
                ", family='" + family + '\'' +
                ", image_url='" + imageUrl + '\'' +
                ", taxonomy=" + taxonomy +
                ", synonyms=" + synonyms +
                ", edible_parts=" + edible_parts +
                ", watering=" + (watering != null ? watering.toString() : "null") +
                ", propagation_methods=" + propagation_methods +
                ", best_light_condition='" + best_light_condition + '\'' +
                ", best_watering='" + best_watering + '\'' +
                ", best_soil_type='" + best_soil_type + '\'' +
                ", toxicity='" + toxicity + '\'' +
                ", cultural_significance='" + cultural_significance + '\'' +
        '}';
    }

    public void setCommonName(String common_name) {
        this.common_name = common_name;
    }

    public void setLightSummary(String lightSummary) {
        this.lightSummary = lightSummary;
    }
    public String getLightSummary() {
        return lightSummary;
    }

    public void setCareLevel(String careLevel) {
        this.careLevel = careLevel;
    }
    public String getCareLevel() {
        return careLevel;
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

        public void setValue(String value) {
            this.value = value;
        }

        public void setCitation(String citation) {
            this.citation = citation;
        }

        public void setLicenseName(String license_name) {
            this.license_name = license_name;
        }

        public void setLicenseUrl(String license_url) {
            this.license_url = license_url;
        }


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
