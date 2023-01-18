/*-
 * Copyright (C) 2022-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package dev.hilla.sso.endpoint;

import java.util.Set;
import java.util.stream.Collectors;

import dev.hilla.Nonnull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class User {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final int ROLE_PREFIX_LENGTH = ROLE_PREFIX.length();

    private String birthdate;
    private String email;
    private String familyName;
    private String fullName;
    private String gender;
    private String givenName;
    private String locale;
    private String middleName;
    private String nickName;
    private String phoneNumber;
    private String picture;
    private String preferredUsername;

    @Nonnull
    private Set<@Nonnull String> roles = Set.of();

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getPreferredUsername() {
        return preferredUsername;
    }

    public void setPreferredUsername(String preferredUsername) {
        this.preferredUsername = preferredUsername;
    }

    @Nonnull
    public Set<@Nonnull String> getRoles() {
        return roles;
    }

    public void setRoles(@Nonnull Set<@Nonnull String> roles) {
        this.roles = roles;
    }

    public static User from(OidcUser ou) {
        User user = new User();
        user.setBirthdate(ou.getBirthdate());
        user.setEmail(ou.getEmail());
        user.setFamilyName(ou.getFamilyName());
        user.setFullName(ou.getFullName());
        user.setGender(ou.getGender());
        user.setGivenName(ou.getGivenName());
        user.setLocale(ou.getLocale());
        user.setMiddleName(ou.getMiddleName());
        user.setNickName(ou.getNickName());
        user.setPhoneNumber(ou.getPhoneNumber());
        user.setPicture(ou.getPicture());
        user.setPreferredUsername(ou.getPreferredUsername());

        user.setRoles(
                ou.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                        .filter(a -> a.startsWith(ROLE_PREFIX))
                        .map(a -> a.substring(ROLE_PREFIX_LENGTH))
                        .collect(Collectors.toSet()));
        return user;
    }
}
