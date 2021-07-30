package unit.com.sourcesense.joyce.importcore;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public interface UtilitySupplier {

	default CsvMapper initCsvMapper() {
		return new CsvMapper()
				.enable(CsvParser.Feature.TRIM_SPACES)
				.enable(CsvParser.Feature.ALLOW_COMMENTS)
				.enable(CsvParser.Feature.ALLOW_TRAILING_COMMA)
				.enable(CsvParser.Feature.INSERT_NULLS_FOR_MISSING_COLUMNS)
				.enable(CsvParser.Feature.SKIP_EMPTY_LINES);
	}

	default InputStream computeResourceAsBytes(String jsonFileName) {
		return this.getClass().getClassLoader().getResourceAsStream(jsonFileName);
	}

	default byte[] computeResourceAsByteArray(String path) throws IOException, URISyntaxException {

		URL url = this.getClass().getClassLoader().getResource(path);
		return Files.readAllBytes(Path.of(url.toURI()));
	}
}
