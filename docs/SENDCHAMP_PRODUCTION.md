# SendChamp email production deployment

The application selects SendChamp through Spring Boot environment variables. Do not put the access key in `application.properties`, a deployment YAML file, the Docker image, or the deploy command.

## One-time setup

1. Rotate any SendChamp key that has appeared in a local config dump, terminal capture, or shared file.
2. In the SendChamp live dashboard, verify the sending domain/address and ensure the account can send transactional email.
3. Store the new live key in Google Secret Manager:

   ```powershell
   $sendchampKey = Read-Host "SendChamp live access key" -AsSecureString
   $sendchampKeyText = [System.Net.NetworkCredential]::new('', $sendchampKey).Password
   $sendchampKeyText | gcloud secrets versions add sendchamp-access-key --data-file=- --project scrapper-464819
   Remove-Variable sendchampKey, sendchampKeyText
   ```

   Create the `sendchamp-access-key` secret first if needed, and grant the Cloud Run runtime service account `roles/secretmanager.secretAccessor` for that secret.

## Deploy

This changes only email-provider settings and preserves unrelated Cloud Run configuration:

```powershell
gcloud run deploy grover-hospital --source . --region europe-west2 --project scrapper-464819 --update-env-vars "APP_NOTIFICATION_EMAIL_PROVIDER=sendchamp,APP_NOTIFICATION_SMS_PROVIDER=console,APP_NOTIFICATION_WHATSAPP_PROVIDER=console,APP_NOTIFICATION_SENDCHAMP_BASE_URL=https://api.sendchamp.com/api/v1,APP_NOTIFICATION_SENDCHAMP_INCLUDE_EMAIL_SENDER=false,APP_NOTIFICATION_SENDCHAMP_CONNECT_TIMEOUT_MS=10000,APP_NOTIFICATION_SENDCHAMP_READ_TIMEOUT_MS=30000" --update-secrets "APP_NOTIFICATION_SENDCHAMP_ACCESS_KEY=sendchamp-access-key:latest"
```

The optional sender object is deliberately omitted. Keep `APP_NOTIFICATION_SENDCHAMP_INCLUDE_EMAIL_SENDER=false` unless the live account requires it; if enabled, also set both `APP_NOTIFICATION_SENDCHAMP_EMAIL_SENDER_NAME` and `APP_NOTIFICATION_SENDCHAMP_EMAIL_SENDER_ADDRESS` to a verified identity.

SMTP settings can remain during rollout because the provider selector activates only one `EmailSender`. Keep the SMTP secret until rollback is no longer needed.

## Verify and roll back

After deployment:

1. Confirm the revision is ready and has no configuration or authentication errors.
2. Trigger one low-risk transactional email to an address controlled by the team.
3. Confirm delivery and a successful notification delivery-log entry.
4. Exercise password reset and one booking notification because both contain production links.
5. Confirm logs contain no access keys, message bodies, or recipient addresses.

Rollback does not require a rebuild:

```powershell
gcloud run services update grover-hospital --region europe-west2 --project scrapper-464819 --update-env-vars "APP_NOTIFICATION_EMAIL_PROVIDER=smtp"
```

## Deployment blockers

Do not deploy until:

- the exposed/live-looking SendChamp key has been revoked and replaced;
- `sendchamp-access-key` exists and the runtime service account can access it;
- the SendChamp account is live, funded if required, and its sending identity is verified;
- production frontend/base URLs are configured so password-reset and result links do not point to localhost;
- a controlled-recipient smoke test and rollback owner are agreed.

No database migration is required for this provider change.
