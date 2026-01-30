package com.malcolm.ecomproj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private com.malcolm.ecomproj.repo.ProductRepo repo;

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM product");
            if (rs.next()) {
                int count = rs.getInt(1);
                if (count == 0) {
                    Resource resource = new ClassPathResource("data.sql");
                    if (resource.exists()) {
                        ScriptUtils.executeSqlScript(conn, resource);
                        if (!conn.getAutoCommit()) {
                            conn.commit();
                        }
                    }
                }
            }
        } catch (Exception e) {
        }

        loadImages();
    }

    private void loadImages() {
        String imageDirectoryPath = "D:\\Malcolm\\DSCE\\Internship\\SENSEI\\ecom image";

        try {
            List<com.malcolm.ecomproj.model.Product> products = repo.findAll();
            int updatedCount = 0;

            for (com.malcolm.ecomproj.model.Product product : products) {
                String imgName = product.getImageName();
                boolean derived = false;

                if (imgName == null || imgName.isEmpty()) {
                    String derivedName = product.getName().toLowerCase().replaceAll("[ -]", "_") + ".jpg";
                    java.io.File checkFile = new java.io.File(imageDirectoryPath, derivedName);

                    if (checkFile.exists()) {
                        imgName = derivedName;
                        product.setImageName(imgName);
                        derived = true;
                    }
                }

                if (imgName != null && !imgName.isEmpty()) {
                    java.io.File imageFile = new java.io.File(imageDirectoryPath, imgName);

                    if (imageFile.exists()) {
                        byte[] imageBytes = java.nio.file.Files.readAllBytes(imageFile.toPath());
                        product.setImageData(imageBytes);

                        if (product.getImageType() == null || product.getImageType().isEmpty()) {
                            String fileName = imageFile.getName();
                            String type = fileName.endsWith(".png") ? "image/png" : "image/jpeg";
                            product.setImageType(type);
                        }

                        repo.save(product);
                        updatedCount++;
                    }
                }
            }
        } catch (Exception e) {
        }
    }
}
