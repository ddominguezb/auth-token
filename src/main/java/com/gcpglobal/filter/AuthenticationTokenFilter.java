package com.gcpglobal.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.NamingException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.gcpglobal.beans.TokenUserBean;
import com.gcpglobal.util.SpringApplicationContext;
import com.gcpglobal.util.UserTokenDAO;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@AuthenticationToken
@Provider
public class AuthenticationTokenFilter implements ContainerRequestFilter {

	private static final Logger LOG = Logger.getLogger(AuthenticationTokenFilter.class);

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		// UserManagerService syncService = (UserManagerService)
		// SpringApplicationContext.getBean("userManagerService");
		Byte tokenEnabled = (Byte) SpringApplicationContext.getBean("tokenEnabled");
		Integer timeOut  = (Integer) SpringApplicationContext.getBean("expirationTime");
		UserTokenDAO tokenDAO = new UserTokenDAO();
		LOG.info(" *****  @AuthenticationToken");

		if (tokenEnabled != null && tokenEnabled.byteValue() == 1) {
			// Se Obtiene el encabezado de la peticion
			String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
			// Checar si el encabezado Authorization se encuentra y si inicia correctamente
			if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
				LOG.fatal("NO existe encabezado AUTHORIZATION : " + authorizationHeader);
				throw new NotAuthorizedException("Authorization header must be provided");
			}

			// Extrae el token desde el encabezado
			String cadJWT = authorizationHeader.substring("Bearer".length()).trim();
			LOG.info("JWT recibido: " + cadJWT);
			String[] subcadenas = cadJWT.split("[.]");
			if (subcadenas.length != 3) {
				LOG.error("AUTHORIZATION invalid token : " + cadJWT);
				requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
			} else {
				// [0]header-decode64
				byte[] decoded = Base64.decodeBase64(subcadenas[0]);
				String cadHeader = new String(decoded, "UTF-8");
				LOG.info(cadHeader);

				// [1]subject-decode64
				decoded = Base64.decodeBase64(subcadenas[1]);
				String cadPayLoad = new String(decoded, "UTF-8");
				LOG.info(cadPayLoad);

				// [sub]- obtener el token dentro del PAYLOAD
				JsonParser parser = new JsonParser();
				JsonObject jsonPL = (JsonObject) parser.parse(cadPayLoad);
				if (jsonPL.has("sub")) {
					Integer userID = jsonPL.get("sub").getAsInt();

					// Se Recupera el TOKEN del usuario mediante su ID
					TokenUserBean userToken;
					try {
						userToken = tokenDAO.obtenerTokenUserJNDI(userID);

						// Se evalua si existe un userTOKEN
						if (userToken != null) {
							String tokenUserID =  userToken.getToken();
							String firmaRecalculada = this.encode(tokenUserID, subcadenas[0] + "." + subcadenas[1]);
							LOG.info("Firma recalculada: " + firmaRecalculada);
							LOG.info("   Firma recibida: " + subcadenas[2]);

							// si firmaRecalculada == firma
							if (firmaRecalculada.trim().equals(subcadenas[2])) {
								LOG.info("Firma Valida!!!");

								// Se debe validar la caducidad del token
								if (((Timestamp) userToken.getExpirationdate()).getTime() > System.currentTimeMillis()) {
									// si esta vigente se actualiza su caducidad
									LOG.info("Caducidad Valida...");

									// Se evalua que la ruta del endpoint que se encuentra en el payload
									// sea la misma que del contecto que se esta ejecutando
									String contextURI = URLDecoder.decode(requestContext.getUriInfo().getRequestUri().toString(), "UTF-8");

									String payloadURI = jsonPL.get("aud").getAsString();
									payloadURI = URLDecoder.decode(payloadURI, "UTF-8");
									
									LOG.info("URI del contexto:" + contextURI);
									LOG.info("URI del payload:" + payloadURI);

									if (contextURI.equals(payloadURI)) {
										long diff = System.currentTimeMillis() - ((Timestamp)userToken.getLastUpdate()).getTime(); 
										LOG.info("URI valida!!!");
										// Update token in database only after 10 minutes since last update
										// Check if more than 10 minutes have passed since the last update
										if ( diff > 600000) {
											if (tokenDAO.actualizarCaducidadDeTokenUserJNDI(userToken.getIdUser(),timeOut)) {
												LOG.info("Caducidad del Token actualizada ");
											} else {
												LOG.info("Caducidad del Token SIN actualizar ");
											}
										}
									} else {
										LOG.error("Ruta inválida: ");
										requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
									}
								} else {
									// si no esta vigente se manda error de token invalido
									LOG.error("Caducidad inválida: " + (Timestamp) userToken.getExpirationdate());
									requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
								}

							} else {
								LOG.error("Firma inválida...: ");
								requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
							}

						} else {
							LOG.error("NO se encuentra registrado el usuarioTOKEN");
							requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
						}

					} catch (NamingException e) {
						// TODO Auto-generated catch block
						LOG.error(e);
						requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						LOG.error(e);
						requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
					}

				} else {
					LOG.error("NO se ha recibido usuario dentro del payload");
					requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
				}
			}

		} else {
			LOG.info("NO se evalua token");
		}

	}

	public String encode(String key, String data) {
		try {

			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
			sha256_HMAC.init(secret_key);
			return new String(new Base64(true).encode(sha256_HMAC.doFinal(data.getBytes("UTF-8"))));

		} catch (NoSuchAlgorithmException e) {
			LOG.error(e);
		} catch (InvalidKeyException e) {
			LOG.error(e);
		} catch (UnsupportedEncodingException e) {
			LOG.error(e);
		}

		return null;
	}

}