package io.sauce.tradingbot.compoent;



import io.micrometer.core.instrument.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHAUtils {

    public static String encrypt(String... args) {

        MessageDigest messageDigest;
        String encodeStr;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            for (String arg : args) {
                if (StringUtils.isNotBlank(arg)) {
                    messageDigest.update(arg.getBytes(StandardCharsets.UTF_8));
                }
            }
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        }

        return encodeStr;
    }

    public static String byte2Hex(byte[] bytes) {
        StringBuilder stringBuffer = new StringBuilder();
        String temp;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }
}
