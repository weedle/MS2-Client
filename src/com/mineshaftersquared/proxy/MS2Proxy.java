package com.mineshaftersquared.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mineshaftersquared.UniversalLauncher;

public class MS2Proxy implements Runnable {

    public static final int MAX_NUM_CONNECTIONS = 16;
    public static final int TIMEOUT_DURATION = 5 * 1000;

    public static Pattern SKIN_URL = Pattern.compile("http://skins\\.minecraft\\.net/MinecraftSkins/(.+?)\\.png");
    public static Pattern CLOAK_URL = Pattern.compile("http://skins\\.minecraft\\.net/MinecraftCloaks/(.+?)\\.png");
    public static Pattern AUTH_URL = Pattern.compile("http://authserver\\.mojang\\.com/(.*)");
    public static Pattern CHECKSERVER_URL = Pattern.compile("http://session.minecraft.net/game/checkserver.jsp(.*)");
    public static Pattern JOINSERVER_URL = Pattern.compile("http://session.minecraft.net/game/joinserver.jsp(.*)");
    public static final Log log = LogFactory.getFactory().getInstance("[MS2Proxy]");

    public final RoutesDataSource routes;
    private ServerSocket server;
    private HandlerFactory handlerFactory;
    private volatile boolean shouldStop;
    private boolean hasStarted;
    private final Object isRunningLock;
    private boolean isInitialized;
    private final Object isInitializedLock;

    public MS2Proxy(RoutesDataSource routes, HandlerFactory handlerFactory) {
        this.routes = routes;
        this.server = null;
        this.handlerFactory = handlerFactory;
        this.shouldStop = false;
        this.hasStarted = false;
        this.isRunningLock = new Object();
        this.isInitialized = false;
        this.isInitializedLock = new Object();
    }

    public Thread async() {
        try {
            this.initialize();
            return new Thread(this);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Thread startAsync() {
        return this.startAsync(true);
    }

    public Thread startAsync(boolean daemon) {
        Thread t = this.async();
        if (t != null) {
            t.setDaemon(daemon);
            t.start();
        }
        return t;
    }

    @Override
    public void run() {
        synchronized (this.isRunningLock) {
            if (this.hasStarted) {
                throw new IllegalStateException("MS2Proxy already running");
            }
            this.hasStarted = true;
        }
        try {
            while (true) {
                Socket s = null;
                try {
                    if (this.shouldStop) {
                        break;
                    }
                    s = this.server.accept();
                    final Socket socket = s;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            MS2Proxy.this.handlerFactory.createHandler().handle(MS2Proxy.this, socket);
                        }
                    }).start();
                } catch (SocketTimeoutException acceptable) {
                    IOUtils.closeQuietly(s);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    IOUtils.closeQuietly(s);
                }
            }
        } finally {
            IOUtils.closeQuietly(this.server);
        }
        UniversalLauncher.log.info("MS2Proxy done");
    }

    public void initialize() throws IOException {
        synchronized (this.isInitializedLock) {
            if (this.isInitialized) {
                throw new IllegalStateException("MS2Proxy already initialized");
            }
            this.isInitialized = true;
        }
        this.server = new ServerSocket(0, MAX_NUM_CONNECTIONS, InetAddress.getLoopbackAddress());
        UniversalLauncher.log.info("Proxy on: " + this.server.getLocalSocketAddress().toString());
        this.server.setSoTimeout(TIMEOUT_DURATION);
    }

    public int getProxyPort() {
        return this.server.getLocalPort();
    }

    public void stopProxy() {
        this.shouldStop = true;
    }

    public static interface Handler {
        public void handle(MS2Proxy ms2Proxy, Socket socket);
    }

    public static interface HandlerFactory {
        public Handler createHandler();
    }

    public static interface RoutesDataSource {
        // textures.js
        public String getSkinURL(String username);

        public String getCloakURL(String username);

        // authenticate.js
        public String getAuthenticateURL();

        public String getRefreshURL();

        public String getInvalidateURL();

        // game.js
        public String getVersionURL();

        public String getLoginURL();

        public String getJoinServerURL();

        public String getCheckServerURL();

        // launcher.js
        public String getLauncherVersionURL();
    }

    public static class MS2RoutesDataSource implements RoutesDataSource {
        public final String baseURL;

        /**
         * Default is http://api.mineshaftersquared.com
         *
         * @param baseURL
         */
        public MS2RoutesDataSource(String baseURL) {
            this.baseURL = baseURL;
        }

        @Override
        public String getSkinURL(String username) {
            return "http://mineshaftersquared.com/game/get_skin/" + username;
            //return this.baseURL + "/skin/" + username;
        }

        @Override
        public String getCloakURL(String username) {
            return "http://mineshaftersquared.com/game/get_cloak/" + username;
            //return this.baseURL + "/cloak/" + username;
        }

        @Override
        public String getAuthenticateURL() {
            return this.baseURL + "/authenticate";
        }

        @Override
        public String getRefreshURL() {
            return this.baseURL + "/refresh";
        }

        @Override
        public String getInvalidateURL() {
            return this.baseURL + "/invalidate";
        }

        @Override
        public String getVersionURL() {
            return this.baseURL + "/version";
        }

        @Override
        public String getLoginURL() {
            return this.baseURL + "/login";
        }

        @Override
        public String getJoinServerURL() {
            return this.baseURL + "/game/joinserver";
        }

        @Override
        public String getCheckServerURL() {
            return this.baseURL + "/game/checkserver";
        }

        @Override
        public String getLauncherVersionURL() {
            return this.baseURL + "/launcher/version";
        }
    }
}
