package com.example.jira_utility.model;

import lombok.Data;

@Data
public class JiraIssue {
    private String key;
    private String epicKey;
    private String assignee;
    private int storyPoints;
    private String[] labels;
}