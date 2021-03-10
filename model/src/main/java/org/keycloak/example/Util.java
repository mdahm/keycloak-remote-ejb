package org.keycloak.example;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

public abstract class Util
{
  private Util()
  {
  }

  public static final String USERINFO_PATH = "/realms/{realm-name}/protocol/openid-connect/userinfo";
  public static final String KEYCLOAK_SECRET = "6ec720af-70dd-4b7b-8a1f-876f7a42c3b7";
  public static final String KEYCLOAK_REALM = "ejb-demo";
  public static final String KEYCLOAK_CLIENT = "ejb-client";
  public static final String AUTHORIZATION_HEADER = "Authorization";

  public static Header createAuthorizationHeader(final KeycloakToken keycloakToken)
  {
    return new BasicHeader(AUTHORIZATION_HEADER,"Bearer " + keycloakToken.getToken());
  }
}
