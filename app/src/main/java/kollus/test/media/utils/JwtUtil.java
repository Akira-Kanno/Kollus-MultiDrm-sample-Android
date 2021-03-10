package kollus.test.media.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import kollus.test.media.Config;

public class JwtUtil {
    private final String TAG = JwtUtil.class.getSimpleName();

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public String createJwt(final String headerJson, final String payloadJson, String secretKey)
            throws NoSuchAlgorithmException, InvalidKeyException {
        String header = Base64.encodeToString(headerJson.getBytes(StandardCharsets.UTF_8), Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
        String payload = Base64.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8), Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
        String content = String.format("%s.%s", header, payload);
        final Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signatureBytes = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.encodeToString(signatureBytes, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
        LogUtil.d(TAG, String.format("%s.%s", content, signature));
        return String.format("%s.%s", content, signature);
    }

    public String createJwt(final String payloadJson, String secretKey)
            throws InvalidKeyException, NoSuchAlgorithmException {
        String headerJson = "{\"alg\": \"HS256\",\"typ\": \"JWT\"}";
        return createJwt(headerJson, payloadJson, secretKey);
    }


    public String[] splitJwt(String jwt) throws Exception {
        String[] parts = jwt.split("\\.");
        if (parts.length == 2 && jwt.endsWith(".")) {
            parts = new String[]{parts[0], parts[1], ""};
        }
        if (parts.length != 3) {
            throw new Exception(String.format("The token was expected to have 3 parts, but got %s.", parts.length));
        }
        return parts;
    }

    public String[] decodeJwt(String jwt) throws Exception {

        String[] parts = splitJwt(jwt);
        String headerJson = new String(Base64.decode(parts[0], Base64.URL_SAFE));
        String payloadJson = new String(Base64.decode(parts[0], Base64.URL_SAFE));
        String signature = parts[2];
        return new String[]{headerJson, payloadJson, signature};
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public boolean verify(String secretKey, String jwt) throws Exception {
        String[] parts = splitJwt(jwt);
        byte[] contentBytes = String.format("%s.%s", parts[0], parts[1]).getBytes(StandardCharsets.UTF_8);
        byte[] signatureBytes = Base64.decode(parts[2], Base64.URL_SAFE);

        final Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] newSignatureBytes = mac.doFinal(contentBytes);
        return MessageDigest.isEqual(newSignatureBytes, signatureBytes);

    }

    public static String AES_Encode(String str, String key, String iv) throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] textBytes = str.getBytes("UTF-8");
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
        SecretKeySpec newKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);

        return Base64.encodeToString(cipher.doFinal(textBytes), Base64.NO_WRAP);
    }

    public static String AES_Decode(String str, String key, String iv) throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] textBytes = Base64.decode(str, 0);
        //byte[] textBytes = str.getBytes("UTF-8");
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
        SecretKeySpec newKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        return new String(cipher.doFinal(textBytes), "UTF-8");
    }

    public static String baseEncodeToString(String str) {
        return Base64.encodeToString(str.getBytes(), Base64.NO_WRAP);
    }

    public static String baseEncodeToString(byte[] str) {
        return Base64.encodeToString(str, Base64.NO_WRAP);
    }

    public static String createHashString(String str) {
        String sha = "";
        try {
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(str.getBytes());
            byte byteData[] = sh.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            sha = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            sha = null;
        }
        return sha;
    }

    public static byte[] createHash(String str) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(str.getBytes());

        return md.digest();
    }
}
