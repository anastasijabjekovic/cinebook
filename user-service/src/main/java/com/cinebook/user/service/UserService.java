package com.cinebook.user.service;

import com.cinebook.user.dto.CreateUserRequest;
import com.cinebook.user.dto.UserResponse;

import java.util.List;

public interface UserService {

    List<UserResponse> getAllUsers();

    UserResponse createUser(CreateUserRequest request);
}
