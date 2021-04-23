package com.sourcesense.nile.connectorcore.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Data
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//Todo: capire perchè non riesce ad iniettarsi il bean
//@Document("#{databaseCollections.getDataInfo()}")
@Document("info")
public abstract class DataInfo {

    @Id
    protected String _id;
    protected Integer version = 0;
    protected String schemaKey;
    protected Date insertDate = new Date();

    protected DataInfo(
            String _id,
            Integer version,
            String schemaKey) {

        this._id = _id;
        this.version = version;
        this.schemaKey = schemaKey;
    }
}
