package com.github.gather.service;

import com.github.gather.dto.request.UserEditRequest;
import com.github.gather.dto.response.*;
import com.github.gather.entity.User;
import com.github.gather.exception.UserNotFoundException;
import com.github.gather.repositroy.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserManagementService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IntroductionService introductionService;

    public UserInfoResponse getUserInfo(String email) throws UserNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자 정보를 찾지 못했습니다."));

        LocationResponse locationResponse = user.getLocationId() != null ?
                new LocationResponse(user.getLocationId().getLocationId(), user.getLocationId().getName()) : null;

        Long categoryId = null;
        String categoryName = null;
        if(user.getCategory() != null) {
            categoryId = user.getCategory().getCategoryId();
            categoryName = user.getCategory().getCategoryName();
        }

        GroupCategoryResponse category = new GroupCategoryResponse(categoryId, categoryName);

        IntroductionResponse introductionResponse = introductionService.getIntroduction();

        // Even if the user location and category are missing,
        // return the other user data:
        return new UserInfoResponse(
                user.getUserId(),
                user.getNickname(),
                user.getEmail(),
                user.getImage(),
                locationResponse,
                category,
                introductionResponse
        );
    }

    // remaining code

    public UserEditResponse editUserInfo(Long userId, UserEditRequest userEditRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("회원 정보를 찾을 수 없습니다."));


        // 사용자 정보 수정 로직
        user.setNickname(userEditRequest.getNickname());
        user.setEmail(userEditRequest.getEmail());

        // 새로운 비밀번호 인코딩하여 설정
        String encodedPassword = passwordEncoder.encode(userEditRequest.getPassword());
        user.setPassword(encodedPassword);

        user.setPhoneNumber(userEditRequest.getPhoneNumber());
        user.setImage(userEditRequest.getImage());
        user.setLocationId(userEditRequest.getLocation());


        // 수정된 정보 저장
        User updatedUser = userRepository.save(user);

        // 수정된 정보를 Response 객체로 변환
        UserEditResponse response = new UserEditResponse();
        response.setUserId(updatedUser.getUserId());
        response.setNickname(updatedUser.getNickname());
        response.setEmail(updatedUser.getEmail());
        response.setPassword(encodedPassword);  // 인코딩된 비밀번호 설정
        response.setPhoneNumber(updatedUser.getPhoneNumber());
        response.setImage(updatedUser.getImage());
        response.setLocation(updatedUser.getLocationId());

        return response;
    }
}
