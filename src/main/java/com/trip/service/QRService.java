package com.trip.service;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class QRService {

    public String generateQRCodeBase64(String text) {
        try {
            BitMatrix matrix = new MultiFormatWriter()
                    .encode(text, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", stream);

            return Base64.getEncoder().encodeToString(stream.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("QR generation failed");
        }
    }
    
    public byte[] generateQRCodeBytes(String text) {
        try {
            BitMatrix matrix = new MultiFormatWriter()
                    .encode(text, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", stream);

            return stream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("QR generation failed");
        }
    }
}