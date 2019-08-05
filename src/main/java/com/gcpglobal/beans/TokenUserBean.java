package com.gcpglobal.beans;

import java.sql.Timestamp;

public class TokenUserBean {
	private Boolean isValid;
	private Timestamp expirationdate;
	private String token;
	private Integer idUserToken;
	private Integer idUser;
	
	
	public Boolean getIsValid() {
		return isValid;
	}
	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
	}
	public Timestamp getExpirationdate() {
		return expirationdate;
	}
	public void setExpirationdate(Timestamp expirationdate) {
		this.expirationdate = expirationdate;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	
	public Integer getIdUser() {
		return idUser;
	}
	public void setIdUser(Integer idUser) {
		this.idUser = idUser;
	}
	public Integer getIdUserToken() {
		return idUserToken;
	}
	public void setIdUserToken(Integer idUserToken) {
		this.idUserToken = idUserToken;
	}
	

}
