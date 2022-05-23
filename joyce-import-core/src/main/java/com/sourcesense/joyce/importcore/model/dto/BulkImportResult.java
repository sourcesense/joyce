package com.sourcesense.joyce.importcore.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BulkImportResult {

	private Integer total;
	private Integer imported;
	private Integer skipped;
	private Integer failed;
}
