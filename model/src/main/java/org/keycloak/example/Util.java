package org.keycloak.example;

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

  public static String createAuthorizationValue(final KeycloakToken keycloakToken)
  {
    return "Bearer " + keycloakToken.getToken();
  }
}
