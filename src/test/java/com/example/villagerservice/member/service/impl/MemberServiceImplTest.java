package com.example.villagerservice.member.service.impl;

import com.example.villagerservice.member.domain.Member;
import com.example.villagerservice.member.exception.MemberException;
import com.example.villagerservice.member.domain.MemberRepository;
import com.example.villagerservice.member.request.MemberCreate;
import com.example.villagerservice.member.request.MemberInfoUpdate;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.example.villagerservice.member.exception.MemberErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    @DisplayName("회원 가입 시 이메일 중복 검사 테스트")
    void createMemberDuplicateTest() {
        // given
        String nickname = "테스트입니다.";
        String email = "test@gmail.com";
        String pass = "123456789";

        MemberCreate memberCreate = createMemberRequest(nickname, email, pass);
        Member member = createMember(email, nickname, pass);

        given(memberRepository.findByEmail(anyString()))
                .willReturn(Optional.of(member));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        // when
        MemberException memberException = assertThrows(MemberException.class, () -> {
            memberService.createMember(memberCreate);
        });

        // then
        verify(memberRepository, times(1)).findByEmail(captor.capture());
        assertThat(captor.getValue()).isEqualTo(email);
        assertThat(memberException.getMemberErrorCode()).isEqualTo(MEMBER_DUPLICATE_ERROR);
    }

    @Test
    @DisplayName("회원 가입 테스트")
    void createMemberTest() {
        // given
        String nickname = "테스트입니다.";
        String email = "test@gmail.com";
        String pass = "123456789";

        MemberCreate memberCreate = createMemberRequest(nickname, email, pass);
        Member member = createMember(email, nickname, pass);

        given(memberRepository.findByEmail(anyString()))
                .willReturn(Optional.empty());

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);

        // when
        memberService.createMember(memberCreate);

        // then
        verify(memberRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getNickname()).isEqualTo(nickname);
        assertThat(captor.getValue().getEmail()).isEqualTo(email);
        assertThat(captor.getValue().getEncodedPassword()).isEqualTo(pass);
    }

    @Test
    @DisplayName("회원정보 변경 시 회원이 없을 경우 테스트")
    void updateMemberInfoNotFoundTest() {
        // given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        MemberException memberException = assertThrows(MemberException.class,
                () -> memberService.updateMemberInfo("test@gamil.com", any()));

        // then
        assertThat(memberException.getMemberErrorCode()).isEqualTo(MEMBER_NOT_FOUND);
        assertThat(memberException.getErrorCode()).isEqualTo(MEMBER_NOT_FOUND.getErrorCode());
        assertThat(memberException.getErrorMessage()).isEqualTo(MEMBER_NOT_FOUND.getErrorMessage());
    }

    @Test
    @DisplayName("회원 정보 변경 시 변경할 데이터가 비어있을 경우 테스트")
    void updateMemberInfoEmptyTest() {
        // given
        MemberInfoUpdate memberInfoUpdate = MemberInfoUpdate.builder()
                .build();

        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(Member.builder().build()));

        // when
        MemberException memberException = assertThrows(MemberException.class,
                () -> memberService.updateMemberInfo("test@gamil.com", memberInfoUpdate));

        // then
        assertThat(memberException.getMemberErrorCode()).isEqualTo(MEMBER_VALID_NOT);
        assertThat(memberException.getErrorCode()).isEqualTo(MEMBER_VALID_NOT.getErrorCode());
        assertThat(memberException.getErrorMessage()).isEqualTo(MEMBER_VALID_NOT.getErrorMessage());
    }

    @Test
    @DisplayName("회원정보 변경 테스트")
    void updateMemberInfoTest() {
        // given
        Member member = Member.builder()
                .nickname("원래 닉네임")
                .build();

        Member mockMember = spy(member);

        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(mockMember));

        MemberInfoUpdate memberInfoUpdate = MemberInfoUpdate.builder()
                .nickname("변경 닉네임")
                .build();
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        // when
        memberService.updateMemberInfo("test@gamil.com", memberInfoUpdate);

        // then
        verify(mockMember, times(1)).updateMemberInfo(captor.capture());
        assertThat(captor.getValue()).isEqualTo("변경 닉네임");
        assertThat(mockMember.getNickname()).isEqualTo("변경 닉네임");
    }

    @Test
    @DisplayName("회원 비밀번호 변경 시 회원이 없을 경우 테스트")
    void updateMemberPasswordNotFoundTest() {
        // given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        MemberException memberException = assertThrows(MemberException.class,
                () -> memberService.updateMemberPassword("test@gamil.com", any()));

        // then
        assertThat(memberException.getMemberErrorCode()).isEqualTo(MEMBER_NOT_FOUND);
        assertThat(memberException.getErrorCode()).isEqualTo(MEMBER_NOT_FOUND.getErrorCode());
        assertThat(memberException.getErrorMessage()).isEqualTo(MEMBER_NOT_FOUND.getErrorMessage());
    }

    @Test
    @DisplayName("회원 비밀번호 변경 시 비밀번호 데이터가 비어있을 경우 테스트")
    void updateMemberPasswordEmptyTest() {
        // given

        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(Member.builder().build()));

        // when
        MemberException memberException = assertThrows(MemberException.class,
                () -> memberService.updateMemberPassword("test@gamil.com", ""));

        // then
        assertThat(memberException.getMemberErrorCode()).isEqualTo(MEMBER_VALID_NOT);
        assertThat(memberException.getErrorCode()).isEqualTo(MEMBER_VALID_NOT.getErrorCode());
        assertThat(memberException.getErrorMessage()).isEqualTo(MEMBER_VALID_NOT.getErrorMessage());
    }

    @Test
    @DisplayName("회원 비밀번호 변경 시 비밀번호가 동일할 경우 테스트")
    @Disabled
    void updateMemberPasswordSameTest() {
        // given
        String password = "test1234";

        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(Member.builder().build()));


        doReturn(true)
                .when(passwordEncoder).matches(any(), anyString());


        // when
        MemberException memberException = assertThrows(MemberException.class,
                () -> memberService.updateMemberPassword("test@gamil.com", "test1234"));

        // then
        assertThat(memberException.getMemberErrorCode()).isEqualTo(MEMBER_UPDATE_SAME_PASS);
        assertThat(memberException.getErrorCode()).isEqualTo(MEMBER_UPDATE_SAME_PASS.getErrorCode());
        assertThat(memberException.getErrorMessage()).isEqualTo(MEMBER_UPDATE_SAME_PASS.getErrorMessage());
    }

    private Member createMember(String nickname, String email, String pass) {
        return Member.builder()
                .nickname(nickname)
                .email(email)
                .encodedPassword(pass)
                .build();
    }

    private MemberCreate createMemberRequest(String nickname, String email, String pass) {
        return MemberCreate.builder()
                .nickname(nickname)
                .email(email)
                .password(pass)
                .build();
    }
}