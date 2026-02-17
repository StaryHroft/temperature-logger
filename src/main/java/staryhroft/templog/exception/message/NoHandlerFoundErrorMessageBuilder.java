package staryhroft.templog.exception.message;

import org.springframework.web.servlet.NoHandlerFoundException;

public final class NoHandlerFoundErrorMessageBuilder {
    private NoHandlerFoundErrorMessageBuilder(){}

    public static String build(NoHandlerFoundException ex){
        return String.format("Ресурс %s %s не найден", ex.getHttpMethod(), ex.getRequestURL());
    }
}
