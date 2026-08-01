package com.simpsons.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Character {

    private int id;
    private Integer age;
    private String birthdate;
    private String description;
    private Integer firstAppearanceEpId;
    private Integer firstAppearanceShId;
    private String gender;
    private String name;
    private String occupation;
    private List<String> phrases;
    private String portraitPath;
    private String status;
    private Episode firstAppearanceEp;
    private Episode firstAppearanceSh;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public List<String> getPhrases() {
        return phrases;
    }

    public void setPhrases(List<String> phrases) {
        this.phrases = phrases;
    }

    @JsonProperty("portrait_path")
    public String getPortraitPath() {
        return portraitPath;
    }

    @JsonProperty("portrait_path")
    public void setPortraitPath(String portraitPath) {
        this.portraitPath = portraitPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
