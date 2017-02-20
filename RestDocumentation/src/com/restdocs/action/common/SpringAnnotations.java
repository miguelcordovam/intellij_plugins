package com.restdocs.action.common;

public enum SpringAnnotations {

    CONTROLLER("org.springframework.stereotype.Controller"),
    REQUEST_PARAM("org.springframework.web.bind.annotation.RequestParam"),
    REQUEST_MAPPING_QUALIFIED_NAME("org.springframework.web.bind.annotation.RequestMapping");

    SpringAnnotations(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    private String qualifiedName;

    public String getQualifiedName() {
        return qualifiedName;
    }
}
