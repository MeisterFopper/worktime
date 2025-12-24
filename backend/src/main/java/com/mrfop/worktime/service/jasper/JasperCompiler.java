package com.mrfop.worktime.service.jasper;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Build-time compiler: compiles *.jrxml from sourceDir to *.jasper under outputDir.
 *
 * Usage (via Maven exec plugin):
 *   args[0] = sourceDir (jrxml)   default: src/main/resources/jasper
 *   args[1] = outputDir (jasper)  default: target/classes/jasper
 */
public final class JasperCompiler {

    private JasperCompiler() {}

    public static void main(String[] args) throws Exception {
        Path sourceDir = Paths.get(args.length > 0 ? args[0] : "src/main/resources/jasper");
        Path outputDir = Paths.get(args.length > 1 ? args[1] : "target/classes/jasper");

        if (!Files.isDirectory(sourceDir)) {
            System.out.println("[JasperCompiler] No JRXML source directory: " + sourceDir.toAbsolutePath());
            return;
        }

        Files.createDirectories(outputDir);

        try (Stream<Path> stream = Files.walk(sourceDir)) {
            stream
                .filter(p -> p.toString().endsWith(".jrxml"))
                .sorted(Comparator.comparing(p -> sourceDir.relativize(p).toString()))
                .forEach(jrxml -> compileOne(sourceDir, outputDir, jrxml));
        }
    }

    private static void compileOne(Path sourceDir, Path outputDir, Path jrxml) {
        final String jrxmlPath = jrxml.toAbsolutePath().toString();

        try {
            // 1) Load (gives better XML/namespace/schema errors)
            try {
                validateXmlWellFormed(jrxml);
                JasperDesign design = JRXmlLoader.load(jrxmlPath);
                if (design == null) {
                    throw new IllegalStateException("JRXmlLoader returned null design");
                }
            } catch (Exception e) {
                System.err.println("[JasperCompiler] JRXML load failed (XML/schema): " + jrxmlPath);
                throw e;
            }
            System.out.println("[JasperCompiler] Well formed: " + jrxmlPath);

            // 2) Output path mirrors input structure
            Path relative = sourceDir.relativize(jrxml);
            Path outFile = outputDir.resolve(relative).normalize()
                .resolveSibling(relative.getFileName().toString().replace(".jrxml", ".jasper"));

            Files.createDirectories(outFile.getParent());

            // 3) Compile to .jasper
            JasperCompileManager.compileReportToFile(
                jrxmlPath,
                outFile.toAbsolutePath().toString()
            );

            System.out.println("[JasperCompiler] Compiled: " + relative + " -> " + outputDir.relativize(outFile));
        } catch (Exception e) {
            System.err.println("[JasperCompiler] JRXML compile failed: " + jrxmlPath);
            throw new RuntimeException("Failed to compile JRXML: " + jrxmlPath, e);
        }
    }

    private static void validateXmlWellFormed(Path file) throws Exception {
        var dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        // read as UTF-8
        String xml = Files.readString(file, StandardCharsets.UTF_8);
        
        var db = dbf.newDocumentBuilder();
        db.parse(new InputSource(new StringReader(xml)));
    }
}