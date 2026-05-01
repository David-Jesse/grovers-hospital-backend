package com.djio.grover_hospital.service.notification.channel;


import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * WhatsApp messages have specific constraints:
 *  - For business-initiated messages outside a 24-hour customer service window,
 *    a pre-approved template must be used.
 *  - templateName + templateParams support that flow.
 *  - For replies within the 24h window, plain text via `text` works.
 */

@Data
@Builder
public class WhatsappMessage {

    private String toPhoneNumber;

    /** Plain text for replies within the 24h customer service window */
    private String text;

    /** Pre-approved template name (required for business-initiated outbound) */
    private String templateName;

    /** Ordered values to substitute into the template's placeholders. */
    private List<String> templateParams;
}
