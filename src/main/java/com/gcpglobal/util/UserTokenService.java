package com.gcpglobal.util;

import java.sql.SQLException;

import javax.naming.NamingException;

public interface UserTokenService {
    String generarTokenByUser(Integer userID)  throws NamingException, SQLException;
}
