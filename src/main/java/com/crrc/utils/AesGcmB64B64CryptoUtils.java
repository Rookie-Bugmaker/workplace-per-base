package com.crrc.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public final class AesGcmB64B64CryptoUtils {

    // GCM 推荐 12 字节 IV
    public static final int IV_LEN = 12;
    // 128-bit Tag
    public static final int TAG_LEN_BIT = 128;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private AesGcmB64B64CryptoUtils() {}

    /** 从 Base64 的 key 生成 SecretKey（key 原始长度需为 16/24/32 字节） */
    public static SecretKey keyFromBase64(String aesKeyBase64) {
        byte[] keyBytes = Base64.getDecoder().decode(aesKeyBase64);
        if (!(keyBytes.length == 16 || keyBytes.length == 24 || keyBytes.length == 32)) {
            throw new IllegalArgumentException("AES key length must be 16/24/32 bytes, but got " + keyBytes.length);
        }
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * 加密：json -> innerBase64 -> AES-GCM(IV前置) -> outerBase64
     * @param json 明文 JSON 字符串
     * @param key AES key
     * @param aad 可选 AAD（不需要可传 null）
     */
    public static String encryptJsonToOuterBase64(String json, SecretKey key, byte[] aad) {
        try {
            // 内层 Base64：jsonBytes -> innerBase64String
            byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
            String innerB64 = Base64.getEncoder().encodeToString(jsonBytes);
            byte[] plain = innerB64.getBytes(StandardCharsets.UTF_8);

            // 生成 IV
            byte[] iv = new byte[IV_LEN];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LEN_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            if (aad != null && aad.length > 0) {
                cipher.updateAAD(aad);
            }

            // doFinal 输出 = ciphertext || tag（Java 自动拼接 tag）
            byte[] cipherTextWithTag = cipher.doFinal(plain);

            // 输出格式：IV(前置) || cipherTextWithTag
            byte[] out = new byte[iv.length + cipherTextWithTag.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(cipherTextWithTag, 0, out, iv.length, cipherTextWithTag.length);

            // 外层 Base64
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalArgumentException("Encrypt failed", e);
        }
    }

    /**
     * 解密：outerBase64 -> AES-GCM(IV前置) -> innerBase64 -> json
     * @param outerBase64 请求体（外层 Base64 字符串）
     * @param key AES key
     * @param aad 可选 AAD（必须与加密端一致）
     */
    public static String decryptOuterBase64ToJson(String outerBase64, SecretKey key, byte[] aad) {
        try {
            String trimmed = outerBase64 == null ? "" : outerBase64.trim();
            if (trimmed.isEmpty()) return "";

            // 有些客户端可能把 body 当作 JSON 字符串发来： "xxxxx"
            if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                trimmed = trimmed.substring(1, trimmed.length() - 1);
            }

            byte[] in = Base64.getDecoder().decode(trimmed);
            // 至少要有 IV + TAG（tag 16 bytes），ciphertext 可为 0，但通常不会
            if (in.length < IV_LEN + 16) {
                throw new IllegalArgumentException("Cipher payload too short: " + in.length);
            }

            byte[] iv = Arrays.copyOfRange(in, 0, IV_LEN);
            byte[] cipherTextWithTag = Arrays.copyOfRange(in, IV_LEN, in.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LEN_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            if (aad != null && aad.length > 0) {
                cipher.updateAAD(aad);
            }

            byte[] plain = cipher.doFinal(cipherTextWithTag);

            // plain 是 innerBase64String 的 UTF-8 bytes
            String innerB64 = new String(plain, StandardCharsets.UTF_8);

            // 内层 Base64 decode -> jsonBytes
            byte[] jsonBytes = Base64.getDecoder().decode(innerB64);
            return new String(jsonBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 包括 AEADBadTagException：tag 校验失败（密钥/IV/AAD/密文被篡改）
            throw new IllegalArgumentException("Decrypt failed", e);
        }
    }
}