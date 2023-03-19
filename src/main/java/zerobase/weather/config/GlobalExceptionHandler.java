package zerobase.weather.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)       // 500번 에러
    @ExceptionHandler(Exception.class)      // 동작시키기 위해 클래스를 넣어줘야함
    public Exception handleAllException() {
        // 예외처리를 위한 로직을 넣어줌
        System.out.println("Error from GlobalExceptionHandler");
        return new Exception();
    }
}
