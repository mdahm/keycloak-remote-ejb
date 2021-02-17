package org.keycloak.example.ejb;

import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;

import org.jboss.as.security.api.ConnectionSecurityContext;
import org.jboss.as.security.api.ContextStateCache;

/**
 * Security actions for this package only.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
final class SecurityActions
{
  private SecurityActions()
  {
  }

  static ContextStateCache pushIdentity(final Principal principal, final Object credential) throws Exception
  {
    return connectionSecurityContextActions().pushIdentity(principal, credential);
  }

  static void popIdentity(final ContextStateCache stateCache)
  {
    connectionSecurityContextActions().popIdentity(stateCache);
  }

  private static ConnectionSecurityContextActions connectionSecurityContextActions()
  {
    return System.getSecurityManager() == null ?
        ConnectionSecurityContextActions.NON_PRIVILEGED : ConnectionSecurityContextActions.PRIVILEGED;
  }

  private interface ConnectionSecurityContextActions
  {
    Collection<Principal> getConnectionPrincipals();

    ContextStateCache pushIdentity(final Principal principal, final Object credential) throws Exception;

    void popIdentity(final ContextStateCache stateCache);

    ConnectionSecurityContextActions NON_PRIVILEGED = new ConnectionSecurityContextActions()
    {
      public Collection<Principal> getConnectionPrincipals()
      {
        return ConnectionSecurityContext.getConnectionPrincipals();
      }

      @Override
      public ContextStateCache pushIdentity(final Principal principal, final Object credential) throws Exception
      {
        return ConnectionSecurityContext.pushIdentity(principal, credential);
      }

      @Override
      public void popIdentity(ContextStateCache stateCache)
      {
        ConnectionSecurityContext.popIdentity(stateCache);
      }
    };

    ConnectionSecurityContextActions PRIVILEGED = new ConnectionSecurityContextActions()
    {
      final PrivilegedAction<Collection<Principal>> GET_CONNECTION_PRINCIPALS_ACTION = NON_PRIVILEGED::getConnectionPrincipals;

      public Collection<Principal> getConnectionPrincipals()
      {
        return AccessController.doPrivileged(GET_CONNECTION_PRINCIPALS_ACTION);
      }

      @Override
      public ContextStateCache pushIdentity(final Principal principal, final Object credential) throws Exception
      {
        try
        {
          return AccessController.doPrivileged(
              (PrivilegedExceptionAction<ContextStateCache>) () -> NON_PRIVILEGED.pushIdentity(principal, credential));
        }
        catch (final PrivilegedActionException e)
        {
          throw e.getException();
        }
      }

      @Override
      public void popIdentity(final ContextStateCache stateCache)
      {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
          NON_PRIVILEGED.popIdentity(stateCache);
          return null;
        });
      }
    };
  }
}
