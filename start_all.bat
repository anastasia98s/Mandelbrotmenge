cd master
start start.bat

timeout /t 5 /nobreak >nul

cd ..
cd worker
start start.bat
start start.bat
start start.bat
start start.bat

cd ..
cd client
start start.bat