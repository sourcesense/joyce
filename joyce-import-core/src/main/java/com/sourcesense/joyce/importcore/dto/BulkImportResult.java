package com.sourcesense.joyce.importcore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class BulkImportResult {

	private Integer processedDocuments;
	private List<SingleImportResult> results;
}
