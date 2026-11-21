# T34 – Programování pro mobilní platformy (PMP)

Team T34 project assignments for the PMP course at VŠPJ (Moodle course PMP-502047).

Team: Kevin Mikeš, Filip Dusil, Petr Šmíd · Teacher: Ing. Marek Musil

## Structure

Each assignment lives in its own folder `ukol-xx` (as required for submission):

| Folder | Assignment | Contents |
|---|---|---|
| `ukol-01` | #1 – BMI calculator | Android Studio project `BmiKalkulacka`, screenshots `image01…09.png`, `dokumentace-01.docx` |

## Build & test (ukol-01)

Open `ukol-01/BmiKalkulacka` in Android Studio, or from the command line:

```sh
cd ukol-01/BmiKalkulacka
./gradlew testDebugUnitTest            # local unit tests
./gradlew connectedDebugAndroidTest    # Espresso UI tests (running emulator/device needed)
./gradlew lintDebug                    # Android Lint
```

## Submission

Moodle expects `projekt-PMP-T34.zip` containing the `T34` folder with `ukol-xx` subfolders
(source code without build outputs, screenshots, documentation).

## Workflow

Work on a feature branch and open a pull request into `main`.
