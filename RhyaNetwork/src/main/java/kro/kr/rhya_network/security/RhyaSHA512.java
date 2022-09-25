package kro.kr.rhya_network.security;

import java.math.BigInteger;
import java.security.MessageDigest;

public class RhyaSHA512 {
    public String getSHA512(String input) {
		String toReturn = null;
		try {
		    MessageDigest digest = MessageDigest.getInstance("SHA-512");
		    digest.reset();
		    digest.update(input.getBytes("utf8"));
		    toReturn = String.format("%0128x", new BigInteger(1, digest.digest()));
		} catch (Exception e) {
		    e.printStackTrace();
		}
	
		return toReturn;
    }
}
