package com.example.villagerservice.party.dto;

import com.example.villagerservice.member.domain.Member;
import com.example.villagerservice.party.domain.Party;
import com.example.villagerservice.party.domain.PartyComment;
import com.example.villagerservice.party.domain.PartyState;
import com.example.villagerservice.party.domain.PartyTag;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class PartyDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request{
        @NotEmpty(message = "모임 이름을 입력해주세요.")
        private String partyName;

        @NotNull(message = "모임 점수를 입력해주세요.")
        private Integer score;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate startDt;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate endDt;


        private Integer amount;

        @Min(1)
        private Integer numberPeople;

        private String location;

        private Double latitude;

        private Double longitude;

        private String content;
        @Size(min = 1 , max = 4)
        private List<PartyTag> tagList;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Response{

        private String partyName;

        private Integer score;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate startDt;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate endDt;

        private Integer amount;

        private Integer numberPeople;

        private String location;

        private String content;

        private List<String> tagNameList;

        private List<PartyCommentDTO.Response> commentList;

        private String nickname;

        private Integer mannerPoint;

        private Boolean PartyLike;

        private Long memberId;

        private PartyState state;

        private List<MemberClub> memberClubs;

        private Integer partyPeople;

        public static PartyDTO.Response createPartyResponse(Party party , List<PartyComment> partyCommentList , Boolean PartyLike , List<Member> memberList) {
            Response response = Response.builder()
                    .partyName(party.getPartyName())
                    .score(party.getScore())
                    .startDt(party.getStartDt())
                    .endDt(party.getEndDt())
                    .amount(party.getAmount())
                    .numberPeople(party.getNumberPeople())
                    .location(party.getLocation())
                    .content(party.getContent())
                    .state(party.getState())
                    .tagNameList(new ArrayList<>())
                    .commentList(new ArrayList<>())
                    .memberClubs(new ArrayList<>())
                    .partyPeople(memberList.size())
                    .nickname(party.getMember().getMemberDetail().getNickname())
                    .mannerPoint(party.getMember().getMemberDetail().getMannerPoint().getPoint())
                    .PartyLike(PartyLike)
                    .memberId(party.getMember().getId())
                    .build();

            for (PartyTag partyTag : party.getTagList()) {
                response.tagNameList.add(partyTag.getTagName());
            }

            for (PartyComment partyComment : partyCommentList) {

                boolean isOwner = party.getMember().getMemberDetail().getNickname().equals(partyComment.getNickname());

                response.commentList.add(PartyCommentDTO.Response.builder()
                                .contents(partyComment.getContents())
                                .nickName(partyComment.getNickname())
                                .isOwner(isOwner)
                                .partyCommentId(partyComment.getId())
                                .partyId(partyComment.getParty().getId()).build());
            }

            for (Member member : memberList) {
                response.memberClubs.add(MemberClub.builder()
                        .memberName(member.getMemberDetail().getNickname())
                        .memberId(member.getId())
                        .build());
            }

            return response;
        }
    }

}
