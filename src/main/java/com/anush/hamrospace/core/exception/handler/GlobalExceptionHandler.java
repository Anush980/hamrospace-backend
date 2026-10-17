package com.anush.hamrospace.core.exception.handler;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.anush.hamrospace.common.dto.response.ApiResponse;
import com.anush.hamrospace.common.dto.response.FieldErrorResponse;
import com.anush.hamrospace.core.exception.ErrorCodes;
import com.anush.hamrospace.core.exception.base.AppException;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        // It dynamically preserves the exact message and status you set in your service
        // layer!
        return errorResponse(ex.getStatus(), ex.getCode(), ex.getMessage());
    }

    /**
     * Handles Spring Security's native access restrictions.
     * Uses fully qualified name to avoid confusion with domain exceptions.
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleSpringSecurityAccessDenied(
            org.springframework.security.access.AccessDeniedException ex) {
        return errorResponse(HttpStatus.FORBIDDEN, ErrorCodes.ACCESS_DENIED,
                "You do not have permission to perform this action");
    }

    // ── Validation errors (Missing fields / DTO validation annotations)
    // ────────────────

    /**
     * Handles every bean-validation failure: JSON bodies
     * (MethodArgumentNotValidException) and query-param/model-attribute
     * filters (BindException). Both extend BindException, so a single
     * handler covers @Valid @RequestBody, @Valid filter records and
     * PaginationRequest — otherwise a bad query param would fall through
     * to the catch-all and answer 500 instead of 400.
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(BindException ex) {
        List<FieldErrorResponse> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorResponse(
                        error.getField(),
                        error.getDefaultMessage()))
                .toList();

        // Pass a structural error indicator code back to the frontend
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail("Validation failed", errors, ErrorCodes.VALIDATION_FAILED));
    }

    // ── Framework Routing Failures
    // ──────────────────────────────────────────────────

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NoResourceFoundException ex) {
        return errorResponse(HttpStatus.NOT_FOUND, ErrorCodes.RESOURCE_NOT_FOUND,
                "The requested resource was not found: " + ex.getResourcePath());
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiResponse<Void>>  handlePropertyReferenceException(PropertyReferenceException ex) {
 

         return errorResponse(HttpStatus.BAD_REQUEST, ErrorCodes.BAD_REQUEST,
               " Invalid sort or filter parameter provided");
    }

    /**
     * Catches when a client passes typo fields, unknown keys, or structurally
     * invalid JSON.
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex) {
        // Check if the underlying cause is a field mapping typo error
        if (ex.getCause() instanceof com.fasterxml.jackson.databind.exc.PropertyBindingException jacksonException) {
            String typoField = jacksonException.getPropertyName();
            log.warn("Client submitted an invalid field typo: [{}]", typoField);
            String message = String.format("The field '%s' is not recognized by this endpoint", typoField);
            return errorResponse(HttpStatus.BAD_REQUEST, ErrorCodes.BAD_REQUEST, message);
        }

        // Catch general unparseable JSON structures (e.g., missing commas, brackets)
        log.warn("Malformed JSON request frame payload structural anomaly: {}", ex.getMessage());
        return errorResponse(HttpStatus.BAD_REQUEST, ErrorCodes.BAD_REQUEST,
                "Malformed JSON body syntax or invalid input type formatting.");
    }

    @ExceptionHandler(org.springframework.web.bind.MissingPathVariableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingPathVariable(
            org.springframework.web.bind.MissingPathVariableException ex) {
        String message = String.format("Required path variable '%s' is missing from the request URL.",
                ex.getVariableName());
        return errorResponse(HttpStatus.BAD_REQUEST, ErrorCodes.BAD_REQUEST, message);
    }

    // ── Missing Query/Multipart Parameters (e.g., missing @RequestParam("images"))
    // ──

    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingRequestParam(
            org.springframework.web.bind.MissingServletRequestParameterException ex) {
        String message = String.format("Required parameter '%s' is missing.", ex.getParameterName());
        return errorResponse(HttpStatus.BAD_REQUEST, ErrorCodes.BAD_REQUEST, message);
    }

    @ExceptionHandler(org.springframework.web.multipart.support.MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingRequestPart(
            org.springframework.web.multipart.support.MissingServletRequestPartException ex) {
        String message = String.format("Required request part '%s' is missing from the multipart payload.",
                ex.getRequestPartName());
        return errorResponse(HttpStatus.BAD_REQUEST, ErrorCodes.BAD_REQUEST, message);
    }

    // ── File Size Exceeded (e.g., uploading a 50MB image)
    // ─────────────────────────

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceeded(
            org.springframework.web.multipart.MaxUploadSizeExceededException ex) {
        return errorResponse(HttpStatus.CONTENT_TOO_LARGE, ErrorCodes.BAD_REQUEST,
                "Uploaded file exceeds the maximum allowed size limit.");
    }

    // ── Wrong HTTP Method (e.g., POST instead of PUT)
    // ──────────────────────────────

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            org.springframework.web.HttpRequestMethodNotSupportedException ex) {
        String message = String.format("HTTP method '%s' is not supported for this endpoint. Supported methods: %s",
                ex.getMethod(), ex.getSupportedHttpMethods());
        return errorResponse(HttpStatus.METHOD_NOT_ALLOWED, ErrorCodes.BAD_REQUEST, message);
    }

    // ── Type Mismatch (e.g., sending 'abc' for @PathVariable Long productId)
    // ──────

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {

        Class<?> requiredType = ex.getRequiredType();
        String typeName = (requiredType != null) ? requiredType.getSimpleName() : "valid type";

        String message = String.format("Parameter '%s' should be of type '%s'", ex.getName(), typeName);
        return errorResponse(HttpStatus.BAD_REQUEST, ErrorCodes.BAD_REQUEST, message);
    }

    //  Unsupported Media Type (e.g., sending JSON instead of Multipart) ─────────

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        String supportedTypes = ex.getSupportedMediaTypes().stream()
                .map(Object::toString)
                .reduce((a, b) -> a + ", " + b)
                .orElse("multipart/form-data");

        String message = String.format("Content-Type '%s' is not supported. Expected media type: [%s]",
                ex.getContentType(), supportedTypes);

        return errorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorCodes.UNSUPPORTED_MEDIA_TYPE, message);
    }

    //  Invalid Multipart Request Frame ──────────────────────────────────────────

    @ExceptionHandler(org.springframework.web.multipart.MultipartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMultipartException(org.springframework.web.multipart.MultipartException ex) {
        log.warn("Invalid multipart request parsing attempt: {}", ex.getMessage());
        return errorResponse(HttpStatus.BAD_REQUEST, ErrorCodes.BAD_REQUEST,
                "Current request is not a valid multipart request. Ensure Content-Type is 'multipart/form-data'.");
    }

    
    // ── Catch-all for unexpected crashes (NullPointer, Database down, etc.)
    // ─────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception ex) {
        // Essential: Keep logging the stack trace so you can debug later!
        log.error("Unhandled infrastructure exception: ", ex);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.INTERNAL_ERROR,
                "An unexpected error occurred. Please try again");
    }

    // ── Reusable Helper
    // ───────────────────────────────────────────────────────────

    private ResponseEntity<ApiResponse<Void>> errorResponse(HttpStatusCode status, ErrorCodes code, String message) {
        return ResponseEntity.status(status).body(ApiResponse.fail(message, code));
    }
}