package cool.muyucloud.tunnel.exception;

import cool.muyucloud.tunnel.TunnelInitializable;

public class TunnelInitiateException extends RuntimeException {
    public TunnelInitiateException(Class<? extends TunnelInitializable> cl) {
        super("Errors occurred during invoking initTunnel() in %s".formatted(cl.getName()));
    }
}
