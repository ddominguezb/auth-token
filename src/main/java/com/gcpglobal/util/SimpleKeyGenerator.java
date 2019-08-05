package com.gcpglobal.util;

import java.security.Key;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Resource;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.HttpHeaders;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service(value="keyGenerator")
public class SimpleKeyGenerator implements KeyGenerator {
	
	private static final Logger LOG = Logger.getLogger(SimpleKeyGenerator.class);

	// ======================================
	// = Business methods =
	// ======================================
	@Resource(name = "keyString")
	private String keyString;
	
	@Resource(name = "expirationTime")
	private Integer expirationTime;

	@Override
	public Key generateKey() {
		Key key = new SecretKeySpec(keyString.getBytes(), 0, keyString.getBytes().length, "DES");
		return key;
	}
	
	
	
	
	public String issueToken(String login, String path) {
		Key key = generateKey();
        Calendar cal = Calendar.getInstance();
        Date d=new Date();
        cal.setTime(d);
        
        if(expirationTime!=null){
        	 cal.add(Calendar.MINUTE, expirationTime);
        }else{
        	 cal.add(Calendar.MINUTE, 60);
        }
        
       
        String jwtToken = Jwts.builder()
                .setSubject(login)
                .setIssuer(path)
                .setIssuedAt(new Date())
                .setExpiration(cal.getTime())
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
        LOG.info("#### generating token for a key : " + jwtToken + " - " + key);
        return jwtToken;

    }
	
	public Claims parseJWT(Key key, String jwt) {
	    //This line will throw an exception if it is not a signed JWS (as expected)
	    Claims claims = Jwts.parser()         
	       .setSigningKey(key)
	       .parseClaimsJws(jwt).getBody();
	    System.out.println("ID: " + claims.getId());
	    System.out.println("Subject: " + claims.getSubject());
	    System.out.println("Issuer: " + claims.getIssuer());
	    System.out.println("Expiration: " + claims.getExpiration());
	    return claims;
	}
	
	public Claims parseJWT(HttpHeaders headers) {
		Key key = generateKey();
		 String token=null;
		if(headers.getRequestHeader("Authorization").size()>0) {
			token = headers.getRequestHeader("Authorization").get(0);
			token = token.substring("Bearer".length()).trim();
		}
	    Claims claims = Jwts.parser()         
	       .setSigningKey(key)
	       .parseClaimsJws(token).getBody();
	    return claims;
	}

}