package staryhroft.templog.exception.message;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

public final class DatabaseErrorMessageBuilder {
    private DatabaseErrorMessageBuilder(){}

    public static String builderMessage(DataAccessException ex){
        if (ex instanceof CannotGetJdbcConnectionException){
            return "Не удалось подключиться к базе данных. Проверьте соединение.";
        } else if (ex instanceof DataIntegrityViolationException){
            return "Нарушение целостности данных. Возможно, запись уже существует.";
        } else {
            return "Ошибка базы данных. Попробуйте позже.";
        }
    }
}
