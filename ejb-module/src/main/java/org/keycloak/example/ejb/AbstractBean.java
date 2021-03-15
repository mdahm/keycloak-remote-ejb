package org.keycloak.example.ejb;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;

@SecurityDomain("keycloak-ejb")
@Interceptors({ ServerSecurityInterceptor.class })
public abstract class AbstractBean
{
  @Resource
  protected SessionContext ctx;

  @Inject
  protected KeyCloakTokenStore keyCloakTokenStore;

  protected SecurityContext getSecurityContext()
  {
    return AccessController.doPrivileged(
        (PrivilegedAction<SecurityContext>) SecurityContextAssociation::getSecurityContext);
  }
}
