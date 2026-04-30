package com.nexus.backend.dto;

import lombok.Data;

@Data
public class PracticeReportRequest {
    private String jobRole;
    private String transcript;
    private String question;
    private Integer eyeContactScore;
    private Integer postureScore;
    private Integer fillerWordCount;
    private Integer speakingPaceWpm;
}