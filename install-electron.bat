@echo off
cd electron
start "" /w /b npm install
start "" /w /b gulp init
start "" /w /b gulp