# author:
z.x.l
# contact me:
if you want installer, please contact me: zhoudaniel02@gmail.com
# release date:
I forgot
# version:
1.1
## abstract
this project give a convinence way to share file to another device with python http server and cloudflare quick tunnel, user can choose a folder and copy a webside to share the localhost folder. this project also generate a QR code and let mobile to scan it.
## project architecture
the architecture shows in 程式架構圖.png at root folder, the project have a main exe files to use filesharing file,based on the project own library(jdk-25 but only basic env).
fileSharing is also based on that, and it catch cloudflared's stdIO and server's stdIO and shows on frame, finally it generate QR code by zxing module. the server wrote by python and based on the python runtime env with only core part.
fileSharing will open the server on localhost and get a random port between 8000~9000
suddenly  use http2 protocol and build a quick tunnel.
## resource
the logo is grab on google; this project used cloudflare 2025.11.1, jdk-25 and python 3.14.0
## env
it only work on \*32 architecture windows OS

