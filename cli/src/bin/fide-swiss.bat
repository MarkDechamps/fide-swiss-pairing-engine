@echo off
rem fide-swiss: the FIDE Swiss Pairing Engine command line. Needs Java 25 on the path.
set "HOME_DIR=%~dp0.."
java -p "%HOME_DIR%\lib" -m io.github.markdechamps.fideswiss.cli/io.github.markdechamps.fideswiss.cli.Main %*
