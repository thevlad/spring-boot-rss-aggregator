package org.thevlad.rss.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ControllerAdvice
@RequestMapping(value = "/errors", produces = MediaType.APPLICATION_JSON_VALUE)
public class ErrorController {

	private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);
	
	@RequestMapping
	@ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
	public Map<String, Object> handle(HttpServletRequest request, Throwable ex) {
		String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
		if (requestUri == null) {
			requestUri = "Unknown";
		}

		logger.error("Unhandled Error on URL: " + requestUri, ex);
		return ResponseMap.mapError(ex.getMessage());
		
	}
	

	
	@ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
	public Map<String, Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<ObjectError> globalErrors = ex.getBindingResult().getGlobalErrors();
        
        Map<String, List<String>> validationErrors = new HashMap<String, List<String>>();
        for (FieldError fieldError : fieldErrors) {
        	List<String> fieldErrorList = null;
            String field = fieldError.getField();
            String fieldErrorMsg = fieldError.getDefaultMessage();
            if (validationErrors.containsKey(field)) {
            	fieldErrorList = validationErrors.get(field);
            } else {
            	fieldErrorList = new ArrayList<String>();
            	validationErrors.put(field, fieldErrorList);
            }
            fieldErrorList.add(fieldErrorMsg);
        }
        for (ObjectError objectError : globalErrors) {
        	List<String> objErrorList = null;
            String objName = objectError.getObjectName();
            String objErrorMsg = objectError.getDefaultMessage();
            if (validationErrors.containsKey(objName)) {
            	objErrorList = validationErrors.get(objName);
            } else {
            	objErrorList = new ArrayList<String>();
            	validationErrors.put(objName, objErrorList);
            }
            objErrorList.add(objErrorMsg);
        }
        logger.error("[400 Validation Errors: ]", ex);
		return ResponseMap.mapError(validationErrors);
    }
	
	
}
