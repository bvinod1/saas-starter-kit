package com.saasstarterkit.user.security;

import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.util.CollectionUtils;

import com.saasstarterkit.user.dto.Tenant;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

public class JwtOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

	@Autowired
	private Environment env;

	@SuppressWarnings("unchecked")
	@Override
	public OAuth2AuthenticatedPrincipal introspect(String token) {

		String appname = Tenant.getAppname();
		String client = appname == null ? "" : appname + ".";
		String introspectionuri = env.getProperty(client + "keycloak.auth-server-url") + "/realms/"
					+ env.getProperty(client + "keycloak.realm") + "/protocol/openid-connect/token/introspect";
		String clientId = env.getProperty(client + "keycloak.resource");
		String clientSecret = env.getProperty(client + "keycloak.credentials.secret");

		OpaqueTokenIntrospector delegate = new NimbusOpaqueTokenIntrospector(introspectionuri, clientId, clientSecret);
		JwtDecoder jwtDecoder = new NimbusJwtDecoder(new ParseOnlyJWTProcessor());

		delegate.introspect(token);
		try {
			Jwt jwt = jwtDecoder.decode(token);
			return new DefaultOAuth2AuthenticatedPrincipal(jwt.getClaims(),
					(Collection<GrantedAuthority>) extractUserRoles(jwt));
		} catch (JwtException ex) {
			throw new OAuth2IntrospectionException(ex.getMessage());
		}
	}

	private static class ParseOnlyJWTProcessor extends DefaultJWTProcessor<SecurityContext> {
		public JWTClaimsSet process(SignedJWT jwt, SecurityContext context) throws JOSEException {
			try {
				return jwt.getJWTClaimsSet();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private Set<? extends GrantedAuthority> extractUserRoles(Jwt jwt) {
	      final Map<String, Object> realmAccess = jwt.getClaim("realm_access");
	      if(realmAccess != null) {
	    	  final List<String> realmRoles = (List<String>) realmAccess.get("roles");
	    	  
	    	  if (!CollectionUtils.isEmpty(realmRoles)) {
	    		  return realmRoles.stream()
	    				  .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
	    				  .collect(Collectors.toSet());
	    	  }
	      }
	      return Collections.emptySet();
	  }
}