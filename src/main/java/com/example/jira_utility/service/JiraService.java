package com.example.jira_utility.service;

import com.example.jira_utility.model.Insight;
import com.example.jira_utility.model.JiraIssue;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JiraService {

    private final boolean USE_MOCK_DATA = true;

    public List<JiraIssue> fetchJiraData(String month) {
        if (USE_MOCK_DATA) {
            return fetchMockData(month);
        }
        return new ArrayList<>(); // For real Jira later
    }

    private List<JiraIssue> fetchMockData(String month) {
        List<JiraIssue> issues = new ArrayList<>();
        issues.add(createMockIssue("ISSUE-1", "EPIC-1", "qa1@example.com", 5, new String[]{"QA_Efforts"}));
        issues.add(createMockIssue("ISSUE-2", "EPIC-1", "qa1@example.com", 3, new String[]{}));
        issues.add(createMockIssue("ISSUE-3", "EPIC-2", "qa2@example.com", 8, new String[]{"QA_Efforts"}));
        issues.add(createMockIssue("ISSUE-4", "EPIC-3", "qa3@example.com", 4, new String[]{}));
        return issues;
    }

    private JiraIssue createMockIssue(String key, String epicKey, String assignee, int storyPoints, String[] labels) {
        JiraIssue issue = new JiraIssue();
        issue.setKey(key);
        issue.setEpicKey(epicKey);
        issue.setAssignee(assignee);
        issue.setStoryPoints(storyPoints);
        issue.setLabels(labels);
        return issue;
    }

    public Insight calculateInsights(List<JiraIssue> issues) {
        Insight insight = new Insight();
        Map<String, Integer> epicPoints = new HashMap<>();
        Map<String, Map<String, Integer>> individualEfforts = new HashMap<>();
        int totalPoints = 0;
        int releasePoints = 0;

        for (JiraIssue issue : issues) {
            totalPoints += issue.getStoryPoints();
            epicPoints.merge(issue.getEpicKey(), issue.getStoryPoints(), Integer::sum);
            if (Arrays.asList(issue.getLabels()).contains("QA_Efforts")) {
                releasePoints += issue.getStoryPoints();
            }
            individualEfforts.computeIfAbsent(issue.getAssignee(), k -> new HashMap<>())
                    .merge(issue.getEpicKey(), issue.getStoryPoints(), Integer::sum);
        }

        insight.setTotalTeamStoryPoints(totalPoints);
        insight.setEpicStoryPoints(epicPoints);
        insight.setReleaseStoryPoints(releasePoints);
        insight.setIndividualEfforts(individualEfforts);
        return insight;
    }
}