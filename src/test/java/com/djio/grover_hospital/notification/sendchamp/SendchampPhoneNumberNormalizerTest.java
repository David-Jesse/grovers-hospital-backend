package com.djio.grover_hospital.notification.sendchamp;

import com.djio.grover_hospital.notification.core.NotificationProviderException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SendchampPhoneNumberNormalizerTest {

    private final SendchampPhoneNumberNormalizer normalizer = new SendchampPhoneNumberNormalizer();

    @Test
    void convertsSupportedNigerianMobileFormatsToDigitsOnlyInternationalForm() {
        assertThat(normalizer.normalize("08012345678")).isEqualTo("2348012345678");
        assertThat(normalizer.normalize("2348012345678")).isEqualTo("2348012345678");
        assertThat(normalizer.normalize("+234 801-234-5678")).isEqualTo("2348012345678");
        assertThat(normalizer.normalize("07012345678")).isEqualTo("2347012345678");
        assertThat(normalizer.normalize("09012345678")).isEqualTo("2349012345678");
    }

    @Test
    void rejectsNonNigerianMalformedAndNonMobileValues() {
        for (String invalid : new String[]{null, "", "234801234567", "080123456789", "+447700900123", "2346012345678", "0801abc5678"}) {
            assertThatThrownBy(() -> normalizer.normalize(invalid))
                    .isInstanceOfSatisfying(NotificationProviderException.class,
                            error -> assertThat(error.getReason()).isEqualTo(NotificationProviderException.Reason.VALIDATION));
        }
    }
}
