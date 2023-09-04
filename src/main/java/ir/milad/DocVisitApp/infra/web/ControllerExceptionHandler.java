package ir.milad.DocVisitApp.infra.web;

import ir.milad.DocVisitApp.domain.ApplicationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String ERROR_500_ATTRIBUTE_NAME = "errorMessage";
    public static final String DEFAULT_ERROR_VIEW = "500 :: error";

    private static final Logger logger = LoggerFactory.getLogger(ControllerExceptionHandler.class);
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = ApplicationException.class)
    public ModelAndView defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        logger.error("Couldnt handle request: {}, exception:", req, e);
        // If the exception is annotated with @ResponseStatus rethrow it and let
        // the framework handle it
        // Otherwise setup and send the user to a default error-view.
        ModelAndView mav = new ModelAndView();
        mav.addObject(ERROR_500_ATTRIBUTE_NAME, e.getMessage());
        mav.setViewName(DEFAULT_ERROR_VIEW);
        return mav;
    }
}
