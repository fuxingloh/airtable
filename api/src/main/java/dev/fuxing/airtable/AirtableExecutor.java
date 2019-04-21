package dev.fuxing.airtable;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
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

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 18:58
 */
public final class AirtableExecutor {

    final static PoolingHttpClientConnectionManager CONNMGR;
    final static HttpClient CLIENT;

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

        final Registry<ConnectionSocketFactory> sfr = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", ssl != null ? ssl : SSLConnectionSocketFactory.getSocketFactory())
                .build();

        CONNMGR = new PoolingHttpClientConnectionManager(sfr);
        CONNMGR.setDefaultMaxPerRoute(100);
        CONNMGR.setMaxTotal(200);
        CONNMGR.setValidateAfterInactivity(1000);
        CLIENT = HttpClientBuilder.create()
                .setConnectionManager(CONNMGR)
                .build();

        // TODO(fuxing): 429 auto retry
    }

    public static Executor newInstance() {
        return Executor.newInstance(CLIENT)
                .use(new EmptyCookieStore());
    }

    public static final class EmptyCookieStore implements CookieStore {

        @Override
        public void addCookie(Cookie cookie) {

        }

        @Override
        public List<Cookie> getCookies() {
            return null;
        }

        @Override
        public boolean clearExpired(Date date) {
            return false;
        }

        @Override
        public void clear() {

        }
    }
}
