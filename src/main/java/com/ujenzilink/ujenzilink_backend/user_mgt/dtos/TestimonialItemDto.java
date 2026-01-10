package com.ujenzilink.ujenzilink_backend.user_mgt.dtos;

public class TestimonialItemDto {
    private String quote;
    private String name;
    private String title;
    private String profile;

    public TestimonialItemDto() {
    }

    public TestimonialItemDto(String quote, String name, String title, String profile) {
        this.quote = quote;
        this.name = name;
        this.title = title;
        this.profile = profile;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
