package com.hamster.gro_up.entity;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Step {
    DOCUMENT("서류"),
    CODING_TEST("코딩테스트"),
    ASSIGNMENT_TEST("과제 테스트"),
    FIRST_INTERVIEW("1차 면접"),
    SECOND_INTERVIEW("2차 면접"),
    THIRD_INTERVIEW("3차 면접"),
    FINAL_PASS("최종 합격"),
    FAIL("불합격");

    private final String displayName;

    Step(String displayName) {
        this.displayName = displayName;
    }

    public static Step of(String step) {
        return Arrays.stream(Step.values())
                .filter(r -> r.name().equalsIgnoreCase(step))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("유효하지 않은 Step"));
    }
}
