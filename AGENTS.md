# Repository Guidelines

## Project Structure & Module Organization
- `composeApp/src/commonMain/kotlin`: shared Kotlin/Compose code for all targets.
- `composeApp/src/androidMain/kotlin`: Android-specific implementations.
- `composeApp/src/iosMain/kotlin`: iOS-specific implementations.
- `iosApp/iosApp`: Xcode iOS entry point and any SwiftUI code.
- `build/` and `composeApp/build/`: generated outputs (do not edit).

## Build, Test, and Development Commands
- Android debug build (Windows): `.\gradlew.bat :composeApp:assembleDebug`
- Android debug build (macOS/Linux): `./gradlew :composeApp:assembleDebug`
- iOS app: open `iosApp` in Xcode and run the target from there.
- Tests: there are no automated tests committed yet. When you add tests under
  `composeApp/src/commonTest/kotlin`, run them with `./gradlew :composeApp:allTests`.

## Coding Style & Naming Conventions
- Indentation: 4 spaces; keep line length reasonable for Compose readability.
- Kotlin naming: `PascalCase` for classes/objects, `camelCase` for functions/vars,
  `UPPER_SNAKE_CASE` for constants.
- Resource naming (if added under Android resources): `lower_snake_case`.
- No formatting or linting tools are configured; follow standard Kotlin/Compose
  conventions and keep imports organized.

## Testing Guidelines
- Frameworks: `kotlin.test` (already in `commonTest` dependencies).
- Naming: `*Test.kt` for test files and `shouldXxx`/`whenXxx` for test cases.
- Prefer shared tests in `commonTest`; use platform tests only when needed.

## Commit & Pull Request Guidelines
- Commit messages in history are short, sentence-case imperatives (e.g., “Add …”, “Fix …”),
  sometimes in Portuguese. Follow the same style and include a clear scope.
- PRs should include: a concise description, linked issues (if any), and screenshots or
  screen recordings for UI changes.
- If you touch Firebase setup or signing, mention it explicitly and reference
  `FIREBASE_SETUP.md`.

## Security & Configuration Tips
- Do not commit keystores or secrets. Use `local.properties` or environment variables for
  signing keys (`RELEASE_KEYSTORE_FILE`, `RELEASE_KEY_ALIAS`, etc.).
- Keep `google-services.json` aligned with the correct Firebase project.
