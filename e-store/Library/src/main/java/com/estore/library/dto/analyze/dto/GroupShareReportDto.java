package com.estore.library.dto.analyze.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GroupShareReportDto {
    private String metric;
    private String groupBy;
    private String startMonth;
    private String endMonth;
    private List<GroupShareRowDto> rows;
    private List<Map<String, Object>> dynamics;
}
