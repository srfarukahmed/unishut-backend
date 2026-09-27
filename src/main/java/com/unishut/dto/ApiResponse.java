package com.unishut.dto;

/**
 * Generic response wrapper used by the uniShut REST API.
 *
 * Example success response:
 *
 * {
 *     "success": true,
 *     "message": "Operation completed successfully",
 *     "data": { ... }
 * }
 *
 * Example error response:
 *
 * {
 *     "success": false,
 *     "message": "User not found",
 *     "data": null
 * }
 *
 * @param <T> type of the response data
 */
public class ApiResponse<T> {

    /**
     * Indicates whether the request was successful.
     */
    private boolean success;

    /**
     * Human-readable response message.
     */
    private String message;

    /**
     * Actual response payload.
     */
    private T data;


    /**
     * Required by Jackson.
     */
    public ApiResponse() {
    }


    /**
     * Complete constructor.
     */
    public ApiResponse(
            boolean success,
            String message,
            T data
    ) {
        this.success = success;
        this.message = message;
        this.data = data;
    }


    /**
     * Creates a successful response with data.
     */
    public static <T> ApiResponse<T> success(
            String message,
            T data
    ) {
        return new ApiResponse<>(
                true,
                message,
                data
        );
    }


    /**
     * Creates a successful response without data.
     */
    public static <T> ApiResponse<T> success(
            String message
    ) {
        return new ApiResponse<>(
                true,
                message,
                null
        );
    }


    /**
     * Creates an unsuccessful response.
     */
    public static <T> ApiResponse<T> error(
            String message
    ) {
        return new ApiResponse<>(
                false,
                message,
                null
        );
    }


    public boolean isSuccess() {
        return success;
    }


    public void setSuccess(boolean success) {
        this.success = success;
    }


    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }


    public T getData() {
        return data;
    }


    public void setData(T data) {
        this.data = data;
    }
}