package com.nexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterviewQuestionsResponse {
    private List<String> technical;
    private List<String> behavioral;
    private List<String> gapBased;
    private String focusArea;
}