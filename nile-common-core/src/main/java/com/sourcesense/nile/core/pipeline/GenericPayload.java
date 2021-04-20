package com.sourcesense.nile.core.pipeline;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class GenericPayload<T> implements Serializable {

    protected T data;
}
