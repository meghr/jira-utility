package com.example.jira_utility;

import com.example.jira_utility.model.Insight;
import com.example.jira_utility.model.JiraIssue;
import com.example.jira_utility.service.JiraService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JiraServiceTest {

    @Autowired
    private JiraService jiraService;

    @Test
    public void testFetchMockData() {
        List<JiraIssue> issues = jiraService.fetchJiraData("2025-02");
        assertEquals(4, issues.size());
        assertEquals("EPIC-1", issues.get(0).getEpicKey());
    }

    @Test
    public void testCalculateInsights() {
        List<JiraIssue> issues = jiraService.fetchJiraData("2025-02");
        Insight insight = jiraService.calculateInsights(issues);
        assertEquals(20, insight.getTotalTeamStoryPoints());
        assertEquals(13, insight.getReleaseStoryPoints());
    }
}