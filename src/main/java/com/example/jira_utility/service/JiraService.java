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

    // Add these properties to your application.properties or application.yml
    @Value("${jira.url}")
    private String jiraUrl;  // e.g., "https://your-domain.atlassian.net"

    @Value("${jira.username}")
    private String username;  // or API token

    @Value("${jira.password}")
    private String password;  // or API token

    @Value("${jira.project.key}")
    private String projectKey;

    public List<JiraIssue> fetchJiraData(String month) {
        String jql = String.format("project = %s AND updatedDate >= '%s-01' AND updatedDate <= '%s-31'",
                projectKey, month, month);
        return fetchIssuesFromJira(jql);
    }

    private List<JiraIssue> fetchIssuesFromJira(String jql) {
        List<JiraIssue> issues = new ArrayList<>();

        try {
            String url = jiraUrl + "/rest/api/2/search?jql=" + jql + "&maxResults=1000";

            // Set up headers for authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode issuesNode = root.path("issues");

            for (JsonNode issueNode : issuesNode) {
                JiraIssue issue = new JiraIssue();

                // Basic fields
                issue.setKey(issueNode.path("key").asText());

                // Epic link (custom field name might vary)
                JsonNode fields = issueNode.path("fields");
                String epicKey = fields.path("customfield_10011").asText(); // Adjust custom field ID
                issue.setEpicKey(epicKey.isEmpty() ? null : epicKey);

                // Assignee
                JsonNode assigneeNode = fields.path("assignee");
                String assigneeEmail = assigneeNode.path("emailAddress").asText();
                issue.setAssignee(assigneeEmail.isEmpty() ? null : assigneeEmail);

                // Story points (custom field name might vary)
                int storyPoints = fields.path("customfield_10024").asInt(0); // Adjust custom field ID
                issue.setStoryPoints(storyPoints);

                // Labels
                List<String> labels = new ArrayList<>();
                JsonNode labelsNode = fields.path("labels");
                labelsNode.forEach(label -> labels.add(label.asText()));
                issue.setLabels(labels.toArray(new String[0]));

                issues.add(issue);
            }

        } catch (Exception e) {
            // Handle errors appropriately (logging, etc.)
            e.printStackTrace();
        }

        return issues;
    }

    // Keep your existing calculateInsights method as is
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