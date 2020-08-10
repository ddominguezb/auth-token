package com.gcpglobal.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.jboss.jca.adapters.jdbc.jdk6.WrappedConnectionJDK6;

import com.gcpglobal.beans.TokenUserBean;

public class UserTokenDAO {

	private static final Logger LOG = Logger.getLogger(UserTokenDAO.class);
	private static final String DATA_SOURCE_CONTEXT = "java:/jdbc/orcaDB";

	public TokenUserBean obtenerTokenUserJNDI(int idUser) throws NamingException, SQLException {
		// Declare the JDBC objects.
		Statement stmt = null;
		ResultSet rs = null;
		Connection con = null;
		TokenUserBean bean = null;
		try {
			Context initialContext = new InitialContext();
			DataSource datasource = (DataSource) initialContext.lookup(DATA_SOURCE_CONTEXT);

			if (datasource != null) {
				con = (WrappedConnectionJDK6) datasource.getConnection();

				String SQL = "select " + " expirationdate, lastupdate, token from ADVISORUSERTOKEN " + " where iduser = " + idUser
						+ " and CAST(EXPIRATIONDATE AS DATETIME) > CAST(GETDATE() AS DATETIME) ";
				stmt = con.createStatement();
				rs = stmt.executeQuery(SQL);
				// Iterate through the data in the result set and display it.
				while (rs.next()) {
					bean = new TokenUserBean();
					bean.setExpirationdate(rs.getTimestamp("expirationdate"));
					bean.setLastUpdate(rs.getTimestamp("lastupdate"));
					bean.setIdUser(idUser);
					bean.setToken(rs.getString("token"));
					break;
				}

			} else {
				LOG.error("Failed to lookup datasource.");
			}
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
					throw e;
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw e;
				}
			if (con != null)
				try {
					con.close();
				} catch (Exception e) {
					throw e;
				}
		}
		return bean;
	}

	public boolean actualizarCaducidadDeTokenUserJNDI(int idUser, int minutesTimeout)
			throws NamingException, SQLException {
		Boolean resultado = false;
		// JNDI de la conexión a base de datos
		// Declare the JDBC objects.
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			Context initialContext = new InitialContext();
			DataSource datasource = (DataSource) initialContext.lookup(DATA_SOURCE_CONTEXT);
			if (datasource != null) {
				con = (WrappedConnectionJDK6) datasource.getConnection();
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.MINUTE, minutesTimeout);
				java.sql.Timestamp timestamp = new java.sql.Timestamp(cal.getTimeInMillis());
				String SQL = "update  ADVISORUSERTOKEN set  expirationdate = '" + timestamp + "'  where iduser = "
						+ idUser;
				stmt = con.createStatement();
				stmt.executeUpdate(SQL);
				resultado = true;
			} else {
				LOG.error("Failed to lookup datasource.");
			}
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
			if (con != null)
				try {
					con.close();
				} catch (Exception e) {
				}
		}
		return resultado;
	}

	public boolean crearTokenUserJNDI(int idUser, String token, int minutesTimeout)
			throws NamingException, SQLException {

		Boolean resultado = false;
		boolean isUpdate = existUser(idUser);

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, minutesTimeout);
		java.sql.Timestamp timestamp = new java.sql.Timestamp(cal.getTimeInMillis());

		String SQL = "INSERT INTO ADVISORUSERTOKEN( IDUSER, CREATIONDATE, EXPIRATIONDATE, TOKEN) " + "VALUES(" + idUser
				+ ", getdate(), '" + timestamp + "', '" + token + "')";

		if (isUpdate) {
			SQL = "update  ADVISORUSERTOKEN set " + "expirationdate = '" + timestamp + "', token = '" + token
					+ "'  where iduser= " + idUser;
		}
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			Context initialContext = new InitialContext();
			DataSource datasource = (DataSource) initialContext.lookup(DATA_SOURCE_CONTEXT);
			if (datasource != null) {
				con = (WrappedConnectionJDK6) datasource.getConnection();
				stmt = con.createStatement();
				return stmt.execute(SQL);
			} else {
				LOG.error("Failed to lookup datasource.");
			}
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
			if (con != null)
				try {
					con.close();
				} catch (Exception e) {
				}
		}
		return resultado;
	}

	public boolean existUser(Integer idUser) throws NamingException, SQLException {
		boolean exist = false;

		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			Context initialContext = new InitialContext();
			DataSource datasource = (DataSource) initialContext.lookup(DATA_SOURCE_CONTEXT);
			if (datasource != null) {
				con = (WrappedConnectionJDK6) datasource.getConnection();
				String SQL = " select COUNT(*) as rows from ADVISORUSERTOKEN where iduser=" + idUser;
				stmt = con.createStatement();
				rs = stmt.executeQuery(SQL);

				while (rs.next()) {
					int count = rs.getInt("rows");
					if (count > 0) {
						exist = true;
					}
					break;
				}

			} else {
				LOG.error("Failed to lookup datasource.");
			}
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
			if (con != null)
				try {
					con.close();
				} catch (Exception e) {
				}
		}

		return exist;
	}

	public boolean actualizarTokenUserJNDI(int idUser, String token, int minutesTimeout)
			throws NamingException, SQLException {
		Boolean resultado = false;

		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			Context initialContext = new InitialContext();
			DataSource datasource = (DataSource) initialContext.lookup(DATA_SOURCE_CONTEXT);
			if (datasource != null) {
				con = (WrappedConnectionJDK6) datasource.getConnection();
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.MINUTE, minutesTimeout);
				java.sql.Timestamp timestamp = new java.sql.Timestamp(cal.getTimeInMillis());
				String SQL = "update  ADVISORUSERTOKEN set " + "expirationdate = '" + timestamp + "', token = '" + token
						+ "'  where iduser= " + idUser;
				stmt = con.createStatement();
				stmt.executeUpdate(SQL);
				resultado = true;
			} else {
				LOG.error("Failed to lookup datasource.");
			}
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
			if (con != null)
				try {
					con.close();
				} catch (Exception e) {
				}
		}
		return resultado;
	}

	public boolean isTokenValid(String token, Integer idUser) throws NamingException, SQLException {
		boolean isValid = false;
	
		TokenUserBean userToken = this.obtenerTokenUserJNDI(idUser);
		if (userToken != null) {
			boolean isNotExpired = ((Timestamp) userToken.getExpirationdate()).getTime() > System.currentTimeMillis();
			isValid = token != null && token.equals(userToken.getToken()) && isNotExpired;
		}
	
		return isValid;
	}

}