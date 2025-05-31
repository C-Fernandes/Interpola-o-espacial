package com.imd;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        LeitorCSV leitor = new LeitorCSV();
        try {
            URL resource = Main.class.getClassLoader().getResource("dataset.csv");
            if (resource == null) {
                System.err.println("Arquivo CSV não encontrado em src/main/resources");
                return;
            }

            Path path = Paths.get(resource.toURI());
            leitor.lerEInterpolar(path.toString());
            System.out.println("Acabou");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
