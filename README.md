## Spring boot starter для логирования HTTP запросов  
  
Стартер предоставляет возможность логировать входящие и исходящие HTTP-запросы в вашем приложении на базе Spring Boot.  
Исходящие запросы логируются только при отправке с помощью RestClient, когда он заинжекчен.  
Если при исходящем запросе сервис недоступен, возвращается код ошибки 503 и текст: "Ошибка при выполнении исходящего запроса".  
### Стартер имеет следующие свойства:
1. `http.logging.enabled` - включает/выключает использование стартера.  
Допустимые значения (`true/false`).  
Значение по умолчанию - `false`.  
При значении `false` остальные свойства неактивны.  
При значении отличном от допустимого происходит ошибка при запуске.  
  

2. `http.logging.format` - формат вывода логов.  
Допустимые значения (`text/json`).  
Значение по умолчанию - `text`.  
При значении отличном от допустимого логи не выводятся.  
  

3. `http.logging.level` - уровень логирования.  
Учитывается при текстовом `http.logging.format=text` формате.  
Допустимые значения (`INFO/DEBUG`).  
Значение по умолчанию - `INFO`.  
При значении отличном от допустимого логи не выводятся.  
  
### Демонстрация  
  
Для проверки задания сделан тестовый сервис. Чтобы его запустить надо:  
1. Перейти в директорию demonstration:     
`cd .\demonstration\`  
2. Запустить docker compose:  
`docker-compose up`  
  
Запускается два контейнера, оба с тестовыми сервисами.  
1. У сервиса по адресу `http://localhost:8080` включено логирование. (Первый сервис)  
2. У сервиса по адресу `http://localhost:8081` отключено логирование. (Второй сервис)  
  
Тестовый сервиc имеет следующий API:  
1. `/test` - возвращает число типа long.  
2. `/client` - делает запрос с помощью RestClient на адрес, указанный в свойстве `client.uri`.  

У первого сервиса в свойстве `client.uri` указана ссылка на второй. 
Это сделано, чтобы показать обработку исходящего запроса.  
  
Чтобы изменить режим работы стартера, необходимо поменять переменные среды в `demonstration/compose.yaml`,  
переменные соответствуют свойствам стартера (см. выше):  
1. `HTTP_LOGGING_ENABLED` - включает/выключает использование стартера. (`true/false`)  
2. `HTTP_LOGGING_FORMAT` - формат вывода логов. (`text/json`)  
3. `HTTP_LOGGING_LEVEL` - уровень логирования. (`INFO/DEBUG`)  
  
---
## Руководство по подключению  
Для использования стартера в вашем приложении spring boot необходимо:
1. Скачать стартер:  
`https://github.com/Prohkit/http-logging-spring-boot-starter.git`  
  

2. Выполнить следующую команду в консоли (в idea можно дважды нажать на ctrl),  
чтобы добавить стартер в локальный maven репозиторий.  
`mvn clean install`  
  

3. Добавить зависимость в pom.xml:
```
<dependency>
   <groupId>com.example</groupId>
   <artifactId>http-logging-spring-boot-starter</artifactId>
   <version>0.0.1-SNAPSHOT</version>
</dependency>
```
  

Или для gradle:  
`implementation("com.example:http-logging-spring-boot-starter:0.0.1-SNAPSHOT")`  

Также для gradle нужно, чтобы в repositories был указан mavenLocal.  
```text
repositories {
    mavenLocal()
}
```
  
4. Для включения логирования в application.properties указать свойство:
`http.logging.enabled=true`  
  
---
## Формат логов  
1. При формате - **text**:  
   - при уровне логирования - **INFO**:  
  
![info.jpg](img/info.jpg)  
  
   - при логировании **исходящего запроса**:  
  
![info out.jpg](img/info%20out.jpg)   
  
   - при уровне логирования - **DEBUG**:  
  
![debug.jpg](img/debug.jpg)  
  
2. При формате - **json**:  
  
![json.jpg](img/json.jpg)  
  
Пример **json**:  
```json
{
  "httpMethod": "GET",
  "url": "http://localhost:8080/stats/time/test/all",
  "requestHeaders": [
    {
      "headerName": "host",
      "headerValue": "localhost:8080"
    }
  ],
  "responseStatus": "200",
  "executionTime": 23,
  "responseHeaders": [
    {
      "headerName": "Content-Type",
      "headerValue": "application/json"
    }
  ],
  "className": "TimeTrackStatisticController",
  "methodName": "getAllTimeByClassNameByInterval"
}
```