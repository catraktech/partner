package com.catrak.exatip.partner.service.impl;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

public class Test {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        String apiKey = UUID.randomUUID().toString();
        System.out.println("apikey value is \t" + apiKey);
        Base64.Encoder enc = Base64.getEncoder();
        Base64.Decoder dec = Base64.getDecoder();

        // encode data using BASE64
        String encoded = enc.encodeToString(apiKey.getBytes());
        System.out.println("encoded value is \t" + encoded);

        // Decode data
        String decoded = new String(dec.decode(encoded));
        System.out.println("decoded value is \t" + decoded);

    }

}
