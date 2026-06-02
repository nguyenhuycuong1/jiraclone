package com.jiraclone.service;

import com.jiraclone.entity.User;
import com.jiraclone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvatarService {

    private final R2StorageService storageService;
    private final UserRepository userRepository;

    private static final int TARGET_SIZE = 256;
    private static final long MAX_FILE_BYTES = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_MINE = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    public String uploadAvatar(UUID userId, MultipartFile file) throws IOException {

        validateFile(file);

        BufferedImage original = ImageIO.read(file.getInputStream());
        if (original == null) {
            throw new IllegalArgumentException("Invalid image file");
        }

        byte[] pngBytes = resizeTo256(original);

        String objectKey = "avatars/" + userId + "/" + UUID.randomUUID() + ".png";

        String newUrl = storageService.upload(objectKey, pngBytes, "image/png");

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        String oldUrl = user.getAvatarUrl();
        user.setAvatarUrl(newUrl);
        userRepository.save(user);

        return "";
    }

    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is null or empty");
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed size of 5MB");
        }
        String mine = file.getContentType();
        if (mine == null || !ALLOWED_MINE.contains(mine)) {
            throw new IllegalArgumentException("Unsupported file type: " + mine);
        }
    }

    public byte[] resizeTo256(BufferedImage src) throws IOException {
        int side = Math.min(src.getWidth(), src.getHeight());
        int x = (src.getWidth() - side) / 2;
        int y = (src.getHeight() - side) / 2;
        BufferedImage cropped = src.getSubimage(x, y, side, side);

        Image scaled = cropped.getScaledInstance(TARGET_SIZE, TARGET_SIZE, Image.SCALE_SMOOTH);
        BufferedImage output = new BufferedImage(TARGET_SIZE, TARGET_SIZE, BufferedImage.TYPE_INT_ARGB);
        output.getGraphics().drawImage(scaled, 0, 0, null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(output, "png", baos);
        return baos.toByteArray();
    }

    @Async
    protected void deleteOldAvatarAsync(String oldUrl) {
        if(oldUrl == null || oldUrl.isBlank()) return;
        String key = storageService.extractKey(oldUrl);
        storageService.delete(key);
    }
}
