# Security Policy

## Supported Versions

This project is under active development. Security fixes are applied to the latest `master` branch.

## Reporting a Vulnerability

Please do **not** open public issues for security vulnerabilities.

Report vulnerabilities privately via one of the following:

- GitHub private security advisory (preferred)
- Email: `security@example.com` (replace with your real address)

Please include:

- Vulnerability type and impact
- Reproduction steps / PoC
- Affected files, endpoints, or versions
- Suggested remediation (if available)

## Response Timeline

- Initial acknowledgment: within 72 hours
- Triage and severity assessment: within 7 days
- Fix plan and patch timeline: communicated after triage

## Security Best Practices for Contributors

- Never commit secrets (`.env`, keys, tokens, credentials)
- Use `.env.example` for placeholders only
- Rotate exposed credentials immediately
- Run secret scanning before PRs (e.g., `gitleaks`)
