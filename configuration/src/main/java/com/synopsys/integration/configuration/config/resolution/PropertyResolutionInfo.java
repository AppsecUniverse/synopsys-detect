package com.synopsys.integration.configuration.config.resolution;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

public class PropertyResolutionInfo {
    @NotNull
    private String source;
    @NotNull
    private String origin;
    @NotNull
    private String raw;

    public PropertyResolutionInfo(@NotNull String source, @NotNull String origin, @NotNull String raw) {
        Assert.notNull(source, "Source cannot be null.");
        Assert.notNull(origin, "Origin cannot be null.");
        Assert.notNull(raw, "Raw cannot be null.");
        this.source = source;
        this.origin = origin;
        this.raw = raw;
    }

    @NotNull
    public String getSource() {
        return source;
    }

    @NotNull
    public String getOrigin() {
        return origin;
    }

    @NotNull
    public String getRaw() {
        return raw;
    }
}
