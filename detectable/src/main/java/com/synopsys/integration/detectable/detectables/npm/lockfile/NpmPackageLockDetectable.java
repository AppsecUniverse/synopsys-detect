package com.synopsys.integration.detectable.detectables.npm.lockfile;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.common.util.finder.FileFinder;
import com.synopsys.integration.detectable.Detectable;
import com.synopsys.integration.detectable.DetectableEnvironment;
import com.synopsys.integration.detectable.detectable.Requirements;
import com.synopsys.integration.detectable.detectable.annotation.DetectableInfo;
import com.synopsys.integration.detectable.detectable.result.DetectableResult;
import com.synopsys.integration.detectable.detectable.result.PassedDetectableResult;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detectable.extraction.ExtractionEnvironment;

@DetectableInfo(language = "Node JS", forge = "npmjs", requirementsMarkdown = "File: package-lock.json. Optionally for better results: package.json also.")
public class NpmPackageLockDetectable extends Detectable {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final String PACKAGE_LOCK_JSON = "package-lock.json";
    public static final String PACKAGE_JSON = "package.json";

    private final FileFinder fileFinder;
    private final NpmLockfileExtractor npmLockfileExtractor;
    private final boolean includeDevDependencies;
    private final boolean includePeerDependencies;

    private File lockfile;
    private File packageJson;

    public NpmPackageLockDetectable(DetectableEnvironment environment, FileFinder fileFinder, NpmLockfileExtractor npmLockfileExtractor, NpmLockfileOptions npmLockfileOptions) {
        super(environment);
        this.fileFinder = fileFinder;
        this.npmLockfileExtractor = npmLockfileExtractor;
        this.includeDevDependencies = npmLockfileOptions.shouldIncludeDeveloperDependencies();
        this.includePeerDependencies = npmLockfileOptions.shouldIncludePeerDependencies();
    }

    @Override
    public DetectableResult applicable() {
        Requirements requirements = new Requirements(fileFinder, environment);
        lockfile = requirements.file(PACKAGE_LOCK_JSON);
        packageJson = requirements.optionalFile(PACKAGE_JSON, () -> logger.warn("Npm applied but it could not find a package.json so dependencies may not be entirely accurate."));
        return requirements.result();
    }

    @Override
    public DetectableResult extractable() {
        return new PassedDetectableResult();
    }

    @Override
    public Extraction extract(ExtractionEnvironment extractionEnvironment) {
        return npmLockfileExtractor.extract(lockfile, packageJson, includeDevDependencies, includePeerDependencies);
    }

}
