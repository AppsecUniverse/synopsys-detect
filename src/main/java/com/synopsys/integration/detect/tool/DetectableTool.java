/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detect.configuration.enumeration.DetectTool;
import com.synopsys.integration.detect.configuration.enumeration.ExitCodeType;
import com.synopsys.integration.detect.lifecycle.run.data.DockerTargetData;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodePublisher;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeRequest;
import com.synopsys.integration.detect.tool.detector.CodeLocationConverter;
import com.synopsys.integration.detect.tool.detector.extraction.ExtractionEnvironmentProvider;
import com.synopsys.integration.detect.workflow.codelocation.DetectCodeLocation;
import com.synopsys.integration.detect.workflow.project.DetectToolProjectInfo;
import com.synopsys.integration.detect.workflow.status.Status;
import com.synopsys.integration.detect.workflow.status.StatusEventPublisher;
import com.synopsys.integration.detect.workflow.status.StatusType;
import com.synopsys.integration.detectable.Detectable;
import com.synopsys.integration.detectable.DetectableEnvironment;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;
import com.synopsys.integration.detectable.detectable.executable.ExecutableFailedException;
import com.synopsys.integration.detectable.detectable.result.DetectableResult;
import com.synopsys.integration.detectable.detectable.result.ExceptionDetectableResult;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detectable.extraction.ExtractionEnvironment;
import com.synopsys.integration.detector.base.DetectableCreatable;
import com.synopsys.integration.util.NameVersion;

public class DetectableTool {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DetectableCreatable detectableCreatable;
    private final ExtractionEnvironmentProvider extractionEnvironmentProvider;
    private final CodeLocationConverter codeLocationConverter;
    private final String name;
    private final DetectTool detectTool;
    private final StatusEventPublisher statusEventPublisher;
    private final ExitCodePublisher exitCodePublisher;

    //TODO: Move docker/bazel out of detectable and drop this notion of a detctable tool. Will simplify this logic and make this unneccessary.
    private Detectable detectable;
    private File sourcePath;

    public DetectableTool(DetectableCreatable detectableCreatable, ExtractionEnvironmentProvider extractionEnvironmentProvider, CodeLocationConverter codeLocationConverter,
        String name, DetectTool detectTool, StatusEventPublisher statusEventPublisher, ExitCodePublisher exitCodePublisher) {
        this.codeLocationConverter = codeLocationConverter;
        this.name = name;
        this.detectableCreatable = detectableCreatable;
        this.extractionEnvironmentProvider = extractionEnvironmentProvider;
        this.detectTool = detectTool;
        this.statusEventPublisher = statusEventPublisher;
        this.exitCodePublisher = exitCodePublisher;
    }

    public boolean initializeAndCheckForApplicable(File sourcePath) { //TODO: Move docker/bazel out of detectable and drop this notion of a detctable tool. Will simplify this logic and make this unneccessary.
        logger.trace("Starting a detectable tool.");

        DetectableEnvironment detectableEnvironment = new DetectableEnvironment(sourcePath);
        detectable = detectableCreatable.createDetectable(detectableEnvironment);
        DetectableResult applicable = detectable.applicable();

        if (!applicable.getPassed()) {
            logger.debug("Was not applicable.");
            return false;
        }

        logger.debug("Applicable passed.");
        return true;
    }

    public DetectableToolResult extract() { //TODO: Move docker/bazel out of detectable and drop this notion of a detctable tool. Will simplify this logic and make this unneccessary.
        DetectableResult extractable;
        try {
            extractable = detectable.extractable();
        } catch (DetectableException e) {
            extractable = new ExceptionDetectableResult(e);
        }

        if (!extractable.getPassed()) {
            logger.error("Was not extractable: " + extractable.toDescription());
            statusEventPublisher.publishStatusSummary(new Status(name, StatusType.FAILURE));
            exitCodePublisher.publishExitCode(ExitCodeType.FAILURE_GENERAL_ERROR, extractable.toDescription());
            return DetectableToolResult.failed(extractable);
        }

        logger.debug("Extractable passed.");

        ExtractionEnvironment extractionEnvironment = extractionEnvironmentProvider.createExtractionEnvironment(name);
        Extraction extraction;
        try {
            extraction = detectable.extract(extractionEnvironment);
        } catch (ExecutableFailedException e) {
            extraction = Extraction.fromFailedExecutable(e);
        }

        if (!extraction.isSuccess()) {
            logger.error("Extraction was not success.");
            statusEventPublisher.publishStatusSummary(new Status(name, StatusType.FAILURE));
            exitCodePublisher.publishExitCode(new ExitCodeRequest(ExitCodeType.FAILURE_GENERAL_ERROR, extractable.toDescription()));
            return DetectableToolResult.failed();
        } else {
            logger.debug("Extraction success.");
            statusEventPublisher.publishStatusSummary(new Status(name, StatusType.SUCCESS));
        }

        Map<CodeLocation, DetectCodeLocation> detectCodeLocationMap = codeLocationConverter.toDetectCodeLocation(sourcePath, extraction, sourcePath, name);
        List<DetectCodeLocation> detectCodeLocations = new ArrayList<>(detectCodeLocationMap.values());

        DockerTargetData dockerTargetData = DockerTargetData.fromExtraction(extraction);

        DetectToolProjectInfo projectInfo = null;
        if (StringUtils.isNotBlank(extraction.getProjectName()) || StringUtils.isNotBlank(extraction.getProjectVersion())) {
            NameVersion nameVersion = new NameVersion(extraction.getProjectName(), extraction.getProjectVersion());
            projectInfo = new DetectToolProjectInfo(detectTool, nameVersion);
        }

        logger.debug("Tool finished.");

        return DetectableToolResult.success(detectCodeLocations, projectInfo, dockerTargetData);
    }
}
