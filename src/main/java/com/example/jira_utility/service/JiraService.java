package com.example.jira_utility.service;

import com.example.jira_utility.model.Insight;
import com.example.jira_utility.model.JiraIssue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Service
public class JiraService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${jira.url}")
    private String jiraUrl;

    @Value("${jira.username}")
    private String username;

    @Value("${jira.password}")
    private String password;

    @Value("${jira.project.key}")
    private String projectKey;

    // Inject the list of epic keys from configuration
    @Value("${jira.epic.keys}")
    private List<String> epicKeys;

    public List<JiraIssue> fetchJiraData(String month) {
        String jql;
        if (epicKeys == null || epicKeys.isEmpty()) {
            // If no epic keys configured, fetch all issues for the project and month
            jql = String.format("project = %s AND updatedDate >= '%s-01' AND updatedDate <= '%s-31'",
                    projectKey, month, month);
        } else {
            // Fetch issues for the configured epic keys using customfield_29800
            String epicKeysList = String.join(", ", epicKeys); // e.g., "XYZ, ABC, PQR"
            jql = String.format("project = %s AND updatedDate >= '%s-01' AND updatedDate <= '%s-31' AND customfield_29800 IN (%s)",
                    projectKey, month, month, epicKeysList);
        }
        return fetchIssuesFromJira(jql);
    }

    private List<JiraIssue> fetchIssuesFromJira(String jql) {
        List<JiraIssue> issues = new ArrayList<>();

        try {
            String url = jiraUrl + "/rest/api/2/search?jql=" + jql + "&maxResults=1000";

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode issuesNode = root.path("issues");

            for (JsonNode issueNode : issuesNode) {
                JiraIssue issue = new JiraIssue();

                issue.setKey(issueNode.path("key").asText());

                JsonNode fields = issueNode.path("fields");
                String epicKeyFromField = fields.path("customfield_xyxx").asText();
                issue.setEpicKey(epicKeyFromField.isEmpty() ? null : epicKeyFromField);

                String assigneeName = fields.path("customfield_abcd").asText();
                issue.setAssignee(assigneeName.isEmpty() ? null : assigneeName);

                int storyPoints = fields.path("customfield_efgh").asInt(0);
                issue.setStoryPoints(storyPoints);

                List<String> labels = new ArrayList<>();
                JsonNode labelsNode = fields.path("labels");
                labelsNode.forEach(label -> labels.add(label.asText()));
                issue.setLabels(labels.toArray(new String[0]));

                issues.add(issue);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return issues;
    }

    // calculateInsights method remains unchanged
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