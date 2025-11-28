package com.crrc.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

public final class ImageUtils {
    private ImageUtils() {}
    /**
     * 通过图片uri下载文件
     * @param imageUri 通过图片uri下载文件
     * @return 文件的byte[]
     * @throws Exception
     */
    public static byte[] downloadImage(String imageUri) throws Exception {
        URL url = new URL(imageUri);
        try (InputStream in = url.openStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();
        }
    }
}
