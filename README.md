Viber bot product shop 
To install 
Up your mysql db with login: root password:root or change application properties file 
With docker :
docker run --name viber_bot -d -e MYSQL_DATABASE=eating_service -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 mysql:5.6
Set your bot token in variable yourBotToken in Application.class
Set your server url in variable yourServerUrl  if you run this on your PC then use NgRock command 
ngrok http 8080 (if you have already installed this)
