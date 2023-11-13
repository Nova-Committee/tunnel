package cool.muyucloud.tunnel;

import com.google.gson.Gson;
import cool.muyucloud.tunnel.annotation.Tunnel;
import cool.muyucloud.tunnel.data.TunnelJson;
import cool.muyucloud.tunnel.exception.ConfigAccessException;
import cool.muyucloud.tunnel.exception.TunnelInitiateException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.List;

public class TunnelInitializer {
    private static final String JSON_PATH = "tunnel.json";

    public static void init(Class<?> mod) {
        ClassLoader loader = mod.getClassLoader();
        List<String> tunnelPaths = getTunnelPaths(loader);
        try {
            for (String tunnelPath : tunnelPaths) {
                Class<?> cl = loader.loadClass(tunnelPath);
                if (isTunnelClass(cl)) {
                    Class<? extends McTunnel> tunnelCl = cl.asSubclass(McTunnel.class);
                    invokeInitTunnel(tunnelCl);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void invokeInitTunnel(Class<? extends McTunnel> cl) {
        try {
            Constructor<? extends McTunnel> constructor = cl.getConstructor();
            McTunnel tunnel = constructor.newInstance();
            tunnel.initTunnel();
        } catch (Exception e) {
            e.printStackTrace();
            throw new TunnelInitiateException(cl);
        }
    }

    private static Boolean isTunnelClass(Class<?> cl) {
        return McTunnel.class.isAssignableFrom(cl) && cl.isAnnotationPresent(Tunnel.class);
    }

    private static List<String> getTunnelPaths(ClassLoader mod) {
        TunnelJson tunnelJson;
        InputStream stream = mod.getResourceAsStream(JSON_PATH);
        if (stream == null) {
            throw ConfigAccessException.of(JSON_PATH);
        }
        InputStreamReader reader = new InputStreamReader(stream);
        Gson gson = new Gson();
        tunnelJson = gson.fromJson(reader, TunnelJson.class);
        try {
            stream.close();
        } catch (IOException e) {
            throw ConfigAccessException.of(JSON_PATH);
        }
        return tunnelJson.getTunnels();
    }
}
