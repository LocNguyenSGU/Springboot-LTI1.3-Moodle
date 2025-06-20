//package com.example.demoLTI13.utils;
//
//import com.auth0.jwk.*;
//import io.jsonwebtoken.*;
//import org.json.JSONObject;
//
//import java.net.URL;
//import java.security.interfaces.RSAPublicKey;
//
//public class JwtUtil {
//    private static final String JWKS_URL = "http://localhost:8100/mod/lti/certs.php";
//
//    public static Claims validate(String idToken) throws Exception {
//        var header = new JSONObject(
//                new String(java.util.Base64.getUrlDecoder().decode(idToken.split("\\.")[0]))
//        );
//        String kid = header.getString("kid");
//
//        Jwk publicKeyJwk = new UrlJwkProvider(new URL(JWKS_URL)).get(kid);
//        RSAPublicKey publicKey = (RSAPublicKey) publicKeyJwk.getPublicKey();
//
//        return Jwts.parserBuilder()
//                .setSigningKey(publicKey)
//                .build()
//                .parseClaimsJws(idToken)
//                .getBody();
//    }
//}

package com.example.demoLTI13.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class JwtUtil {

    public static Claims validate(String idToken) throws Exception {
        RSAPublicKey publicKey = getHardcodedPublicKey();

        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(idToken)
                .getBody();
    }

    private static RSAPublicKey getHardcodedPublicKey() throws Exception {
        String n = "rgOLHuwSxH1ruC83HDrr4GmgvIn1G-RKPVhlXw-l_NJ3jNpkYua1lKQDY4dUNS0a6uvW-Fp-iFH3DWZgPUzwJL9lfxG4yOikLiqGBOv6TJHvoaQc8ggEPLucS-mtntGPmb6Yd6kz0qSf9YGjBpQ-HtiZe5ywrRH5Wye2bAsGFRqCuLAR9H6B-znnh8NcN_1V1H4qxbIhsHw0hcHSAD_szBjU5y58hhjKIvs61c8t0Ddnt4s8j2-p8QC5YpeGQQqCJdf7yp59-bhg7s76qc5p6cKbfAW1M8sUA2Ez9Qyiy7H7ILcwdnRK21WzP5WMPyl_MwJYyGn3w2Qooj9TC0-w9w";
        String e = "AQAB";

        BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(n));
        BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(e));

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}