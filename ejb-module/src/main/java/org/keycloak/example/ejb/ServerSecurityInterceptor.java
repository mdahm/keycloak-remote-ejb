package org.keycloak.example.ejb;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.logging.Logger;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.common.util.StreamUtil;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.example.KeyCloakDeploymentHolder;
import org.keycloak.example.KeycloakToken;

@Interceptor
public class ServerSecurityInterceptor
{
  private static final Logger LOGGER = Logger.getLogger(ServerSecurityInterceptor.class);
  public static final String USERINFO_PATH = "/realms/{realm-name}/protocol/openid-connect/userinfo";

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

  public void logout(final KeycloakDeployment deployment, final KeycloakToken keycloakToken) throws IOException
  {
    final HttpClient client = deployment.getClient();
    final String authServerBaseUrl = deployment.getAuthServerBaseUrl();
    final HttpPost request = new HttpPost(KeycloakUriBuilder.fromUri(authServerBaseUrl)
        .path(ServiceUrlConstants.TOKEN_SERVICE_LOGOUT_PATH).build(deployment.getRealm()));
    final List<NameValuePair> formparams = new ArrayList<>();
    formparams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, deployment.getResourceName()));
    formparams.add(new BasicNameValuePair(OAuth2Constants.REFRESH_TOKEN, keycloakToken.getRefreshToken()));
    formparams.add(new BasicNameValuePair(OAuth2Constants.USERNAME, keycloakToken.getUsername()));
    final UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, "UTF-8");

    request.setEntity(form);

    // https://stackoverflow.com/questions/48274251/keycloak-access-token-validation-end-point

    final HttpResponse response = client.execute(request);
    final int status = response.getStatusLine().getStatusCode();
    final HttpEntity entity = response.getEntity();
    final boolean validEntity = entity != null && entity.getContent() != null;
    final String body = validEntity ? StreamUtil.readString(entity.getContent(), Charset.defaultCharset()) : "";

    LOGGER.info("Response body: " + body);
  }

  private void validateToken(final KeycloakDeployment deployment, final KeycloakToken keycloakToken) throws IOException
  {
    final HttpClient client = deployment.getClient();
    final String authServerBaseUrl = deployment.getAuthServerBaseUrl();
    final URI userInfoUri = KeycloakUriBuilder.fromUri(authServerBaseUrl).path(USERINFO_PATH).build(deployment.getRealm());
    final HttpGet request = new HttpGet(userInfoUri);
    request.addHeader("Authorization", "Bearer " + keycloakToken.getToken());
    final HttpResponse response = client.execute(request);
    final int status = response.getStatusLine().getStatusCode();
    final HttpEntity entity = response.getEntity();
    final boolean validEntity = entity != null && entity.getContent() != null;
    final String body = validEntity ? StreamUtil.readString(entity.getContent(), Charset.defaultCharset()) : "";

    LOGGER.info("Response body: " + body);

    switch (status)
    {
    case SC_UNAUTHORIZED:
      throw new IOException("Unauthorized request: " + body);
    case SC_OK:
      return;
    default:
      throw new IOException("Invalid response from Server: Status " + status);
    }
  }
}
