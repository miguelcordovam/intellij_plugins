package com.restdocs.action.common;

public enum SpringAnnotations {

    CONTROLLER("org.springframework.stereotype.Controller"),
    REST_CONTROLLER("org.springframework.web.bind.annotation.RestController"),
    REQUEST_PARAM("org.springframework.web.bind.annotation.RequestParam"),
    REQUEST_MAPPING_QUALIFIED_NAME("org.springframework.web.bind.annotation.RequestMapping"),
    GET_MAPPING_QUALIFIED_NAME("org.springframework.web.bind.annotation.GetMapping"),
    POST_MAPPING_QUALIFIED_NAME("org.springframework.web.bind.annotation.PostMapping"),
    PUT_MAPPING_QUALIFIED_NAME("org.springframework.web.bind.annotation.PutMapping"),
    DELETE_MAPPING_QUALIFIED_NAME("org.springframework.web.bind.annotation.DeleteMapping"),
    PATCH_MAPPING_QUALIFIED_NAME("org.springframework.web.bind.annotation.PatchMapping");

    private String qualifiedName;

    SpringAnnotations(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }
}
