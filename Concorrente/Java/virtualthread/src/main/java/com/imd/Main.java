package com.imd;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.net.URL;

public class Main {
    public static void main(String[] args) {
        LeitorCSV leitor = new LeitorCSV();
        try {
            URL resource = Main.class.getClassLoader().getResource("dataset.csv");
            if (resource == null) {
                System.err.println("Arquivo CSV não encontrado em src/main/resources");
                return;
            }

            Path tempFile = Files.createTempFile("dataset", ".csv");
            try (InputStream is = resource.openStream()) {
                Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
                leitor.lerEInterpolar(tempFile.toString());
                System.out.println("Acabou");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}