# leiriPosti

JavaFX desktop app for managing and printing messages, with Google Sheets as the backing store. It includes a simple UI, printer selection, compact logging via Log4j2, and a small event bus to decouple background tasks from the UI.

## Features

- JavaFX UI (FXML) with controllers and a separate printer dialog
- Printing via Java Print Service (A4 layout; supports default printer selection)
- Google Sheets integration for reading message data
- Log4j2 logging (compact console pattern) + optional in-app console view
- Simple event bus and custom events for decoupled updates
- Session/profile persistence and OAuth token caching

## Requirements

- JDK 21 (configured via Gradle toolchain)
- Internet access for Google APIs
- A Google Cloud project with the Google Sheets API enabled
- OAuth client credentials (credentials.json)

## Project layout

- `app/` — Gradle subproject with sources and resources
  - `src/main/java/` — application code (JavaFX app entry: `tel.kontra.leiriposti.App`)
  - `src/main/resources/` — FXML, logging config, and app configs
    - `main.fxml`, `printer.fxml`, `log4j2.xml`, `leiriposti.properties`, `credentials.json`
- `tokens/StoredCredential` — created on first successful OAuth flow
- `session_profiles/` — session/profile persistence (e.g., `default_session.ser`)

## Configuration

Place your Google OAuth client file as `app/src/main/resources/credentials.json` (or update the app config to point to your file). On first run, you will be prompted to authorize; the refresh token is stored under `tokens/StoredCredential`.

The file `app/src/main/resources/leiriposti.properties` contains application settings. Common keys:

- `spreadsheetId` — The Google Sheets spreadsheet ID to read messages from. If missing, the app guides you to set it on first run.

Logging is configured by `app/src/main/resources/log4j2.xml`. The console pattern is short for better readability; you can adjust the level or pattern there.

## Build and run

The project uses Gradle with the Application and JavaFX plugins. Commands below assume running from the repository root.

- Windows (PowerShell):
  - `./gradlew.bat clean build`
  - `./gradlew.bat run` (or `./gradlew.bat :app:run`)
- macOS/Linux:
  - `./gradlew clean build`
  - `./gradlew run` (or `./gradlew :app:run`)

If you change Google API dependencies, consider a clean rebuild to avoid cached conflicts: `clean build`.

## Printing notes

- The app uses the Java Print Service API (`javax.print`) under the hood. A message is rendered to an A4 page and sent as a `Doc` to the selected printer.
- If you get a blank page:
  - Ensure the printable content is drawn within the printer’s imageable area.
  - Try a different printer driver or disable “fit to page”.
  - Check logs for rendering/printing warnings.

## Google API dependencies

Google libraries are declared in `app/build.gradle`. If you encounter `NoSuchMethodError` or similar version conflicts (often with Guava), align the versions of `google-api-client`, `google-oauth-client`, and the Sheets API artifact, then run a clean build.

## Troubleshooting

- JavaFX FXML issues: ensure @FXML fields match the FXML IDs; set a root for `TreeView` to avoid NPEs.
- UI updates from background threads must use `Platform.runLater(..)`.
- If the stage appears null in startup code, ensure the primary `Stage` is set before showing the scene.

## License

See `LICENSE` for details.

