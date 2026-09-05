# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: auth-flow.spec.ts >> Auth and User Management E2E Flow >> TC-E2E-002: Regular user (ROLE_USER) is blocked when directly accessing /admin and redirected to /dashboard
- Location: e2e\auth-flow.spec.ts:41:3

# Error details

```
Error: browserType.launch: Executable doesn't exist at C:\Users\Harry\AppData\Local\ms-playwright\chromium_headless_shell-1243\chrome-headless-shell-win64\chrome-headless-shell.exe
╔════════════════════════════════════════════════════════════╗
║ Looks like Playwright was just installed or updated.       ║
║ Please run the following command to download new browsers: ║
║                                                            ║
║     npx playwright install                                 ║
║                                                            ║
║ <3 Playwright Team                                         ║
╚════════════════════════════════════════════════════════════╝
```