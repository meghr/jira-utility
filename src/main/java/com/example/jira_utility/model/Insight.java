package com.example.jira_utility.model;

import lombok.Data;
import java.util.Map;

@Data
public class Insight {
    private int totalTeamStoryPoints;
    private Map<String, Integer> epicStoryPoints;
    private int releaseStoryPoints;
    private Map<String, Map<String, Integer>> individualEfforts;
}