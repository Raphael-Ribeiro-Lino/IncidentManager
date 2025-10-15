package br.com.incidentemanager.helpdesk.handler;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.com.incidentemanager.helpdesk.exceptions.BadRequestBusinessException;
import br.com.incidentemanager.helpdesk.exceptions.NotFoundBusinessException;
import br.com.incidentemanager.helpdesk.exceptions.SafeResponseBusinessException;
import br.com.incidentemanager.helpdesk.exceptions.UnauthorizedAccessBusinessException;


@ControllerAdvice
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler{

	@ExceptionHandler(BadRequestBusinessException.class)
	public final ResponseEntity<ProblemExceptionOutput> handlerBadRequestBussinessException(
			BadRequestBusinessException ex, WebRequest request) {
		ProblemExceptionOutput problema = new ProblemExceptionOutput(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
		return new ResponseEntity<ProblemExceptionOutput>(problema, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NotFoundBusinessException.class)
	public final ResponseEntity<ProblemExceptionOutput> handlerNotFoundBussinessException(NotFoundBusinessException ex,
			WebRequest request) {
		ProblemExceptionOutput problema = new ProblemExceptionOutput(HttpStatus.NOT_FOUND.value(), ex.getMessage());
		return new ResponseEntity<ProblemExceptionOutput>(problema, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(SafeResponseBusinessException.class)
	public ResponseEntity<ProblemExceptionOutput> handleSafeResponseBusinessException(SafeResponseBusinessException ex) {
	    return ResponseEntity.ok(new ProblemExceptionOutput(HttpStatus.OK.value(), ex.getMessage()));
	}

	@ExceptionHandler(UnauthorizedAccessBusinessException.class)
	public final ResponseEntity<ProblemExceptionOutput> handlerNotFoundBussinessException(
			UnauthorizedAccessBusinessException ex, WebRequest request) {
		ProblemExceptionOutput problema = new ProblemExceptionOutput(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
		return new ResponseEntity<ProblemExceptionOutput>(problema, HttpStatus.UNAUTHORIZED);
	}

	@Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		String detail = "Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.";
		BindingResult bindingResult = ex.getBindingResult();
		List<FieldsExceptionOutput> problemObjects = bindingResult.getAllErrors().stream().map(objectError -> {

			String message = objectError.getDefaultMessage();
			String name = objectError.getObjectName();

			if (objectError instanceof FieldError) {
				name = ((FieldError) objectError).getField();
			}

			message = message.replace("{1}", name);
			return FieldsExceptionOutput.builder().name(name).message(message).build();
		}).collect(Collectors.toList());

		ProblemExceptionOutput problema = new ProblemExceptionOutput(HttpStatus.BAD_REQUEST.value(), detail,
				problemObjects);

		return new ResponseEntity<Object>(problema, HttpStatus.BAD_REQUEST);
    }
	
}
