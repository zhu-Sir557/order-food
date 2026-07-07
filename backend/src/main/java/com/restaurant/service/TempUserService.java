package com.restaurant.service;

import com.restaurant.entity.TempUser;

public interface TempUserService {
    TempUser createTempUser();
    TempUser getByToken(String token);
}
