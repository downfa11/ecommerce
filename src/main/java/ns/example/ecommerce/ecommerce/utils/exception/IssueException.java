package ns.example.ecommerce.ecommerce.utils.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ns.example.ecommerce.ecommerce.domain.enums.ErrorCode;

@Getter
public class IssueException extends RuntimeException{
    private final ErrorCode errorCode;
    private final String message;

    public IssueException(final ErrorCode errorCode){
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }

    public IssueException(final ErrorCode errorCode, final String message){
        this.errorCode = errorCode;
        this.message = errorCode + " " + message;
    }

    @Override
    public String getMessage() {
        return "[%s] %s".formatted(errorCode, message);
    }
}
