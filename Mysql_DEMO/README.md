# date
2026-01-13
## description
this is a MySQL practicing demo, the MySQL Table be like:
```
CREATE TABLE system_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    log_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    log_level VARCHAR(10) NOT NULL,
    source VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    user_id INT NULL,
    extra JSON NULL
);

```
## running
path to this project root folder
paste this in the cmd
```
java -cp "C:\Users\user\Desktop\Mysql_DEMO\bin;C:\Users\user\Desktop\Mysql_DEMO\mysql-connector-j-9.4.0\mysql-connector-j-9.4.0\mysql-connector-j-9.4.0.jar" LogTableSetup
```
## why MySQL
make the data I/O management and efficiency better
