package com.mrfop.worktime.service.jasper;

import com.mrfop.worktime.service.jasper.model.WorkReportPdf;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader; // <-- FIX
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JasperRenderer {

    private final ResourceLoader resourceLoader;

    public byte[] renderPdf(WorkReportPdf model) {
        try (
            InputStream inMain = resourceLoader.getResource("classpath:jasper/work-report.jasper").getInputStream();
            InputStream inDay = resourceLoader.getResource("classpath:jasper/subreports/day.jasper").getInputStream();
            InputStream inSession = resourceLoader.getResource("classpath:jasper/subreports/session.jasper").getInputStream();
            InputStream inSegment = resourceLoader.getResource("classpath:jasper/subreports/segment.jasper").getInputStream()
        ) {
            // Main Report
            JasperReport reportMain = (JasperReport) JRLoader.loadObject(inMain);
            // Subreports
            JasperReport reportDay = (JasperReport) JRLoader.loadObject(inDay);
            JasperReport reportSession = (JasperReport) JRLoader.loadObject(inSession);
            JasperReport reportSegment = (JasperReport) JRLoader.loadObject(inSegment);

            Map<String, Object> params = new HashMap<>();
            params.put("SR_DAY", reportDay);
            params.put("SR_SESSION", reportSession);
            params.put("SR_SEGMENT", reportSegment);

            JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(Collections.singletonList(model));

            JasperPrint print = JasperFillManager.fillReport(reportMain, params, ds);
            return JasperExportManager.exportReportToPdf(print);

        } catch (Exception e) {
            throw new RuntimeException("Failed to render PDF", e);
        }
    }
}