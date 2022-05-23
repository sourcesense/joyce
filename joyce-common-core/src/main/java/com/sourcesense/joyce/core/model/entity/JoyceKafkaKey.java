package com.sourcesense.joyce.core.model.entity;

import com.sourcesense.joyce.core.enumeration.JoyceAction;
import com.sourcesense.joyce.core.model.uri.JoyceURI;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoyceKafkaKey<J extends JoyceURI, M extends JoyceKafkaKeyMetadata> {

	private J uri;
	private M metadata;

	@Builder.Default
	private JoyceAction action = JoyceAction.INSERT;
}
