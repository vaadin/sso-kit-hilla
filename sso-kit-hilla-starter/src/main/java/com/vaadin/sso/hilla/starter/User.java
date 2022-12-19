package com.vaadin.sso.hilla.starter;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dev.hilla.Nonnull;

public class User {

    @Nonnull
    private String username;
    @Nonnull
    private String name;
    @JsonIgnore
    private String hashedPassword;
    @Nonnull
    private Set<String> roles;
    @Nonnull
    private byte[] profilePicture;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

}
