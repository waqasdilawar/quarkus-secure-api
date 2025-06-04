# Access Token
```
export access_token=$(curl --location 'http://localhost:8180/realms/devgurupk/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'client_id=secured-api' \
--data-urlencode 'client_secret=TvzMGHjySFk4Nd1jZV0uh1Z8NP8DPiIq' \
--data-urlencode 'grant_type=client_credentials'| jq --raw-output '.access_token' \
 )
```

# RPT

```
export rpt_token=$(curl --location 'http://localhost:8180/realms/devgurupk/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--header "Authorization: Bearer "$access_token \
--data-urlencode 'grant_type=urn:ietf:params:oauth:grant-type:uma-ticket' \
--data-urlencode 'audience=secured-api'| jq --raw-output '.access_token' \
 )
```


# API
```
 curl --location 'http://localhost:8080/hello' --header "Authorization: Bearer "$rpt_token
```