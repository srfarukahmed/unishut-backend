package com.unishut.dto;

/**
 * Response returned after successful authentication.
 *
 * The response contains:
 *
 * - JWT access token
 * - User information
 *
 * The password is intentionally never included.
 */
public class AuthResponse {

    /**
     * JWT access token.
     */
    private String token;

    /**
     * Token type.
     *
     * Normally:
     * Bearer
     */
    private String tokenType;

    /**
     * Authenticated user's information.
     */
    private UserDTO user;


    /**
     * Required by Jackson.
     */
    public AuthResponse() {
    }


    /**
     * Constructor using the default Bearer token type.
     */
    public AuthResponse(
            String token,
            UserDTO user
    ) {
        this.token = token;
        this.tokenType = "Bearer";
        this.user = user;
    }


    /**
     * Complete constructor.
     */
    public AuthResponse(
            String token,
            String tokenType,
            UserDTO user
    ) {
        this.token = token;
        this.tokenType = tokenType;
        this.user = user;
    }


    public String getToken() {
        return token;
    }


    public void setToken(String token) {
        this.token = token;
    }


    public String getTokenType() {
        return tokenType;
    }


    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }


    public UserDTO getUser() {
        return user;
    }


    public void setUser(UserDTO user) {
        this.user = user;
    }
}