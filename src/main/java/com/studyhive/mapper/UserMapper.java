package com.studyhive.mapper;

import com.studyhive.model.entity.User;
import com.studyhive.model.response.UserGlobalProfileResponse;
import com.studyhive.model.response.UserProfileResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserProfileResponse toUserProfileResponse(User user);

    UserGlobalProfileResponse toGlobalProfileResponse(User user);
}
