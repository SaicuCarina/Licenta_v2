package com.example.licenta_v2.model;

import java.util.List;

public class PlantDetailsResponse {
    private String common_name;
    private String scientificName;
    private Description description;
    private String family;
    private Taxonomy taxonomy;
    private List<String> synonyms;
    private List<String> edibleParts;
    private Watering watering;
    private List<String> propagationMethods;
    private String bestLightCondition;
    private String imageUrl;
    private boolean isFavorite = false;

    public boolean isFavorite() {
        return isFavorite;
    }

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

    public void setEdibleParts(List<String> edibleParts) {
        this.edibleParts = edibleParts;
    }

    public void setWatering(Watering watering) {
        this.watering = watering;
    }

    public void setPropagationMethods(List<String> propagationMethods) {
        this.propagationMethods = propagationMethods;
    }

    public void setBestLightCondition(String bestLightCondition) {
        this.bestLightCondition = bestLightCondition;
    }


    public String getCommonName() {
        return common_name;
    }
    public String getBestLightCondition() {
        return bestLightCondition;
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
        return edibleParts;
    }

    public Watering getWatering() {
        return watering;
    }

    public List<String> getPropagationMethods() {
        return propagationMethods;
    }

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
                ", edible_parts=" + edibleParts +
                ", watering=" + (watering != null ? watering.toString() : "null") +
                ", propagation_methods=" + propagationMethods +
                ", best_light_condition='" + bestLightCondition + '\'' +
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
