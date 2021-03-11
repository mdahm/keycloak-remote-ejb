package org.keycloak.example;

import java.net.URI;

import org.apache.http.client.methods.HttpGet;
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

  public static HttpGet createUserInfoRequest(final URI userInfoUri, final KeycloakToken keycloakToken)
  {
    final HttpGet request = new HttpGet(userInfoUri);
    request.addHeader(new BasicHeader(AUTHORIZATION_HEADER,"Bearer " + keycloakToken.getToken()));
    return request;
  }
}
