package org.keycloak.example;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Username + token sent from remote EJB client to the Wildfly
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class KeycloakToken implements Serializable
{
  private static final long serialVersionUID = 1L;

  public static final String TOKEN_KEY = "de.bwb.kanal.ubi.tokenKey";

  private final String username;
  private final Set<String> roles;
  private final String token;
  private final String refreshToken;

  public KeycloakToken(final String username, final Set<String> roles, final String token, final String refreshToken)
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

  public Set<String> getRoles()
  {
    return roles;
  }

  public String getToken()
  {
    return token;
  }

  @Override
  public String toString()
  {
    return "KeycloakToken{" + "username='" + username + '\'' + ", roles=" + roles + '}';
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
