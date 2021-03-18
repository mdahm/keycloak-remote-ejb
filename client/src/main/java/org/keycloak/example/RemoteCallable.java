package org.keycloak.example;

import java.util.concurrent.Callable;

public class RemoteCallable implements Callable<Void>
{
  private final Callable<Void> _callable;
  private final KeycloakToken _keycloakToken;

  RemoteCallable(final Callable<Void> callable, final KeycloakToken keycloakToken)
  {
    _callable = callable;
    _keycloakToken = keycloakToken;
  }

  @Override
  public Void call() throws Exception
  {
    /* Fungiert quasi als Zwischenspeicher für unsere Daten, wird vom ClientInteceptor ausgelesen und den InvocationContext
     * hinzugefügt. Und von dort wird es dann serverseitig vom ServerSecurityInterceptor ausgelesen
     */
    SecurityActions.securityContextSetPrincipalCredential(null, _keycloakToken);

    try
    {
      return _callable.call();
    }
    finally
    {
      SecurityActions.clearSecurityContext();
    }
  }
}
