package hce.edu.vn.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiError> handleApiException(ApiException exception, HttpServletRequest request) {
		return response(exception.getStatus(), exception.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.map(FieldError::getDefaultMessage)
				.distinct()
				.collect(Collectors.joining("; "));
		return response(HttpStatus.BAD_REQUEST, message, request);
	}

	@ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
	public ResponseEntity<ApiError> handleUnreadableRequest(Exception exception, HttpServletRequest request) {
		return response(HttpStatus.BAD_REQUEST, "Request body hoặc tham số không đúng định dạng.", request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiError> handleDataIntegrity(HttpServletRequest request) {
		return response(HttpStatus.CONFLICT, "Dữ liệu vi phạm ràng buộc liên kết hoặc toàn vẹn.", request);
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ApiError> handleDatabaseFailure(HttpServletRequest request) {
		return response(HttpStatus.SERVICE_UNAVAILABLE,
				"Không thể truy cập cơ sở dữ liệu. Vui lòng thử lại sau.", request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnexpectedFailure(HttpServletRequest request) {
		return response(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi hệ thống.", request);
	}

	private ResponseEntity<ApiError> response(HttpStatus status, String message, HttpServletRequest request) {
		ApiError body = new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message,
				request.getRequestURI());
		return ResponseEntity.status(status).body(body);
	}
}