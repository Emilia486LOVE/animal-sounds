package com.example.animalvoiceprint.service;

import com.example.animalvoiceprint.exception.BusinessException;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {
    
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    private static final long EXPIRATION_TIME = 5 * 60 * 1000;
    
    private static final String CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    
    private final ConcurrentHashMap<String, CaptchaData> captchaStore = new ConcurrentHashMap<>();
    private final Random random = new Random();
    
    public record CaptchaData(String code, long timestamp) {}
    
    public CaptchaResponse generateCaptcha() {
        String captchaId = UUID.randomUUID().toString();
        String code = generateCode();
        
        captchaStore.put(captchaId, new CaptchaData(code, System.currentTimeMillis()));
        
        String imageBase64 = generateImage(code);
        
        return new CaptchaResponse(captchaId, imageBase64);
    }
    
    public boolean validateCaptcha(String captchaId, String inputCode) {
        CaptchaData data = captchaStore.get(captchaId);
        
        if (data == null) {
            throw new BusinessException("验证码不存在或已过期");
        }
        
        if (System.currentTimeMillis() - data.timestamp() > EXPIRATION_TIME) {
            captchaStore.remove(captchaId);
            throw new BusinessException("验证码已过期");
        }
        
        if (!data.code().equalsIgnoreCase(inputCode)) {
            captchaStore.remove(captchaId);
            throw new BusinessException("验证码错误");
        }
        
        captchaStore.remove(captchaId);
        return true;
    }
    
    private String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHAR_SET.charAt(random.nextInt(CHAR_SET.length())));
        }
        return sb.toString();
    }
    
    private String generateImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        for (int i = 0; i < 20; i++) {
            g.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            g.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT),
                    random.nextInt(WIDTH), random.nextInt(HEIGHT));
        }
        
        for (int i = 0; i < 50; i++) {
            g.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            g.drawOval(random.nextInt(WIDTH), random.nextInt(HEIGHT), 2, 2);
        }
        
        Font[] fonts = {
            new Font("Arial", Font.BOLD, 24),
            new Font("Times New Roman", Font.BOLD, 24),
            new Font("Verdana", Font.BOLD, 24),
            new Font("Georgia", Font.BOLD, 24)
        };
        
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            g.setFont(fonts[random.nextInt(fonts.length)]);
            g.setColor(new Color(random.nextInt(80) + 100, random.nextInt(80) + 100, random.nextInt(80) + 100));
            
            int x = 15 + i * 25;
            int y = HEIGHT / 2 + 10;
            
            double rotate = (random.nextDouble() - 0.5) * 0.5;
            g.rotate(rotate, x, y);
            g.drawString(String.valueOf(c), x, y);
            g.rotate(-rotate, x, y);
        }
        
        g.dispose();
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }
    
    public void cleanupExpiredCaptchas() {
        long now = System.currentTimeMillis();
        captchaStore.entrySet().removeIf(entry -> now - entry.getValue().timestamp() > EXPIRATION_TIME);
    }
    
    public record CaptchaResponse(String captchaId, String imageBase64) {}
}