package com.synopsys.integration.detectable.detectables.maven.parsing;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;

import com.synopsys.integration.bdio.graph.MutableMapDependencyGraph;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectables.maven.parsing.parse.PomDependenciesHandler;
import com.synopsys.integration.detectable.extraction.Extraction;

public class MavenParseExtractor {
    private final ExternalIdFactory externalIdFactory;
    private final SAXParser saxParser;

    public MavenParseExtractor(ExternalIdFactory externalIdFactory, SAXParser saxParser) {
        this.externalIdFactory = externalIdFactory;
        this.saxParser = saxParser;
    }

    public Extraction extract(File pomXmlFile, MavenParseOptions mavenParseOptions) {
        try (InputStream pomXmlInputStream = new FileInputStream(pomXmlFile)) {
            //we have to create a new handler or the state of all handlers would be shared.
            //we could create a handler factory or some other indirection so it could be injected but for now we make a new one.
            PomDependenciesHandler pomDependenciesHandler = new PomDependenciesHandler(externalIdFactory, mavenParseOptions.isIncludePlugins());
            saxParser.parse(pomXmlInputStream, pomDependenciesHandler);
            List<Dependency> dependencies = pomDependenciesHandler.getDependencies();

            MutableMapDependencyGraph dependencyGraph = new MutableMapDependencyGraph();
            dependencyGraph.addChildrenToRoot(dependencies);

            CodeLocation codeLocation = new CodeLocation(dependencyGraph);
            return new Extraction.Builder().success(codeLocation).build();
        } catch (Exception e) {
            return new Extraction.Builder().exception(e).build();
        }
    }
}
