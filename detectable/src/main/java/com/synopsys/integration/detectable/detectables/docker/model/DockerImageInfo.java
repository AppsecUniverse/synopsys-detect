package com.synopsys.integration.detectable.detectables.docker.model;

public class DockerImageInfo {
    private final String imageRepo;
    private final String imageTag;

    public DockerImageInfo(String imageRepo, String imageTag) {
        this.imageRepo = imageRepo;
        this.imageTag = imageTag;
    }

    public String getImageRepo() {
        return imageRepo;
    }

    public String getImageTag() {
        return imageTag;
    }
}
