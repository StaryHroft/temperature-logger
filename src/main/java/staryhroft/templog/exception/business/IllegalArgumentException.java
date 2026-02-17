package staryhroft.templog.exception.business;

public class IllegalArgumentException extends RuntimeException {
    public IllegalArgumentException(String message) {

        super("Вводится неверное значение входного параметра." +
                " Проверьте что при вводе название города не может быть пустым или состоять только из пробелов\"\n" +
                "\n" + "\"Название города должно содержать только латинские буквы, пробелы или дефисы\"\n" +
                "\n" + "\"Имя пользователя не может быть длиннее 50 символов\"message");
    }
}
