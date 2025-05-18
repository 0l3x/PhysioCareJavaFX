package olex.physiocareapifx.utils.pdf;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import olex.physiocareapifx.model.Records.Record;
import olex.physiocareapifx.services.RecordService;

import java.io.File;
import java.io.FileNotFoundException;

public class PdfUtils {
    private static final Paragraph header = new Paragraph("COHMPANY Clinic S.A. - S/ McDonalds, Tenesse")
            .setFontSize(16)
            .setItalic()
            .setTextAlignment(TextAlignment.RIGHT)
            .setMarginBottom(20);

    public static void main(String[] args) {
        RecordService.getRecordById("67f3fe3996b49b1892b182f0")
                .thenAccept(record ->{
                    medicalRecordPdfCreator(record.getRecord());
                });
    }

    public static void medicalRecordPdfCreator(Record record){
        String dest = "/output/records/" + record.getPatient().getInsuranceNumber() + ".pdf";
        Document document;
        try{
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            document = new Document(pdf);
            // Header
            document.add(header);
            // Title
            Paragraph title = new Paragraph("Medical Record")
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);
            // Tabla with patient info
            float[] colWidths = {2, 4};
            Table recordInfo = new Table(UnitValue.createPercentArray(colWidths));
            recordInfo.setWidth(UnitValue.createPercentValue(100));
            recordInfo.addCell(getHeaderCell("Record ID"));
            recordInfo.addCell(String.valueOf(record.getId()));
            recordInfo.addCell(getHeaderCell("Patient's Insurance Number"));
            recordInfo.addCell(String.valueOf(record.getPatient().getInsuranceNumber()));
            recordInfo.addCell(getHeaderCell("Patient Name"));
            recordInfo.addCell(record.getPatient().getFullName());
            recordInfo.addCell(getHeaderCell("Email"));
            recordInfo.addCell(record.getPatient().getEmail());
            document.add(recordInfo);

            // Space
            document.add(new Paragraph("\n"));
            // Medical record
            Paragraph medTitle = new Paragraph("Description:")
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(10);
            document.add(medTitle);

            Paragraph medRecord = new Paragraph(record.getMedicalRecord())
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.JUSTIFIED);

            document.add(medRecord);

            document.close();
            System.out.println("Medical Record PDF created successfully.");

            File pdfFile = new File(dest);
//            if (pdfFile.exists()) {
//                Sftp.savePDF(pdfFile.getAbsolutePath(), pdfFile.getName());
//            }

        }catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }
    }

        private static Cell getHeaderCell(String text) {
        return new Cell().add(new Paragraph(text))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBold()
                .setBorder(new SolidBorder(ColorConstants.GRAY, 1))
                .setTextAlignment(TextAlignment.LEFT);
    }
}