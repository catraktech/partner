package com.catrak.exatip.partner.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterPartnerDTO implements Serializable {

    @JsonProperty
    private String partner;

    @JsonProperty
    private String contactNumber;

    @JsonProperty
    private String email;

    public RegisterPartnerDTO() {
        super();
    }

    @JsonCreator
    public static RegisterPartnerDTO of(@JsonProperty("partner") String partner,
            @JsonProperty("contactNumber") String contactNumber, @JsonProperty("email") String email) {
        RegisterPartnerDTO person = new RegisterPartnerDTO();
        person.partner = partner;
        person.contactNumber = contactNumber;
        person.email = email;

        return person;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}