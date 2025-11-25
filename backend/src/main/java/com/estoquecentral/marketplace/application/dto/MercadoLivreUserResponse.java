package com.estoquecentral.marketplace.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for Mercado Livre /users/me response
 * Story 5.1: Mercado Livre OAuth2 Authentication
 */
public class MercadoLivreUserResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("registration_date")
    private String registrationDate;

    @JsonProperty("country_id")
    private String countryId;

    @JsonProperty("email")
    private String email;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    // Constructors

    public MercadoLivreUserResponse() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
