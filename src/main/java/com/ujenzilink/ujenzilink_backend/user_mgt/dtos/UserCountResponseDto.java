package com.ujenzilink.ujenzilink_backend.user_mgt.dtos;

import java.util.List;

public class UserCountResponseDto {
    private long totalUsers;

    private List<UserInfoDto> users;

    public UserCountResponseDto() {
    }

    public UserCountResponseDto(long totalUsers, List<UserInfoDto> users) {
        this.totalUsers = totalUsers;
        this.users = users;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public List<UserInfoDto> getUsers() {
        return users;
    }

    public void setUsers(List<UserInfoDto> users) {
        this.users = users;
    }
}
