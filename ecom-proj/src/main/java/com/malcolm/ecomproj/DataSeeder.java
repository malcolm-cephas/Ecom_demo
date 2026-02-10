package com.malcolm.ecomproj;

import com.malcolm.ecomproj.model.Product;
import com.malcolm.ecomproj.repo.ProductRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * Component that runs automatically on startup to populate the database with
 * initial data
 * and load product images from the local file system.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final DataSource dataSource;
    private final ProductRepo repo;

    @Override
    public void run(String... args) {
        // Step 1: Execute SQL seeds
        seedDatabase();
        // Step 2: Sync local image files to the database
        loadImages();
    }

    /**
     * Checks if the database is empty and executes data.sql if it is.
     */
    private void seedDatabase() {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {

            // Check if products already exist to avoid duplicate seeding
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM product")) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    if (count == 0) {
                        // Load the initial SQL script from resources
                        Resource resource = new ClassPathResource("data.sql");
                        if (resource.exists()) {
                            ScriptUtils.executeSqlScript(conn, resource);

                            if (!conn.getAutoCommit()) {
                                conn.commit();
                            }
                            log.info("Database seeded successfully from data.sql");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error seeding database: {}", e.getMessage(), e);
        }
    }

    /**
     * Reads images from the 'ecom image' folder and stores them as BLOBs in the
     * database.
     */
    private void loadImages() {
        // Path to the external directory containing product images
        String imageDirectoryPath = "D:\\Malcolm\\DSCE\\Internship\\SENSEI\\ecom image";

        try {
            List<Product> products = repo.findAll();
            int updatedCount = 0;

            for (Product product : products) {
                String imgName = product.getImageName();

                // If the product has no image name assigned, try to guess it from the product
                // name
                if (imgName == null || imgName.isEmpty()) {
                    String derivedName = product.getName().toLowerCase().replaceAll("[ -]", "_") + ".jpg";
                    File checkFile = new File(imageDirectoryPath, derivedName);

                    if (checkFile.exists()) {
                        imgName = derivedName;
                        product.setImageName(imgName);
                    }
                }

                // If we have a valid image name, load the file bytes into the database
                if (imgName != null && !imgName.isEmpty()) {
                    File imageFile = new File(imageDirectoryPath, imgName);

                    if (imageFile.exists()) {
                        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                        product.setImageData(imageBytes);

                        // Detect MIME type
                        if (product.getImageType() == null || product.getImageType().isEmpty()) {
                            String fileName = imageFile.getName();
                            String type = fileName.endsWith(".png") ? "image/png" : "image/jpeg";
                            product.setImageType(type);
                        }

                        // Save the product with the newly attached image data
                        repo.save(product);
                        updatedCount++;
                    }
                }
            }
            if (updatedCount > 0) {
                log.info("Loaded images for {} products", updatedCount);
            }
        } catch (Exception e) {
            log.error("Error loading images: {}", e.getMessage(), e);
        }
    }
}
