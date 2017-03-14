Short link service 
===============================

####DB: Redis
####DB client:lettuce
####WEB: Spring MVC + Spring AOP 
####Other:zxing QR code, GeoIP DB
####Front - Bootstrap+JQuery

### <a href="http://app.whydt.ru/49193/linkss" target=_blank>Демо разрабатываемого приложения</a>


####REDIS INSTANCE 1
#####redis:0 WORK_DB
- hset key:_visits
    - field:shortLink
    - value:visitsActual
- hset key:_visits_by_domain_actual 
    - field:domain
    - value:visitsActual
- hset key:_visits_by_domain_history 
    - field:domain
    - value:visitsActual
- hset key:_preferences
    - field:pref_name       
    - value:value
- hset key:_users
    - field:userName
    - value:json User
- hset key:userName
    - field:shortLink
    - value:link

#####redis:1 FREELINK_DB 
- key:shortLink

#####redis:2 LINK_DB 
- key:shortLink
- value:{"link":link, "location":{creator location}}

#####redis:3 ARCHIVE_LINK_DB 
- hset key:userName
    - field:shortLink
    - value:{"key":shortLink,"link":link,"visits":visits,"del_date":deleted_date}