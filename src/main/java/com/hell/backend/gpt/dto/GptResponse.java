package com.hell.backend.gpt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GptResponse {

    @Schema(description = "메시지 역할", example = "assistant")
    private String role;

    @Schema(description = "GPT의 응답 내용")
    private Content content;

    @Schema(description = "응답 상태", example = "accept")
    private String state;

    @Getter
    @Setter
    public static class Content {

        @Schema(description = "메시지", example = "초코바는 맛있죠! CU에서 드셨군요.")
        private String message;

        @Schema(description = "추가 데이터")
        private List<Data> values;
    }

    @Getter
    @Setter
    public static class Data {

        @Schema(description = "사용 일시", example = "2024-11-08T15:00:00+09:00")
        private String datetime;

        @Schema(description = "거래 유형", example = "expense")
        private String type;

        @Schema(description = "금액", example = "50000")
        private Integer amount;

        @Schema(description = "카테고리", example = "snack")
        private String category;

        @Schema(description = "사용처", example = "CU 강남대점")
        private String place;
    }
}
