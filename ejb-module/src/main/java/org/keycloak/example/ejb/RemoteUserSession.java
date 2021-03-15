package org.keycloak.example.ejb;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface RemoteUserSession
{
  String NAME = "users";

  void login();

  void logout();
}
