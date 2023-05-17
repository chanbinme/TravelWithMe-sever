package com.frog.travelwithme.intergration.feed;

import com.frog.travelwithme.domain.feed.controller.dto.FeedDto;
import com.frog.travelwithme.domain.feed.entity.Tag;
import com.frog.travelwithme.domain.feed.repository.TagRepository;
import com.frog.travelwithme.domain.feed.service.FeedService;
import com.frog.travelwithme.domain.feed.service.TagService;
import com.frog.travelwithme.domain.member.controller.dto.MemberDto;
import com.frog.travelwithme.domain.member.service.MemberService;
import com.frog.travelwithme.global.config.AES128Config;
import com.frog.travelwithme.global.exception.ErrorResponse;
import com.frog.travelwithme.global.exception.ExceptionCode;
import com.frog.travelwithme.global.security.auth.controller.dto.TokenDto;
import com.frog.travelwithme.global.security.auth.jwt.JwtTokenProvider;
import com.frog.travelwithme.global.security.auth.userdetails.CustomUserDetails;
import com.frog.travelwithme.intergration.BaseIntegrationTest;
import com.frog.travelwithme.utils.ObjectMapperUtils;
import com.frog.travelwithme.utils.ResultActionsUtils;
import com.frog.travelwithme.utils.StubData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
class FeedIntegrationTest extends BaseIntegrationTest {

    private final String BASE_URL = "/feed";

    private final String EMAIL = StubData.MockMember.getEmail();

    private final String TAG_NAME = StubData.MockFeed.getTagName();

    private final int SIZE = StubData.MockFeed.getSize();

    private long feedId;

    @Autowired
    private FeedService feedService;

    @Autowired
    private TagService tagService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private AES128Config aes128Config;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void beforEach() {
        MemberDto.SignUp signUpDto = StubData.MockMember.getSignUpDto();
        memberService.signUp(signUpDto);
        Tag tagOne = Tag.builder().name(TAG_NAME + "1").build();
        tagRepository.save(tagOne);
        Tag tagTwo = Tag.builder().name(TAG_NAME + "2").build();
        tagRepository.save(tagTwo);
        FeedDto.Post postDto = StubData.MockFeed.getPostDto();
        FeedDto.Response response = feedService.postFeed(this.EMAIL, postDto);
        feedId = response.getId();
    }

    @Test
    @DisplayName("피드 작성")
    void feedControllerTest1() throws Exception {
        // given
        feedService.deleteFeed(EMAIL, feedId);
        CustomUserDetails userDetails = StubData.MockMember.getUserDetails();
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(userDetails);
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();
        String encryptedRefreshToken = aes128Config.encryptAes(refreshToken);
        FeedDto.Post postDto = StubData.MockFeed.getPostDto();
        MockMultipartFile file = new MockMultipartFile("file",
                "test.png",
                "image/png",
                "fileContent".getBytes());

        // when
        String uri = UriComponentsBuilder.newInstance().path(BASE_URL)
                .build().toUri().toString();
        String json = ObjectMapperUtils.asJsonString(postDto);
        ResultActions actions = ResultActionsUtils.postRequestWithContentAndTokenAndMultiPart(
                mvc, uri, json, accessToken, encryptedRefreshToken, file);

        // then
        FeedDto.Response response = ObjectMapperUtils.actionsSingleToResponseWithData(
                actions, FeedDto.Response.class);
        log.info("response.getContents() : {}", response.getContents());
        log.info("postDto.getContents() : {}", postDto.getContents());
        assertThat(response.getContents()).isEqualTo(postDto.getContents());
        assertThat(response.getTags()).isEqualTo(postDto.getTags());
        assertThat(response.getLocation()).isEqualTo(postDto.getLocation());
        assertThat(response.getCommentCount()).isZero();
        assertThat(response.getLikeCount()).isZero();
        assertThat(response.getNickname()).isNotNull();
        actions
                .andExpect(status().isCreated())
                /*.andDo(document("post-feed",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        RequestSnippet.getPostFeedSnippet(),
                        ResponseSnippet.getFeedSnippet()))*/;
    }

    @Test
    @DisplayName("피드 조회")
    void feedControllerTest2() throws Exception {
        // given
        CustomUserDetails userDetails = StubData.MockMember.getUserDetails();
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(userDetails);
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();
        String encryptedRefreshToken = aes128Config.encryptAes(refreshToken);
        // when
        String uri = UriComponentsBuilder.newInstance().path(BASE_URL + "/" + feedId)
                .build().toUri().toString();
        ResultActions actions = ResultActionsUtils.getRequestWithToken(mvc, uri, accessToken, encryptedRefreshToken);

        // then
        FeedDto.Response response = ObjectMapperUtils.actionsSingleToResponseWithData(
                actions, FeedDto.Response.class);
        assertThat(response.getId()).isNotNull();
        assertThat(response.getContents()).isNotNull();
        assertThat(response.getNickname()).isNotNull();
        assertThat(response.getTags()).isNotNull();
        actions
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("모든 피드 조회")
    void feedControllerTest3() throws Exception {
        // given
        CustomUserDetails userDetails = StubData.MockMember.getUserDetails();
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(userDetails);
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();
        String encryptedRefreshToken = aes128Config.encryptAes(refreshToken);

        // when
        String uri = UriComponentsBuilder.newInstance().path(BASE_URL)
                .build().toUri().toString();
        ResultActions actions = ResultActionsUtils.getRequestWithToken(
                mvc, uri, accessToken, encryptedRefreshToken);

        // then
        // TODO: actionsMultiToResponseWithData 필요
        actions
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("피드 수정")
    void feedControllerTest4() throws Exception {
        // given
        CustomUserDetails userDetails = StubData.MockMember.getUserDetails();
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(userDetails);
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();
        String encryptedRefreshToken = aes128Config.encryptAes(refreshToken);
        FeedDto.Patch patchDto = StubData.MockFeed.getPatchDto();

        // when
        String json = ObjectMapperUtils.asJsonString(patchDto);
        String uri = UriComponentsBuilder.newInstance().path(BASE_URL + "/" + feedId)
                .build().toUri().toString();
        ResultActions actions = ResultActionsUtils.patchRequestWithContentAndToken(
                mvc, uri, json, accessToken, encryptedRefreshToken);

        // then
        FeedDto.Response response = ObjectMapperUtils.actionsSingleToResponseWithData(
                actions, FeedDto.Response.class);
        assertThat(response.getContents()).isEqualTo(patchDto.getContents());
        assertThat(response.getTags()).isEqualTo(response.getTags());
        assertThat(response.getLocation()).isEqualTo(patchDto.getLocation());
        assertThat(response.getCommentCount()).isZero();
        assertThat(response.getLikeCount()).isZero();
        assertThat(response.getNickname()).isNotNull();
        actions
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("피드 삭제")
    void feedControllerTest5() throws Exception {
        // given
        CustomUserDetails userDetails = StubData.MockMember.getUserDetails();
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(userDetails);
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();
        String encryptedRefreshToken = aes128Config.encryptAes(refreshToken);

        // when
        String uri = UriComponentsBuilder.newInstance().path(BASE_URL + "/" + feedId)
                .build().toUri().toString();
        ResultActions actions = ResultActionsUtils.deleteRequestWithToken(
                mvc, uri, accessToken, encryptedRefreshToken);

        // then
        actions
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Tag Name과 유사한 모든 TAG 조회")
    void feedControllerTest6() throws Exception {
        // given
        CustomUserDetails userDetails = StubData.MockMember.getUserDetails();
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(userDetails);
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();
        String encryptedRefreshToken = aes128Config.encryptAes(refreshToken);
        MultiValueMap<String, String> tagNamePapram = new LinkedMultiValueMap<>();
        MultiValueMap<String, String> sizeParam = new LinkedMultiValueMap<>();
        tagNamePapram.add(TAG_NAME, TAG_NAME);
        sizeParam.add("size", String.valueOf(SIZE));
        log.info("tagOne : {}", tagRepository.findAll().get(0).getName());

        // when
        String uri = UriComponentsBuilder.newInstance().path(BASE_URL + "/tags")
                .build().toUri().toString();
        ResultActions actions = ResultActionsUtils.getRequestWithTokenAndTwoParams(
                mvc, uri, accessToken, encryptedRefreshToken, tagNamePapram, sizeParam);

        // then
        // TODO: actionsMultiToResponseWithData 필요
        actions
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Feed 좋아요")
    void feedControllerTest7() throws Exception {
        // given
        CustomUserDetails userDetails = StubData.MockMember.getUserDetails();
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(userDetails);
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();
        String encryptedRefreshToken = aes128Config.encryptAes(refreshToken);

        // when
        String uri = UriComponentsBuilder.newInstance().path(BASE_URL + "/" + feedId + "/likes")
                .build().toUri().toString();
        ResultActions actions = ResultActionsUtils.postRequestWithToken(
                mvc, uri, accessToken, encryptedRefreshToken);

        // then
        FeedDto.Response response = feedService.findFeedById(userDetails.getEmail(), feedId);
        assertThat(response.getLikeCount()).isPositive();
        actions
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Feed 좋아요 취소")
    void feedControllerTest8() throws Exception {
        // given
        CustomUserDetails userDetails = StubData.MockMember.getUserDetails();
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(userDetails);
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();
        String encryptedRefreshToken = aes128Config.encryptAes(refreshToken);
        feedService.doLike(userDetails.getEmail(), feedId);

        // when
        String uri = UriComponentsBuilder.newInstance().path(BASE_URL + "/" + feedId + "/likes")
                .build().toUri().toString();
        ResultActions actions = ResultActionsUtils.deleteRequestWithToken(
                mvc, uri, accessToken, encryptedRefreshToken);

        // then
        FeedDto.Response response = feedService.findFeedById(userDetails.getEmail(), feedId);
        assertThat(response.getLikeCount()).isZero();
        actions
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("이미 좋아요한 Feed를 좋아요할 때 예외 발생")
    void feedControllerTest9() throws Exception {
        // given
        CustomUserDetails userDetails = StubData.MockMember.getUserDetails();
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(userDetails);
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();
        String encryptedRefreshToken = aes128Config.encryptAes(refreshToken);
        feedService.doLike(userDetails.getEmail(), feedId);

        // when
        String uri = UriComponentsBuilder.newInstance().path(BASE_URL + "/" + feedId + "/likes")
                .build().toUri().toString();
        ResultActions actions = ResultActionsUtils.postRequestWithToken(
                mvc, uri, accessToken, encryptedRefreshToken);

        // then
        ErrorResponse errorResponse = ObjectMapperUtils.actionsSingleToResponse(actions, ErrorResponse.class);
        assertThat(errorResponse.getMessage()).isEqualTo(ExceptionCode.ALREADY_LIKED_FEED.getMessage());
        assertThat(errorResponse.getStatus()).isEqualTo(ExceptionCode.ALREADY_LIKED_FEED.getStatus());
        actions
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Feed 좋아요를 하지 않았을 때 좋아요 취소를 하게 되면 예외 발생")
    void feedControllerTest10() throws Exception {
        // given
        CustomUserDetails userDetails = StubData.MockMember.getUserDetails();
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(userDetails);
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();
        String encryptedRefreshToken = aes128Config.encryptAes(refreshToken);

        // when
        String uri = UriComponentsBuilder.newInstance().path(BASE_URL + "/" + feedId + "/likes")
                .build().toUri().toString();
        ResultActions actions = ResultActionsUtils.deleteRequestWithToken(
                mvc, uri, accessToken, encryptedRefreshToken);

        // then
        ErrorResponse errorResponse = ObjectMapperUtils.actionsSingleToResponse(actions, ErrorResponse.class);
        assertThat(errorResponse.getMessage()).isEqualTo(ExceptionCode.UNABLE_TO_CANCEL_LIKE.getMessage());
        assertThat(errorResponse.getStatus()).isEqualTo(ExceptionCode.UNABLE_TO_CANCEL_LIKE.getStatus());
        actions
                .andExpect(status().is4xxClientError());
    }
}
