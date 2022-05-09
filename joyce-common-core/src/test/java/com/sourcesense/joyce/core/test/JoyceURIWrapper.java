package com.sourcesense.joyce.core.test;

import com.sourcesense.joyce.core.model.uri.JoyceURI;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoyceURIWrapper<J extends JoyceURI> {

	private Integer _id;
	private J joyceURI;
}
