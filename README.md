Short link repo service
===============================

DB: Redis
DB client:lettuce
WEB:Servlet + Spring web 
Other:zxing QR code


DB structure:

redis:0 -- hset key:_links
                    key:shortLink       value:link
           hset key:_visits
                    key:shortLink       value:count
           hset key:_visits_by_domain 
                    key:domain          value:count
           hset key:_preferences
                    key:pref            value:value
           hset key:_users
                    key:userName        value:password
           hset key:userName
                    key:shortLink       value:link

redis:1 -- key:shortLink
