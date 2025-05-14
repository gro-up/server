package com.hamster.gro_up.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class RetrospectListResponse {
    private List<RetrospectResponse> retrospectList;

    public static RetrospectListResponse of(List<RetrospectResponse> retrospectList) {
        return new RetrospectListResponse(retrospectList);
    }
}
