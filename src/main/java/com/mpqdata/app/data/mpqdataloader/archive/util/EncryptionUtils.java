package com.mpqdata.app.data.mpqdataloader.archive.util;

import org.apache.commons.codec.digest.DigestUtils;

public class EncryptionUtils {

	public static final int ENCRYPTION_HASH_MAIN = 0x54007B47;
	public static final int ENCRYPTION_HASH_SCRIPTS = 0xB29F8D49;

	public static int hash(String lpString, int dwHash) {
		int dwLength = lpString.length();
		for (int i = 0; i < dwLength; i++)
		{
			dwHash = lpString.charAt(i) + 33 * dwHash;
		}
		return dwHash;
	}

	public static void decrypt(byte[] lpBuffer, int dwSeed, int dwSize) {
		int j = 0;
		var bTemp = 0;

		for (int i = 0; i < dwSize; i++)
		{
			bTemp = (byte)(j & 0x18);
			j += 8;
			lpBuffer[i] ^= (byte)((dwSeed >> bTemp) + (101 * (i >> 2)));
		}
	}

	public static String md5CheckSum(byte[] bytes) {
		return DigestUtils.md5Hex(bytes).toUpperCase();
	}

}
