package cool.muyucloud.tunnel.exception;

import cool.muyucloud.tunnel.McTunnel;

public class TunnelInitiateException extends RuntimeException {
    public TunnelInitiateException(Class<? extends McTunnel> cl) {
        super("Errors occurred during invoking initTunnel() in %s".formatted(cl.getName()));
    }
}
