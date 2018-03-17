package io.pay.coupon.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GenCouponId {
	private static int KAKAOPAY_COUPON_LEN = 16;
	private static final char[] s = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
			.toCharArray();

	public static String suffleString(String s) {
		String[] sarray = s.split("");
		List<String> letters = Arrays.asList(sarray);
		Collections.shuffle(letters);
		StringBuilder sb = new StringBuilder(s.length());
		for (String c : letters) {
			sb.append(c);
		}
		return sb.toString();
	}

	public static String encodeSh256(String magic) throws NoSuchAlgorithmException {
		StringBuffer encoded = new StringBuffer();
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] encodedByte = digest.digest(magic.getBytes());
		for (byte byt : encodedByte) {
			String hex = Integer.toHexString(0xff & byt);
			if (hex.length() == 1)
				encoded.append('0');
			encoded.append(hex);
		}
		return encoded.toString();
	}

	public static String generateIdByRand(String magic, int len) {
		char[] randomChars = new char[len];

		for (int i = 0; i < len; i++) {
			randomChars[i] = s[ThreadLocalRandom.current().nextInt(s.length)];
		}
		return new String(randomChars);
	}

	public static String generateIdBySha256(String magic, int len) {
		try {
			String encoded = encodeSh256(magic);
			char[] randomChars = new char[len];

			if (encoded.length() < len) {
				return "";
			}

			int keylen = encoded.length() / len;

			for (int i = 0; i < len; i++) {
				randomChars[i] = s[encoded.substring(i * keylen, i * keylen + keylen).hashCode()
						% s.length];
			}
			return new String(randomChars);
		} catch (Exception e) {
			return "";
		}
	}

	public static String generatePayCouponId(String email) {
		return generateIdBySha256(email, KAKAOPAY_COUPON_LEN);
	}
}
