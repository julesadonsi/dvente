package com.usetech.dvente.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.path:uploads}")
    private String uploadBasePath;

    /**
     * Sauvegarde un fichier avatar et retourne son URL.
     */
    public String saveAvatar(MultipartFile file) {
        return saveFile(file, "avatars");
    }

    /**
     * Supprime un fichier avatar.
     */
    public void deleteAvatar(String avatarUrl) {
        deleteFile(avatarUrl);
    }

    /**
     * Sauvegarde un document (PDF, DOC, DOCX) et retourne son URL.
     *
     * @param file le fichier à sauvegarder
     * @param documentType le type de document (ex: "ifu", "rcm")
     * @return l'URL du fichier sauvegardé
     */
    public String saveDocument(MultipartFile file, String documentType) {
        return saveFile(file, "documents/" + documentType);
    }

    /**
     * Supprime un document.
     */
    public void deleteDocument(String documentUrl) {
        deleteFile(documentUrl);
    }

    /**
     * Sauvegarde un fichier dans un sous-dossier spécifique.
     */
    private String saveFile(MultipartFile file, String subFolder) {
        try {
            String fileDir = uploadBasePath + "/" + subFolder;
            Path uploadPath = Paths.get(fileDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String filename = UUID.randomUUID().toString() + extension;

            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + subFolder + "/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Échec de la sauvegarde du fichier: " + e.getMessage());
        }
    }

    /**
     * Supprime un fichier du disque.
     */
    private void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            String relativePath = fileUrl.startsWith("/uploads/")
                    ? fileUrl.substring("/uploads/".length())
                    : fileUrl;

            Path filePath = Paths.get(uploadBasePath, relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                System.out.println("Fichier supprimé : " + filePath);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la suppression du fichier : " + e.getMessage());
        }
    }

    /**
     * Vérifie si un fichier existe.
     */
    public boolean fileExists(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }

        String relativePath = fileUrl.startsWith("/uploads/")
                ? fileUrl.substring("/uploads/".length())
                : fileUrl;

        Path filePath = Paths.get(uploadBasePath, relativePath);
        return Files.exists(filePath);
    }
}