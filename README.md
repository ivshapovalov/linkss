Short link repo service
===============================

####DB client:lettuce
####WEB:Servlet + Spring web + Spring MVC
####Other:zxing QR code
####DB: Redis


####REDIS DB 1
#####redis:0 WORK_DB
- hset key:_visits
    - field:shortLink
    - value:count
- hset key:_visits_by_domain 
    - field:domain
    - value:count
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

####REDIS DB 2
#####redis:1 LINK_DB 
- key:shortLink
- value:link