package com.sourcesense.nile.core.model;

import lombok.Getter;

import java.net.URI;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Getter
public class NileURI {

    public enum Type {

        SCHEMA("schema"),
        CONTENT("content");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        private static final Map<String, Type> lookup = new HashMap<>();

        static {
            for (Type type : Type.values()) {
                lookup.put(type.getValue(), type);
            }
        }

        public static Optional<Type> get(String type) {
            return Optional.ofNullable(lookup.get(type));
        }

    }

    public enum Subtype {

        IMPORT("import"),
        MODEL("model");

        private final String value;

        Subtype(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        private static final Map<String, Subtype> lookup = new HashMap<>();

        static {
            for (Subtype type : Subtype.values()) {
                lookup.put(type.getValue(), type);
            }
        }

        public static Optional<Subtype> get(String type) {
            return Optional.ofNullable(lookup.get(type));
        }

    }

    private static final String schema = "nile";
    private Type type;
    private Subtype subtype;
    private String class_;
    private String id;

    private URI uri;

    public static NileURI make(Type type, Subtype subtype, String class_, String id) {
        return new NileURI(URI.create(String.format("%s://%s/%s/%s/%s", schema, type.getValue(), subtype.getValue(), class_, id)));
    }

    public static NileURI make(Type type, Subtype subtype, String class_) {
        return new NileURI(URI.create(String.format("%s://%s/%s/%s", schema, type.getValue(), subtype.getValue(), class_)));
    }

    public NileURI(URI uri) {
        this.uri = uri;
        this.type = Type.get(uri.getHost()).orElseThrow(() -> new IllegalArgumentException(String.format("Invalid type %s", uri.getHost())));
        List<String> paths = Arrays.stream(uri.getPath().split("/"))
                .filter(Predicate.not(String::isEmpty))
                .collect(Collectors.toList());
        if (paths.size() < 2) {
            throw new IllegalArgumentException(String.format("Missing class and id in URI %s", uri.getPath()));
        }
        this.subtype = Subtype.get(paths.get(0)).orElseThrow(() -> new IllegalArgumentException(String.format("Invalid subtype %s", paths.get(0))));
        this.class_ = paths.get(1);
        if (paths.size() > 2) {
            this.id = paths.get(2);
        }
    }

    public static Optional<NileURI> createURI(String uriString) {
        try {
            URI uri = new URI(uriString);
            if (!uri.getScheme().equals(schema)) {
                return Optional.empty();
            }
            NileURI nileUri = new NileURI(uri);

            return Optional.of(nileUri);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}
