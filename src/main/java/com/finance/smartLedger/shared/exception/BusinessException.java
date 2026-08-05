package com.finance.smartLedger.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

  private final ErrorCodes errorCodes;
  private final HttpStatus httpStatus;

  public BusinessException(ErrorCodes errorCodes) {
    super(errorCodes.getMessage());
    this.errorCodes = errorCodes;
    this.httpStatus = determineHttpStatus(errorCodes);
  }

  public BusinessException(ErrorCodes errorCodes, String customMessage) {
    super(customMessage);
    this.errorCodes = errorCodes;
    this.httpStatus = determineHttpStatus(errorCodes);
  }

  public BusinessException(ErrorCodes errorCodes, Throwable cause) {
    super(errorCodes.getMessage(), cause);
    this.errorCodes = errorCodes;
    this.httpStatus = determineHttpStatus(errorCodes);
  }

  private HttpStatus determineHttpStatus(ErrorCodes errorCodes) {
    String code = errorCodes.getCode();
    if (code.startsWith("ERR-1")) {
      if (code.equals("ERR-1004")) return HttpStatus.UNAUTHORIZED;
      if (code.equals("ERR-1005")) return HttpStatus.FORBIDDEN;
      if (code.equals("ERR-1006")) return HttpStatus.CONFLICT;
      return HttpStatus.BAD_REQUEST;
    } else if (code.startsWith("ERR-2")) {
      return HttpStatus.BAD_REQUEST;
    } else if (code.startsWith("ERR-3")) {
      return HttpStatus.NOT_FOUND;
    } else if (code.startsWith("ERR-4")) {
      return HttpStatus.BAD_REQUEST;
    } else if (code.startsWith("ERR-5")) {
      return HttpStatus.BAD_REQUEST;
    } else if (code.startsWith("ERR-6")) {
      return HttpStatus.BAD_REQUEST;
    } else if (code.startsWith("ERR-7")) {
      return HttpStatus.SERVICE_UNAVAILABLE;
    } else if (code.startsWith("ERR-8")) {
      return HttpStatus.SERVICE_UNAVAILABLE;
    }
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }
}
