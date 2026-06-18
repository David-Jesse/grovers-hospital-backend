package com.djio.grover_hospital.security.ratelimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.security.rate-limit")
public class RateLimitProperties {

    /** Master kill switch — set false to disable rate limiting entirely (e.g. load tests). */
    private boolean enabled = true;

    /** Login attempts per minute per IP (admin + patient). */
    private int loginRequestsPerMinute = 5;

    /** Patient registration attempts per 10 minutes per IP. */
    private int registerRequestsPer10Min = 3;

    /** Password reset requests per 5 minutes per IP. */
    private int passwordResetRequestsPer5Min = 3;

    /** Download link requests per 5 minutes per IP. */
    private int downloadRequestRequestsPer5Min = 5;
}