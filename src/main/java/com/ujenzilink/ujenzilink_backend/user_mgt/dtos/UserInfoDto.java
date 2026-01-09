package com.ujenzilink.ujenzilink_backend.user_mgt.dtos;

public class UserInfoDto {
    private String name;
    private String profile;

    public UserInfoDto() {
    }

    public UserInfoDto(String name, String profile) {
        this.name = name;
        this.profile = profile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
