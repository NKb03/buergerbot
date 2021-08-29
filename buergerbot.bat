:locate_javafx_sdk
get /p j=Do you have the JavaFX SDK installed? ([y]es/[n]o):
if %j%=="y" (
    cd %HOMEDRIVE%%HOMEPATH%\.javafx
    bitsadmin /transfer "Download JavaFX" /download https://gluonhq.com/download/javafx-11-0-2-sdk-windows javafx.zip
    bitsadmin /transfer "Download 7z" /download https://www.7-zip.org/a/7z1900-x64.exe 7z.exe
    7z.exe x javafx.zip
    rm 7z.exe
    rm javafx.zip
    set JAVAFX_SDK=%HOMEDRIVE%%HOMEPATH%/.javafx/javafx-sdk-11.0.2
) else (
    get /p JAVAFX_SDK=Where is your JavaFX SDK installed?
    if NOT EXIST %JAVAFX_SDK%\lib\javafx.controls.jar (
        echo "JavaFX SDK is not recognized."
    )
)
EXIT /B 0

if %JAVAFX_SDK%=="%JAVAFX_SDK%" (
    call :locate_javafx_sdk
) else (
    if NOT EXIST %JAVAFX_SDK%\lib\javafx.controls.jar (
        echo "Your JavaFX SDK is not recognized."
        call :locate_javafx_sdk
    )
)

java --module-path "%JAVAFX_SDK%\lib" --add-modules javafx.controls --add-modules javafx.web --add-opens javafx.web/com.sun.webkit.dom=ALL-UNNAMED -jar buergerbot.jar
