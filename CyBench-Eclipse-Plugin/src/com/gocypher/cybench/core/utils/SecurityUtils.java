package com.gocypher.cybench.core.utils;

import javax.crypto.*;
import java.io.*;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class SecurityUtils {
	private static final String CYB_PUBLIC_KEY_FILE = "cybench_key.pub";
	private static final String CYB_PRIVATE_KEY_FILE = "cybench_key.key";
	
	private static final String CYB_PUBLIC_KEY_RAW = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAw/23CncKAjXNKbj5unwTrxzr2R0AC3oIDlSjuKEEcEhylpXBsQGNEmRpDbB6krs9RC51f2yNO1Bk4ATcNZv+ac/qcTobF4YTWEhrE8CGSibktNQt6bNGj0eQfZ+Pd1rNynj1lALRIFnWUvc/BVRvMnRtlZCc3OiHZXlMBvE/rb+Qb2X6VxaT8cREjiKy5OXmaNCT9igzYjJG7g3e6KZT7V7McnrLxeYf8Ubw2NtTmmjePNdZQuKCAlGYg+Io30c7r8Q4i6dob3jfI3eNPQxs4BRKLddLvpDUm1ZJQKnUCcvh6LpJQLZZ2sfXW8ryFTAcDy4+NA5/F4QvVxsgEZ7m/u9l/iTr/JhaIzdpwYU9J94soRKtBZHZhtEvBuGHmGDLPxVMvAcLND/yiGeQyWnVCGxoFLkX4Pv7/67Y5LdUFwZZwXDjIsrS2943yEkmEud+04iuRfOn89F1mV4Pwuwm3OL/6oY2E/jN7/R0tseshTXEJJxOB8Wjydq5tAtbX+8Eee1ttciO62msDPp08UVmeJneR0m1uwQmyTlXAK97CiGvVmtOr64ICyG+SCEL2iR3oqB3Za5NuKrcAEU5xTKLeKb6tUILoMfFqdTVo/DUbXg9sVVKtQeYWPwqW9VmIqnQ0tYkTOfQpwh07OAMvW7wFZrodODuzhjCWoldJGSJDwECAwEAAQ==" ;

	private static final String BEGIN_PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----";
	private static final String END_PRIVATE_KEY = "-----END RSA PRIVATE KEY-----";

	private static final String BEGIN_PUBLIC_KEY = "-----BEGIN RSA PUBLIC KEY-----";
	private static final String END_PUBLIC_KEY = "-----END RSA PUBLIC KEY-----";

	public static String computeStringHash(String string) {
		if (string != null) {
			try {
				return hashByteArray(string.getBytes());
			} catch (Exception e) {
				//LOG.error("Failed to compute hash: arg={}", string, e);
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String computeClassHash(Object object) {
		if (object != null) {
			return computeClassHash(object.getClass());
		}
		return null;
	}

	public static String computeClassHash(Class<?> clazz) {
		if (clazz != null) {
			String name = clazz.getName();
			String fileName = "" + name.replaceAll("\\.", "/") + ".class";
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			//LOG.info("Computing hash for class {}, classloader {}", fileName, loader);
			try (InputStream in = loader.getResourceAsStream(fileName)) {
				byte[] classBytes = file2ByteArray(in);
				String classMD5Hash = hashByteArray(classBytes);
				//LOG.info("Computed hash {} for class {}, classloader {}", classMD5Hash, fileName, loader);
				return classMD5Hash;
			} catch (Exception e) {
				//LOG.error("Failed to compute hash for class {}", clazz, e);
				e.printStackTrace();
			}
		}
		return null;
	}

	private static byte[] file2ByteArray(InputStream inputStream) {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[1024];
			while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}

			buffer.flush();
			byte[] byteArray = buffer.toByteArray();
			return byteArray;
		} catch (Exception e) {
			//LOG.error("Failed to read input stream", e);
			e.printStackTrace();
		}
		return null;
	}

	private static String hashByteArray(byte[] classBytes) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.reset();
		byte[] digested = md.digest(classBytes);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < digested.length; i++) {
			sb.append(Integer.toHexString(0xff & digested[i]));
		}
		return sb.toString();
	}
	

	public static void generateAndStoreKeyPair(String fileName) {
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(4096);
			KeyPair kp = kpg.generateKeyPair();
			Key pub = kp.getPublic();
			Key pvt = kp.getPrivate();

			Base64.Encoder encoder = Base64.getEncoder();
			Writer out = new FileWriter(fileName + ".key");
			out.write(BEGIN_PRIVATE_KEY);
			out.write("\n");
			out.write(encoder.encodeToString(pvt.getEncoded()));
			out.write("\n");
			out.write(END_PRIVATE_KEY);
			out.write("\n");
			out.close();

			out = new FileWriter(fileName + ".pub");
			out.write(BEGIN_PUBLIC_KEY);
			out.write("\n");
			out.write(encoder.encodeToString(pub.getEncoded()));
			out.write("\n");
			out.write(END_PUBLIC_KEY);
			out.write("\n");
			out.close();
		} catch (Exception e) {
			//LOG.error("Failed to generate keypair: filename={}", fileName, e);
			e.printStackTrace();
		}
	}

	public static RSAPublicKey loadPublicKey() {
		try {
			//byte[] keyBytes = loadBase64EncodedKey(CYB_PUBLIC_KEY_FILE, true);
			byte[] keyBytes = decodeRawPublicKey () ;
			X509EncodedKeySpec ks = new X509EncodedKeySpec(keyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(ks);
			return pubKey;
		} catch (Exception e) {
			//LOG.error("Error on loading key: file={}", CYB_PUBLIC_KEY_FILE, e);
			e.printStackTrace();
		}
		return null;
	}

	public static PrivateKey loadPrivateKey() {
		try {
			byte[] keyBytes = loadBase64EncodedKey(CYB_PRIVATE_KEY_FILE, false);
			PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PrivateKey pvt = kf.generatePrivate(ks);
			return pvt;
		} catch (Exception e) {
			//LOG.error("Error on loading key: file={}", CYB_PRIVATE_KEY_FILE, e);
			e.printStackTrace();
		}
		return null;
	}

	private static byte[] loadBase64EncodedKey(String fileName, boolean isPublicKey) throws IOException {
		ClassLoader CLDR = SecurityUtils.class.getClassLoader();
		System.out.println("Loading key from file:"+fileName);
		InputStream in = CLDR.getResourceAsStream(fileName);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String header = reader.readLine();
		boolean valid = (header != null)
				&& (isPublicKey ? header.contains(BEGIN_PUBLIC_KEY) : header.contains(BEGIN_PRIVATE_KEY));
		if (!valid) {
			throw new IOException("Invalid key header=" + header);
		}
		String publicKeyPEM = reader.readLine();
		String footer = reader.readLine();
		valid = valid && (isPublicKey ? footer.contains(END_PUBLIC_KEY) : footer.contains(END_PRIVATE_KEY));
		if (!valid) {
			throw new IOException("Invalid key footer=" + footer);
		}		
		return Base64.getDecoder().decode(publicKeyPEM);
	}
	
	private static byte [] decodeRawPublicKey () {
		return Base64.getDecoder().decode(CYB_PUBLIC_KEY_RAW) ;
	}

	private static byte[] encryptRSA(PublicKey key, byte[] plaintext) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(plaintext);
	}

	public static String encryptReport(String report) {
		try {
			if (report != null && !report.isEmpty()) {
				RSAPublicKey key = loadPublicKey();
				SecretKey secKey = generateKey();
				byte[] encryptedReport = encryptAES(secKey, report);
				byte[] encryptedKey = encryptRSA(key, secKey.getEncoded());

				Base64.Encoder encoder = Base64.getEncoder();
				Map<String, String> map = new HashMap<>();
				map.put("encryptedKey", encoder.encodeToString(encryptedKey));
				map.put("encryptedReport", encoder.encodeToString(encryptedReport));
				return encoder.encodeToString(JSONUtils.marshalToJson(map).getBytes());
			}
		} catch (Exception e) {
			//LOG.error("Failed to encrypt report", e);
			e.printStackTrace();
		}
		return null;
	}

	private static byte[] encryptAES(SecretKey secKey, String text) throws Exception {
		Cipher aesCipher = Cipher.getInstance("AES");
		aesCipher.init(Cipher.ENCRYPT_MODE, secKey);
		byte[] byteCipherText = aesCipher.doFinal(text.getBytes("UTF-8"));
		return byteCipherText;
	}

	private static SecretKey generateKey() throws Exception {
		KeyGenerator generator = KeyGenerator.getInstance("AES");
		generator.init(128); // The AES key size in number of bits
		SecretKey secKey = generator.generateKey();
		return secKey;
	}
}