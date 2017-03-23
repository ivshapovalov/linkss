Short link service 
===============================

####DB: Redis
####DB client:lettuce
####WEB: Spring MVC + Spring AOP 
####Other:zxing QR code, GeoIP DB, Google reCaptcha, Yandex Maps
####Front - Bootstrap+JQuery


First check the constants

####REDIS INSTANCE 1
#####redis:0 WORK_DB
- hset key:_visits
    - field: String shortLink
    - value: long visitsActual
- hset key:_visits_by_domain_actual 
    - field: String domain
    - value: long visitsActual
- hset key:_visits_by_domain_history 
    - field: String domain
    - value: long visitsActual
- hset key:_preferences
    - field: String pref_name       
    - value: String value
- hset key:_users
    - field: String userName
    - value: json User user
- hset key:_users_unverified
    - field: String UUID
    - value: String userName    
- hset key:userName
    - field: String shortLink
    - value: String link

#####redis:1 FREELINK_DB 
- key: String shortLink

#####redis:2 LINK_DB 
- key: String shortLink
- value: String link

#####redis:3 ARCHIVE_LINK_DB 
- hset key:userName
    - field: String shortLink
    - value: json FullLink link

#####redis:4 VISITS_DB
- hset key:shortLink
    - field: long time
    - value: json Visit visit
    
#####redis:5 VISITS_BY_DOMAIN_ACTUAL_DB
- hset key:domain
    - field: long time
    - value: json Visit visit
    
#####redis:6 VISITS_BY_DOMAIN_HISTORY_DB
- hset key:domain
    - field: long time
    - value: json Visit visit    