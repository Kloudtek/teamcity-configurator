# teamcity-configurator

Application used for unattended configuration of teamcity.

Usage: java -jar original-teamcity-configurator-[version].jar [options]

  Options:
    -ht, --http-timeout
       HTTP connection timeout
       Default: 10000
    -wt, --wait-timeout
       How long to wait for server to be ready
       Default: 180000
  * -n
       Username for the admin account to be created on teamcity
  * -p
       Password for the admin account to be created on teamcity
  * -u
       Server URL

Example:

```
java -jar original-teamcity-configurator-1.0-beta1.jar -n http://teamcityserver/ -n admin -p verysecretadminpassword
```
