package com.unishut.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Service responsible for creating and validating JWT tokens.
 *
 * UniShut uses JWT-based stateless authentication.
 *
 * JWT flow:
 *
 * Login
 *   ↓
 * Credentials verified
 *   ↓
 * JwtService generates token
 *   ↓
 * Frontend stores token
 *   ↓
 * Frontend sends:
 *
 * Authorization: Bearer <token>
 *
 *   ↓
 * JwtAuthenticationFilter validates token
 *   ↓
 * Spring SecurityContext receives authenticated user
 */
@Service
public class JwtService {

    /**
     * Secret key used for signing JWT tokens.
     *
     * This value must be provided through application.properties
     * or an environment variable.
     *
     * Do NOT commit a production secret key to GitHub.
     */
    @Value("${unishut.jwt.secret}")
    private String secret;


    /**
     * JWT expiration time in milliseconds.
     *
     * Default:
     *
     * 86400000 ms = 24 hours
     */
    @Value("${unishut.jwt.expiration:86400000}")
    private long expiration;


    /**
     * Generate a JWT token for a user.
     *
     * The user's email is stored as the JWT subject.
     *
     * The user's role is stored as a custom claim.
     *
     * @param email user email
     * @param role user role
     * @return signed JWT token
     */
    public String generateToken(
            String email,
            String role
    ) {

        if (email == null || email.isBlank()) {

            throw new IllegalArgumentException(
                    "Email cannot be empty"
            );
        }


        if (role == null || role.isBlank()) {

            throw new IllegalArgumentException(
                    "Role cannot be empty"
            );
        }


        Date issuedAt =
                new Date();


        Date expirationDate =
                new Date(
                        issuedAt.getTime()
                                + expiration
                );


        return Jwts.builder()

                /*
                 * JWT subject.
                 */
                .subject(
                        email.trim()
                )

                /*
                 * User role.
                 */
                .claim(
                        "role",
                        role.trim().toUpperCase()
                )

                /*
                 * Token creation time.
                 */
                .issuedAt(
                        issuedAt
                )

                /*
                 * Token expiration time.
                 */
                .expiration(
                        expirationDate
                )

                /*
                 * Sign the token.
                 */
                .signWith(
                        getSigningKey()
                )

                .compact();
    }


    /**
     * Extract the email/subject from a JWT.
     *
     * @param token JWT token
     * @return email stored as subject
     */
    public String extractUsername(
            String token
    ) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }


    /**
     * Extract the user's role from a JWT.
     *
     * @param token JWT token
     * @return role
     */
    public String extractRole(
            String token
    ) {

        return extractClaim(
                token,
                claims -> claims.get(
                        "role",
                        String.class
                )
        );
    }


    /**
     * Extract token expiration date.
     *
     * @param token JWT token
     * @return expiration date
     */
    public Date extractExpiration(
            String token
    ) {

        return extractClaim(
                token,
                Claims::getExpiration
        );
    }


    /**
     * Extract an arbitrary claim.
     *
     * @param token JWT token
     * @param claimsResolver claim resolver
     * @param <T> claim type
     * @return requested claim
     */
    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {

        Claims claims =
                extractAllClaims(token);


        return claimsResolver.apply(
                claims
        );
    }


    /**
     * Check whether a token has expired.
     *
     * @param token JWT token
     * @return true if expired
     */
    public boolean isTokenExpired(
            String token
    ) {

        Date expirationDate =
                extractExpiration(token);


        return expirationDate.before(
                new Date()
        );
    }


    /**
     * Validate a token against a user's email.
     *
     * @param token JWT token
     * @param email expected email
     * @return true if valid
     */
    public boolean isTokenValid(
            String token,
            String email
    ) {

        try {

            String username =
                    extractUsername(
                            token
                    );


            return username != null
                    && username.equalsIgnoreCase(
                    email
            )
                    && !isTokenExpired(
                    token
            );

        } catch (Exception exception) {

            return false;
        }
    }


    /**
     * Parse all claims from a JWT.
     *
     * Signature validation is performed automatically
     * by the parser.
     *
     * @param token JWT token
     * @return JWT claims
     */
    private Claims extractAllClaims(
            String token
    ) {

        if (token == null
                || token.isBlank()) {

            throw new IllegalArgumentException(
                    "JWT token cannot be empty"
            );
        }


        return Jwts.parser()

                .verifyWith(
                        getSigningKey()
                )

                .build()

                .parseSignedClaims(
                        token
                )

                .getPayload();
    }


    /**
     * Convert the Base64-encoded secret into
     * a cryptographic signing key.
     *
     * JJWT 0.12.x requires a SecretKey for
     * modern signing APIs.
     *
     * @return JWT signing key
     */
    private SecretKey getSigningKey() {

        if (secret == null
                || secret.isBlank()) {

            throw new IllegalStateException(
                    "JWT secret is not configured"
            );
        }


        byte[] keyBytes =
                Decoders.BASE64.decode(
                        secret
                );


        return Keys.hmacShaKeyFor(
                keyBytes
        );
    }
}