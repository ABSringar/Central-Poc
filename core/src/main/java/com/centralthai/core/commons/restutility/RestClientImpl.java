package com.centralthai.core.commons.restutility;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Designate(ocd = RestClientImpl.Config.class)
@Component(service = RestClient.class)
public class RestClientImpl implements RestClient {


    @ObjectClassDefinition(name = "Central REST Client Configurations",
            description = "Rest client configurations to make API calls from AEM")
    public static @interface Config {

        @AttributeDefinition(name = "Connection Timeout"
                , description = "Determines the timeout until a connection is established")
        int connection_timeout() default 60;

        @AttributeDefinition(name = "Socket Timeout",
                description = "Defines the default socket timeout in milliseconds which is the timeout for waiting for data")
        int socket_timeout() default 60;

        @AttributeDefinition(name = "Maximum Total Connection",
                description = "The maximum number of connections allowed")
        int max_total_connection() default 1000;

        @AttributeDefinition(name = "Default Max Connections Per Host",
                description = "The default maximum number of connections allowed per host")
        int default_max_connections_per_host() default 1000;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientImpl.class);
    private PoolingHttpClientConnectionManager connectionManger;
    private int connectionTimeout;
    private int socketTimeout;
    private int maxTotalConn;
    private int defaultMaxConnPerHost;
    private long end;
    private long start;

    @Activate
    protected void activate(final RestClientImpl.Config config) {
        connectionTimeout = config.connection_timeout();
        socketTimeout = config.socket_timeout();
        maxTotalConn = config.max_total_connection();
        defaultMaxConnPerHost = config.default_max_connections_per_host();
    }


    @Deactivate
    protected void deactivate(final RestClientImpl.Config config) {
        connectionTimeout = 0;
        socketTimeout = 0;
        maxTotalConn = 0;
        defaultMaxConnPerHost = 0;
    }

    /**
     * Method to execute GET request
     *
     * @param url      API endpoint
     * @param username auth username
     * @param password auth password
     * @param headers  httpheaders
     * @return ResponseModel
     */
    @Override
    public ResponseModel httpGet(String url, String username, String password, Map<String, String> headers) {
        LOGGER.debug("------>Inside httpGet()<------");
        String apiResponse = StringUtils.EMPTY;
        ResponseModel responseModel = new ResponseModel();
        CloseableHttpClient httpClientInstance = getHttpClientInstance();
        HttpGet getMethod = new HttpGet(url);
        for (Map.Entry<String, String> httpGetHeader : headers.entrySet()) {
            getMethod.setHeader(httpGetHeader.getKey(), httpGetHeader.getValue());
        }
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            String auth = setAuthorizationHeader(username, password);
            getMethod.setHeader(HttpHeaders.AUTHORIZATION, auth);
        }
        CloseableHttpResponse response = null;
        try {
            start = System.currentTimeMillis();
            LOGGER.debug("----->Request URL<------:{}", url);
            response = httpClientInstance.execute(getMethod);
            end = System.currentTimeMillis();
            LOGGER.debug("------>Response time in milliseconds<-------={} ", end - start);
            responseModel.setStatusCode(response.getStatusLine().getStatusCode());
            responseModel.setHeaders(response.getAllHeaders());
            HttpEntity httpEntity = response.getEntity();
            if (httpEntity != null) {
                apiResponse = EntityUtils.toString(httpEntity, "UTF-8");
                EntityUtils.consume(httpEntity);
            }
            responseModel.setResponseBody(apiResponse);
        } catch (IOException e) {
            LOGGER.error("IOException in RestClientImpl:: httpGet() ", e);
        } finally {
            getMethod.releaseConnection();
        }
        return responseModel;

    }


    /**
     * Method to execute POST request
     *
     * @param url        API endpoint
     * @param username   auth username
     * @param password   auth password
     * @param parameters Request body
     * @param headers    httpheaders
     * @return ResponseModel
     */
    @Override
    public ResponseModel httpPost(String url, String username, String password, String parameters, Map<String, String> headers) {
        LOGGER.debug("------>Inside httpPost()<------");
        String apiResponse = StringUtils.EMPTY;
        ResponseModel responseModel = new ResponseModel();
        Optional<CloseableHttpClient> httpClientInstance;
        httpClientInstance = Optional.ofNullable(getHttpClientInstance());
        if (StringUtils.isNotEmpty(url) && httpClientInstance.isPresent()) {
            HttpPost postMethod = new HttpPost(url);
            for (Map.Entry<String, String> httpPostHeader : headers.entrySet()) {
                postMethod.setHeader(httpPostHeader.getKey(), httpPostHeader.getValue());
            }
            if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                String auth = setAuthorizationHeader(username, password);
                postMethod.setHeader(HttpHeaders.AUTHORIZATION, auth);
            }
            if (StringUtils.isNotEmpty(parameters)) {
                postMethod.setEntity(new StringEntity(parameters, "UTF8"));
            }
            CloseableHttpResponse response;
            try {
                start = System.currentTimeMillis();
                LOGGER.debug("---->RequestURL<----- :{}", url);
                LOGGER.debug("---->UserName<----:{}", username);
                LOGGER.debug("---->Password<-----:{}", password);
                LOGGER.debug("---->Request Parameters<----:{}", parameters);
                response = httpClientInstance.get().execute(postMethod);
                end = System.currentTimeMillis();
                LOGGER.debug("------> Response time in milliseconds<----- ={}", end - start);
                responseModel.setStatusCode(response.getStatusLine().getStatusCode());
                responseModel.setHeaders(response.getAllHeaders());
                Optional<HttpEntity> httpEntity = Optional.ofNullable(response.getEntity());
                if (httpEntity.isPresent()) {
                    apiResponse = EntityUtils.toString(response.getEntity(), "UTF-8");
                    EntityUtils.consume(httpEntity.get());
                }
                responseModel.setResponseBody(apiResponse);
            } catch (IOException e) {
                LOGGER.error("IOException in RestClientImpl:: httpPost() ", e);
            } finally {
                postMethod.releaseConnection();
            }
        }

        return responseModel;
    }

    /**
     * Method to create httpclient using request config and connection manger
     *
     * @return CloseableHttpClient
     */
    private CloseableHttpClient getHttpClientInstance() {
        return HttpClientBuilder.create().
                setConnectionManager(getConnPool()).
                setDefaultRequestConfig(getTimeoutConfigs()).build();

    }

    /**
     * Method to get request Config
     *
     * @return requestConfig
     */
    private RequestConfig getTimeoutConfigs() {
        return RequestConfig.custom().
                setConnectionRequestTimeout(connectionTimeout).
                setConnectTimeout(connectionTimeout).
                setSocketTimeout(socketTimeout).build();
    }

    /**
     * Method to set the data pool connection properties
     *
     * @return connectionManger
     */
    private PoolingHttpClientConnectionManager getConnPool() {
        if (connectionManger != null) {
            connectionManger = new PoolingHttpClientConnectionManager();
            connectionManger.setMaxTotal(maxTotalConn);
            connectionManger.setMaxTotal(defaultMaxConnPerHost);
        }
        return connectionManger;
    }

    /**
     * setting authentication computed using the username and password encoded to base64.
     *
     * @return
     */
    private static String setAuthorizationHeader(String userName, String password) {
        final String BASIC_AUTH_PREFIX = "Basic ";
        String userNamePassword = userName + ":" + password;
        String encodedString = encodeBase64(userNamePassword);
        return BASIC_AUTH_PREFIX + encodedString;
    }

    /**
     * @param encodedValue String to be encoded.
     * @return base64 encoding of the string passed.
     */
    public static String encodeBase64(String encodedValue) {
        byte[] encoded = Base64.encodeBase64(encodedValue.getBytes());
        return new String(encoded);
    }

}
