package com.djio.grover_hospital.notification.sendchamp;

import com.djio.grover_hospital.notification.core.NotificationProviderException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/** Normalizes supported Nigerian mobile formats to the E.164 digits required by Sendchamp. */
@Component
public class SendchampPhoneNumberNormalizer {

    private static final Pattern LOCAL_NIGERIAN_MOBILE = Pattern.compile("0[789]\\d{9}");
    private static final Pattern INTERNATIONAL_NIGERIAN_MOBILE = Pattern.compile("234[789]\\d{9}");
    private static final Pattern ALLOWED_FORMATTING = Pattern.compile("[\\s()\\-]");

    public String normalize(String rawPhoneNumber) {
        if (rawPhoneNumber == null || rawPhoneNumber.isBlank()) {
            throw invalidNumber();
        }

        String compact = ALLOWED_FORMATTING.matcher(rawPhoneNumber.trim()).replaceAll("");
        if (compact.startsWith("+")) {
            compact = compact.substring(1);
        }

        if (LOCAL_NIGERIAN_MOBILE.matcher(compact).matches()) {
            return "234" + compact.substring(1);
        }
        if (INTERNATIONAL_NIGERIAN_MOBILE.matcher(compact).matches()) {
            return compact;
        }
        throw invalidNumber();
    }

    private NotificationProviderException invalidNumber() {
        return new NotificationProviderException(
                "sendchamp",
                NotificationProviderException.Reason.VALIDATION,
                "Sendchamp SMS requires a valid Nigerian mobile number in local or international format"
        );
    }
}
