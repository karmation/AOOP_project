@echo off
if exist "C:\Users\gauta\.maven\maven-3.9.15\bin\mvn.cmd" (
    "C:\Users\gauta\.maven\maven-3.9.15\bin\mvn.cmd" %*
) else (
    mvn %*
)
