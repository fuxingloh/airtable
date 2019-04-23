package dev.fuxing.airtable;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Executor;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 18:58
 */
public final class AirtableExecutor {

    final static PoolingHttpClientConnectionManager CONNECTION_MANAGER;
    final static RequestConfig REQUEST_CONFIG;
    final static HttpClient CLIENT;
    final static Registry<ConnectionSocketFactory> SFR;

    static {
        LayeredConnectionSocketFactory ssl = null;
        try {
            ssl = SSLConnectionSocketFactory.getSystemSocketFactory();
        } catch (final SSLInitializationException ex) {
            final SSLContext sslcontext;
            try {
                sslcontext = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
                sslcontext.init(null, null, null);
                ssl = new SSLConnectionSocketFactory(sslcontext);
            } catch (final SecurityException | KeyManagementException | NoSuchAlgorithmException ignore) {
            }
        }

        SFR = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", ssl != null ? ssl : SSLConnectionSocketFactory.getSocketFactory())
                .build();

        CONNECTION_MANAGER = new PoolingHttpClientConnectionManager(SFR);
        // No point having too many, or else it will cause 429 error rather quickly
        CONNECTION_MANAGER.setMaxTotal(8);
        CONNECTION_MANAGER.setDefaultMaxPerRoute(8);
        CONNECTION_MANAGER.setValidateAfterInactivity(1000);

        REQUEST_CONFIG = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD).build();

        CLIENT = HttpClientBuilder.create()
                .setConnectionManager(CONNECTION_MANAGER)
                .setServiceUnavailableRetryStrategy(new RetryStrategy(2))
                .setDefaultRequestConfig(REQUEST_CONFIG)
                .build();
    }

    /**
     * @return default Executor with 3 maximum retry before failing
     */
    public static Executor newInstance() {
        return newInstance(true, 3);
    }

    /**
     * @param autoRetry whether auto try is enabled
     * @param maxRetry  maximum retry before failing
     * @return Executor
     */
    public static Executor newInstance(boolean autoRetry, int maxRetry) {
        HttpClientBuilder builder = HttpClientBuilder.create()
                .setConnectionManager(CONNECTION_MANAGER)
                .setDefaultRequestConfig(REQUEST_CONFIG);

        if (autoRetry) {
            builder.setServiceUnavailableRetryStrategy(new RetryStrategy(maxRetry));
        }

        return Executor.newInstance(builder.build())
                .use(new CookieStore());
    }

    /**
     * @return Executor with no retry, with 100 max conneciton pool
     */
    public static Executor newInstanceTurbo() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(SFR);
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(100);
        connectionManager.setValidateAfterInactivity(1000);

        HttpClientBuilder builder = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(REQUEST_CONFIG);

        return Executor.newInstance(builder.build())
                .use(new CookieStore());
    }

    public static final class CookieStore implements org.apache.http.client.CookieStore {

        @Override
        public void addCookie(Cookie cookie) {

        }

        @Override
        public List<Cookie> getCookies() {
            return Collections.emptyList();
        }

        @Override
        public boolean clearExpired(Date date) {
            return false;
        }

        @Override
        public void clear() {

        }
    }

    public static final class RetryStrategy implements ServiceUnavailableRetryStrategy {
        private static final Logger logger = Logger.getLogger(RetryStrategy.class.getName());

        private final int maxCount;

        public RetryStrategy(int maxCount) {
            this.maxCount = maxCount;
        }

        @Override
        public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 429 && executionCount < maxCount) {
                logger.info("429: Airtable Retry, Sleeping, Count: " + executionCount);
                sleep();
                logger.info("429: Airtable Retry, Resuming, Count: " + executionCount);
                return true;
            }

            return false;
        }

        @Override
        public long getRetryInterval() {
            return 0;
        }

        private void sleep() {
            try {
                Thread.sleep(30_001);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
