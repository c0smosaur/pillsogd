package gist.pilldispenser.domain.users.controller;

import gist.pilldispenser.common.security.UsersDetails;
import gist.pilldispenser.domain.users.model.KakaoUserInfoResponse;
import gist.pilldispenser.domain.users.model.OAuthTokenResponse;
import gist.pilldispenser.domain.users.service.OAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    @GetMapping("/login/oauth2/code/kakao")
    public KakaoUserInfoResponse kakaoLogin(@RequestParam("code") String code) throws IOException {
        log.info("code: "+code);
        OAuthTokenResponse tokenResponse = oAuthService.getKakaoToken(code);
        KakaoUserInfoResponse userInfo = oAuthService.kakaoUserInfo(tokenResponse.getAccessToken());

        oAuthService.saveAccessTokenToRedis(tokenResponse, userInfo.getEmail());
        oAuthService.saveRefreshTokenToRedis(tokenResponse, userInfo.getEmail());
        return userInfo;
    }

    @GetMapping("/oauth2/token")
    public void kakaoTokenRenew(@AuthenticationPrincipal UsersDetails usersDetails) throws IOException {
        oAuthService.reissueTokens(usersDetails);
    }

}