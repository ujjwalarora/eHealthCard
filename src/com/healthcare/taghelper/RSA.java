package com.healthcare.taghelper;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.StringTokenizer;

import javax.crypto.Cipher;

/*
 * It takes key in the form of 'modulus|exponent'
 */
public class RSA {
	
	BigInteger modulus;
	BigInteger exponent;
	
	public RSA(final String key) {
		StringTokenizer strtok = new StringTokenizer(key, "|");
		modulus = new BigInteger(strtok.nextToken());
		exponent = new BigInteger(strtok.nextToken());
	}
	
	public byte[] encrypt(final byte[] plainText) throws Exception {
		KeyFactory fact = KeyFactory.getInstance("RSA");
	    RSAPublicKeySpec pubSpec = new RSAPublicKeySpec(modulus, exponent);
	    PublicKey pubKey = fact.generatePublic(pubSpec);
	    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
	    cipher.init(Cipher.ENCRYPT_MODE, pubKey);
	    return cipher.doFinal(plainText);
	}
	
	public byte[] decrypt(final byte[] cipherText) throws Exception {
		KeyFactory fact = KeyFactory.getInstance("RSA");
	    RSAPrivateKeySpec privSpec = new RSAPrivateKeySpec(modulus, exponent);
	    PrivateKey privKey = fact.generatePrivate(privSpec);
	    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
	    cipher.init(Cipher.DECRYPT_MODE, privKey);
	    return cipher.doFinal(cipherText);
	}
}