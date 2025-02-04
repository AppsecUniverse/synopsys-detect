package com.synopsys.integration.detectable.detectables.maven.parsing;

import java.io.File;

import com.synopsys.integration.common.util.finder.FileFinder;
import com.synopsys.integration.detectable.Detectable;
import com.synopsys.integration.detectable.DetectableEnvironment;
import com.synopsys.integration.detectable.detectable.Requirements;
import com.synopsys.integration.detectable.detectable.annotation.DetectableInfo;
import com.synopsys.integration.detectable.detectable.result.DetectableResult;
import com.synopsys.integration.detectable.detectable.result.PassedDetectableResult;
import com.synopsys.integration.detectable.detectable.result.PropertyInsufficientDetectableResult;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detectable.extraction.ExtractionEnvironment;

@DetectableInfo(language = "various", forge = "Maven Central", requirementsMarkdown = "File: pom.xml.")
public class MavenParseDetectable extends Detectable {
    private static final String POM_XML_FILENAME = "pom.xml";

    private final FileFinder fileFinder;
    private final MavenParseExtractor mavenParseExtractor;
    private final MavenParseOptions mavenParseOptions;

    private File pomXmlFile;

    public MavenParseDetectable(DetectableEnvironment environment, FileFinder fileFinder, MavenParseExtractor mavenParseExtractor, MavenParseOptions mavenParseOptions) {
        super(environment);
        this.fileFinder = fileFinder;
        this.mavenParseExtractor = mavenParseExtractor;
        this.mavenParseOptions = mavenParseOptions;
    }

    @Override
    public DetectableResult applicable() {
        if (mavenParseOptions.isEnableLegacyMode()) {
            Requirements requirements = new Requirements(fileFinder, environment);
            pomXmlFile = requirements.file(POM_XML_FILENAME);
            return requirements.result();
        } else {
            return new PropertyInsufficientDetectableResult("Maven legacy buildless parse must be enabled.");
        }
    }

    @Override
    public DetectableResult extractable() {
        return new PassedDetectableResult();
    }

    @Override
    public Extraction extract(ExtractionEnvironment extractionEnvironment) {
        return mavenParseExtractor.extract(pomXmlFile, mavenParseOptions);
    }

}
