package org.keycloak.example.ejb;

import java.util.HashSet;
import java.util.Set;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

@Singleton
@Lock(LockType.WRITE)
public class KeyCloakTokenStore
{
  private final Set<KeycloakToken> registeredTokens = new HashSet<>();

  /**
   * Return true if given token needs to be checked against KeyCloak server
   */
  public boolean checkToken(final KeycloakToken token) {
    assert token != null : "token must not be null";

    return registeredTokens.add(token);
  }


}
