package org.keycloak.example.ejb;

import java.io.IOException;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface RemoteHello
{
  String helloSimple() throws IOException;

  void logout() throws IOException;

  String helloAdvanced() throws IOException;
}
