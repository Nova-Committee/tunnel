package cool.muyucloud.tunnel.data;

import java.util.List;

public class TunnelJson {
    public List<String> getTunnels() {
        return tunnels;
    }

    public void setTunnels(List<String> tunnels) {
        this.tunnels = tunnels;
    }

    private List<String> tunnels;
}
