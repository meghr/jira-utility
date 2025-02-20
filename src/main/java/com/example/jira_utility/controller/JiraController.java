package com.example.jira_utility.controller;

import com.example.jira_utility.model.Insight;
import com.example.jira_utility.model.JiraIssue;
import com.example.jira_utility.service.ExcelService;
import com.example.jira_utility.service.JiraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Controller // Must be present
public class JiraController {

    @Autowired
    private JiraService jiraService;

    @Autowired
    private ExcelService excelService;

    @GetMapping("/")
    public String showForm() {
        return "index";
    }

    @PostMapping("/fetch-insights")
    public String fetchInsights(@RequestParam("month") String month, Model model) {
        System.out.println("Received month: " + month); // Debug
        List<JiraIssue> issues = jiraService.fetchJiraData(month);
        Insight insight = jiraService.calculateInsights(issues);
        model.addAttribute("insight", insight);
        model.addAttribute("month", month);
        return "insights";
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel(@RequestParam("month") String month) throws IOException {
        List<JiraIssue> issues = jiraService.fetchJiraData(month);
        Insight insight = jiraService.calculateInsights(issues);
        byte[] excelBytes = excelService.generateExcel(insight, month);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=jira_insights_" + month + ".xlsx");
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }
}