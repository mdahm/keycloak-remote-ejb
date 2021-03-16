package org.keycloak.example.ejb;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.security.PermitAll;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import org.keycloak.example.KeycloakToken;

import org.jboss.logging.Logger;

@Singleton
@Lock(LockType.WRITE)
@PermitAll
public class KeyCloakTokenStore
{
  private static final Logger LOGGER = Logger.getLogger(KeyCloakTokenStore.class);

  private final Map<String, KeycloakToken> registeredTokens = new HashMap<>();

  /**
   * Return true if given keycloakToken needs to be checked against KeyCloak server, i.e. is new
   */
  public boolean registerToken(final KeycloakToken keycloakToken)
  {
    assert keycloakToken != null : "keycloakToken must not be null";

    final String token = keycloakToken.getToken();
    final boolean result = !registeredTokens.containsKey(token);

    registeredTokens.putIfAbsent(token, keycloakToken);

    LOGGER.info("Register KC Token for user " + keycloakToken.getUsername());

    return result;
  }

  public void invalidate(final String token)
  {
    assert token != null : "token must not be null";

    if (!registeredTokens.containsKey(token))
    {
      LOGGER.warn("Invalidate: Unknown token");
    }

    registeredTokens.remove(token);
  }
}
