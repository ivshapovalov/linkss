Short link repo service
===============================

####DB: Redis
####DB client:lettuce
####WEB:JSP + Spring MVC
####Other:zxing QR code


####REDIS INSTANCE 1
#####redis:0 WORK_DB
- hset key:_visits
    - field:shortLink
    - value:visits
- hset key:_visits_by_domain 
    - field:domain
    - value:visits
- hset key:_preferences
    - field:pref_name       
    - value:value
- hset key:_users
    - field:userName
    - value:password
- hset key:userName
    - field:shortLink
    - value:link

#####redis:1 FREELINK_DB 
- key:shortLink

####REDIS INSTANCE 2
#####redis:0 LINK_DB 
- key:shortLink
- value:link