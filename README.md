# teamcity-configurator

Application used for unattended configuration of teamcity.

Usage: java -jar original-teamcity-configurator-[version].jar [options]

Example:

```
java -jar original-teamcity-configurator-1.0-beta1.jar -u http://teamcityserver/ -n admin -p verysecretadminpassword
```

### Important note

This tool doesn't configure the database because that can be done using database.properties and maintainDb.sy (note this 
must be done before starting the server).

Exampe:

```
echo >/var/lib/teamcity/.BuildServer/config/database.properties "connectionProperties.user=${DB_USERNAME}"
echo >>/var/lib/teamcity/.BuildServer/config/database.properties "connectionProperties.password=${DB_PASSWORD}"
echo >>/var/lib/teamcity/.BuildServer/config/database.properties "connectionUrl=jdbc\:postgresql\://${DB_ADDRESS}:${DB_PORT}/${DB_NAME}"
echo >>/var/lib/teamcity/.BuildServer/config/internal.properties "teamcity.disable.super.user=true"
/usr/share/teamcity/bin/maintainDB.sh new-db -T /var/lib/teamcity/.BuildServer/config/database.properties --init -A /var/lib/teamcity/.BuildServer
```