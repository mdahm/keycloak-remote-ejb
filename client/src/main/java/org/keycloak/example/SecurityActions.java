package org.keycloak.example;

import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;

import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SecurityContextFactory;

/**
 * Security actions for this package only.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class SecurityActions
{
  static SecurityContext securityContextSetPrincipalCredential(final Principal principal, final Object credential)
      throws Exception
  {
    return securityContextActions().setPrincipalCredential(principal, credential);
  }

  static Object securityContextGetCredential()
  {
    return securityContextActions().getCredential();
  }

  static void clearSecurityContext()
  {
    securityContextActions().clearSecurityContext();
  }

  private static SecurityContextActions securityContextActions()
  {
    return System.getSecurityManager() == null ? SecurityContextActions.NON_PRIVILEGED : SecurityContextActions.PRIVILEGED;
  }

  private interface SecurityContextActions
  {

    SecurityContext setPrincipalCredential(final Principal principal, final Object credential) throws Exception;

    Principal getPrincipal();

    Object getCredential();

    void set(final SecurityContext securityContext);

    void clearSecurityContext();

    SecurityContextActions NON_PRIVILEGED = new SecurityContextActions()
    {
      @Override
      public SecurityContext setPrincipalCredential(Principal principal, Object credential) throws Exception
      {
        SecurityContext current = SecurityContextAssociation.getSecurityContext();

        SecurityContext nextContext = SecurityContextFactory.createSecurityContext(principal, credential,
            new Subject(), "USER_DELEGATION");
        SecurityContextAssociation.setSecurityContext(nextContext);

        return current;
      }

      @Override
      public Principal getPrincipal()
      {
        return SecurityContextAssociation.getPrincipal();
      }

      @Override
      public Object getCredential()
      {
        return SecurityContextAssociation.getCredential();
      }

      @Override
      public void set(SecurityContext securityContext)
      {
        SecurityContextAssociation.setSecurityContext(securityContext);
      }

      @Override
      public void clearSecurityContext()
      {
        SecurityContextAssociation.clearSecurityContext();
      }
    };

    SecurityContextActions PRIVILEGED = new SecurityContextActions()
    {
      final PrivilegedAction<Principal> GET_PRINCIPAL_ACTION = NON_PRIVILEGED::getPrincipal;
      final PrivilegedAction<Object> GET_CREDENTIAL_ACTION = NON_PRIVILEGED::getCredential;

      @Override
      public SecurityContext setPrincipalCredential(final Principal principal, final Object credential) throws Exception
      {
        try
        {
          return AccessController.doPrivileged(
              (PrivilegedExceptionAction<SecurityContext>) () -> NON_PRIVILEGED.setPrincipalCredential(principal, credential));
        }
        catch (final PrivilegedActionException e)
        {
          throw e.getException();
        }
      }

      @Override
      public Principal getPrincipal()
      {
        return AccessController.doPrivileged(GET_PRINCIPAL_ACTION);
      }

      @Override
      public Object getCredential()
      {
        return AccessController.doPrivileged(GET_CREDENTIAL_ACTION);
      }

      @Override
      public void set(final SecurityContext securityContext)
      {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
          NON_PRIVILEGED.set(securityContext);
          return null;
        });
      }

      @Override
      public void clearSecurityContext()
      {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
          NON_PRIVILEGED.clearSecurityContext();
          return null;
        });
      }
    };
  }
}
