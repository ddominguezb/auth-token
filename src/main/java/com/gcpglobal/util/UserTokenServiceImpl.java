package com.gcpglobal.util;

import java.sql.SQLException;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.gcpglobal.beans.TokenUserBean;

@Service(value="UserTokenService")
public class UserTokenServiceImpl implements UserTokenService {
	
	private static final Logger LOG = Logger.getLogger(UserTokenServiceImpl.class);

	// ======================================
	// = Business methods =
	// ======================================
	public String generateString63() {
		return new RamdomString(63).nextString();
	}

	@Override
	public String generarTokenByUser(Integer userID)  {
		UserTokenDAO userDAO = new UserTokenDAO();
		String token = null;
		//Se obtiene el token del usuario
		TokenUserBean userToken;
		Integer timeOut  = (Integer) SpringApplicationContext.getBean("expirationTime");
		try {
			userToken = userDAO.obtenerTokenUserJNDI(userID);
			if(userToken == null ){
				token = generateString63();
				LOG.info("Token generado:" + token);
				LOG.info("Usuario Sin TOKEN");
				//Se registra el TOKEN para el usuario
				userDAO.crearTokenUserJNDI(userID, token,timeOut);
			}else{
				token= userToken.getToken();
			}
		} catch (NamingException e) {
			LOG.error(e);
		} catch (SQLException e) {
			LOG.error(e);
		}
		
		return token;
	}
	
	
	
	
}