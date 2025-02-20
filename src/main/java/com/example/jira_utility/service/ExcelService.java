package com.example.jira_utility.service;

import com.example.jira_utility.model.Insight;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ExcelService {

    public byte[] generateExcel(Insight insight, String month) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Insights - " + month);
        int rowNum = 0;

        Row header = sheet.createRow(rowNum++);
        header.createCell(0).setCellValue("Metric");
        header.createCell(1).setCellValue("Value");

        sheet.createRow(rowNum++).createCell(0).setCellValue("Total Team Story Points");
        sheet.getRow(rowNum - 1).createCell(1).setCellValue(insight.getTotalTeamStoryPoints());

        sheet.createRow(rowNum++).createCell(0).setCellValue("Release Testing (QA_Efforts)");
        sheet.getRow(rowNum - 1).createCell(1).setCellValue(insight.getReleaseStoryPoints());

        for (var epic : insight.getEpicStoryPoints().entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("Epic: " + epic.getKey());
            row.createCell(1).setCellValue(epic.getValue());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }
}