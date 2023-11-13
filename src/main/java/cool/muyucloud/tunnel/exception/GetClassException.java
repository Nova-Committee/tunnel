package cool.muyucloud.tunnel.exception;

public class GetClassException extends RuntimeException {
    public GetClassException(String path) {
        super("Failed to get classes in package %s by loader %s".formatted(path));
    }
}
