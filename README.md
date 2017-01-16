Short link repo service
===============================

####DB client:lettuce
####WEB:Servlet + Spring web + Spring MVC
####Other:zxing QR code
####DB: Redis

#####redis:0 WORK_DB
- hset key:_links
    - field:shortLink
    - value:link
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
