package org.keycloak.example.ejb;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Username + token sent from remote EJB client to the Wildfly
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class KeycloakToken implements Serializable
{
  private static final long serialVersionUID = 1L;

  public static final String TOKEN_KEY = "tokenKey";

  private final String username;
  private final List<String> roles;
  private final String token;
  private final String refreshToken;

  public KeycloakToken(final String username, final List<String> roles, final String token, final String refreshToken)
  {
    assert username != null : "username must not be null";
    assert roles != null : "roles must not be null";
    assert token != null : "token must not be null";
    assert refreshToken != null : "refreshToken must not be null";

    this.username = username;
    this.roles = roles;
    this.token = token;
    this.refreshToken = refreshToken;
  }

  public String getUsername()
  {
    return username;
  }

  public String getRefreshToken()
  {
    return refreshToken;
  }

  public List<String> getRoles()
  {
    return roles;
  }

  public String getToken()
  {
    return token;
  }

  @Override
  public boolean equals(final Object o)
  {
    if (this == o)
    { return true; }
    if (!(o instanceof KeycloakToken))
    { return false; }

    final KeycloakToken that = (KeycloakToken) o;
    return token.equals(that.token);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(token);
  }
}
