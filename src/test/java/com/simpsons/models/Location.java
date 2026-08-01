package com.simpsons.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {

    private int id;
    private String description;
    private Integer firstAppearanceEpId;
    private Integer firstAppearanceShId;
    private String imagePath;
    private String name;
    private String town;
    private String use;
    private Episode firstAppearanceEp;
    private Episode firstAppearanceSh;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("first_appearance_ep_id")
    public Integer getFirstAppearanceEpId() {
        return firstAppearanceEpId;
    }

    @JsonProperty("first_appearance_ep_id")
    public void setFirstAppearanceEpId(Integer firstAppearanceEpId) {
        this.firstAppearanceEpId = firstAppearanceEpId;
    }

    @JsonProperty("first_appearance_sh_id")
    public Integer getFirstAppearanceShId() {
        return firstAppearanceShId;
    }

    @JsonProperty("first_appearance_sh_id")
    public void setFirstAppearanceShId(Integer firstAppearanceShId) {
        this.firstAppearanceShId = firstAppearanceShId;
    }

    @JsonProperty("image_path")
    public String getImagePath() {
        return imagePath;
    }

    @JsonProperty("image_path")
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    @JsonProperty("first_appearance_ep")
    public Episode getFirstAppearanceEp() {
        return firstAppearanceEp;
    }

    @JsonProperty("first_appearance_ep")
    public void setFirstAppearanceEp(Episode firstAppearanceEp) {
        this.firstAppearanceEp = firstAppearanceEp;
    }

    @JsonProperty("first_appearance_sh")
    public Episode getFirstAppearanceSh() {
        return firstAppearanceSh;
    }

    @JsonProperty("first_appearance_sh")
    public void setFirstAppearanceSh(Episode firstAppearanceSh) {
        this.firstAppearanceSh = firstAppearanceSh;
    }
}
