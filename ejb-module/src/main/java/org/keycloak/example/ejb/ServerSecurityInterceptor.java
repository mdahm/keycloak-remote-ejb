package org.keycloak.example.ejb;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.keycloak.example.Util.KEYCLOAK_SECRET;
import static org.keycloak.example.Util.USERINFO_PATH;
import static org.keycloak.example.Util.createUserInfoRequest;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.jboss.logging.Logger;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.common.util.StreamUtil;
import org.keycloak.example.KeyCloakDeploymentHolder;
import org.keycloak.example.KeycloakToken;

@Interceptor
public class ServerSecurityInterceptor
{
  private static final Logger LOGGER = Logger.getLogger(ServerSecurityInterceptor.class);

  @Inject
  private KeyCloakTokenStore keyCloakTokenStore;

  @Inject
  private KeyCloakDeploymentHolder holder;

  @AroundInvoke
  public Object aroundInvoke(final InvocationContext invocationContext) throws Exception
  {
    LOGGER.info("Intercept");

    final Map<String, Object> contextData = invocationContext.getContextData();

    if (contextData.containsKey(KeycloakToken.TOKEN_KEY))
    {
      final KeycloakToken keycloakToken = (KeycloakToken) contextData.get(KeycloakToken.TOKEN_KEY);
      final boolean validate = keyCloakTokenStore.registerToken(keycloakToken);

      if (validate)
      {
        LOGGER.info("KeyCloak token for user " + keycloakToken.getUsername() + " needs to be validated!");
        final KeycloakDeployment keycloakDeployment = holder.getKeycloakDeployment();

        if (keycloakDeployment != null)
        {
          validateToken(keycloakDeployment, keycloakToken);
        }
        else
        {
          throw new IllegalStateException("Cannot obtain reference to KeyCloak deployment");
        }
      }
    }

    return invocationContext.proceed();
  }

  // TODO

//  public void logout(final KeycloakDeployment deployment, final KeycloakToken keycloakToken) throws IOException
//  {
//    final HttpClient client = deployment.getClient();
//    final String authServerBaseUrl = deployment.getAuthServerBaseUrl();
//    final HttpPost request = new HttpPost(KeycloakUriBuilder.fromUri(authServerBaseUrl)
//        .path(ServiceUrlConstants.TOKEN_SERVICE_LOGOUT_PATH).build(deployment.getRealm()));
//    final List<NameValuePair> formparams = new ArrayList<>();
//    formparams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, deployment.getResourceName()));
//    formparams.add(new BasicNameValuePair(OAuth2Constants.REFRESH_TOKEN, keycloakToken.getRefreshToken()));
//    formparams.add(new BasicNameValuePair(OAuth2Constants.USERNAME, keycloakToken.getUsername()));
//    formparams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_SECRET, KEYCLOAK_SECRET));
//    final UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, "UTF-8");
//
//    request.setEntity(form);
//
//    // https://stackoverflow.com/questions/48274251/keycloak-access-token-validation-end-point
//
//    final HttpResponse response = client.execute(request);
//    final int status = response.getStatusLine().getStatusCode();
//    final HttpEntity entity = response.getEntity();
//    final boolean validEntity = entity != null && entity.getContent() != null;
//    final String body = validEntity ? StreamUtil.readString(entity.getContent(), Charset.defaultCharset()) : "";
//
//    LOGGER.info("Response body: " + body);
//  }

  private void validateToken(final KeycloakDeployment deployment, final KeycloakToken keycloakToken) throws IOException
  {
    final HttpClient client = deployment.getClient();
    final String authServerBaseUrl = deployment.getAuthServerBaseUrl();
    final URI userInfoUri = KeycloakUriBuilder.fromUri(authServerBaseUrl).path(USERINFO_PATH)
        .queryParam(OAuth2Constants.CLIENT_SECRET, KEYCLOAK_SECRET).build(deployment.getRealm());
    final HttpGet request = createUserInfoRequest(userInfoUri, keycloakToken);
    final HttpResponse response = client.execute(request);
    final int status = response.getStatusLine().getStatusCode();
    final HttpEntity entity = response.getEntity();
    final boolean validEntity = entity != null && entity.getContent() != null;
    final String body = validEntity ? StreamUtil.readString(entity.getContent(), Charset.defaultCharset()) : "";

    LOGGER.info("Response body: " + body);

    switch (status)
    {
    case SC_UNAUTHORIZED:
      throw new SecurityException("Unauthorized request: " + body);
    case SC_OK:
      return;
    default:
      throw new SecurityException("Invalid response from Server: Status " + status);
    }
  }
}
