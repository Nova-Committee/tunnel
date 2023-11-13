package cool.muyucloud.tunnel.exception;

public class ConfigAccessException extends RuntimeException {
    ConfigAccessException(String message) {
        super(message);
    }

    public static ConfigAccessException of(String path) {
        String message = "Can not access config file %s".formatted(path);
        return new ConfigAccessException(message);
    }
}
