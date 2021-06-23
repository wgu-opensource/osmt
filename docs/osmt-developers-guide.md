# OSMT Developers Guide

## Overview
The ```Accept``` header is used for content negotiation.  The content types in use generally are: ```application/json```, ```text/html```, and ```text/csv```.

An api call can return ```202 Accepted``` to indicate an asynchrous process is occuring.  It will return a json TaskResult object which has an ```href``` which can be polled to get the final results in the mime type requested in the Accept header.

If an endpoint returns more than a single json object, it will be paginated.  You can use the [RFC 5988](https://tools.ietf.org/html/rfc5988#section-5) ```Link``` header to retrieve the url to access the next or previous pages.

All API Calls are rate limited.  Each response will have the headers ```X-RateLimit-Limit``` and ```X-RateLimit-Remain``` indicating rate limit status.
Unauthenticated API requests (based on IP-Address) are limited to 60 requests/hr.  Authenticated API requests are limited to 5000 requests/hr.
Public Canonical URL endpoints are exempt from rate limits.
## API User Stories

### I want to retrieve all the skills in the library in CSV format.

Make a request to ```/api/skills``` with the Accept header set to "text/csv":
```bash
# curl -H "Accept: text/csv" https://Domain.Name/api/skills
```
Since it may take a while for this CSV to be prepared you will receive a response with http status code 202 Accepted with content-type "application/json".
```json
{
  "href": "/tasks/3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "content-type": "text/csv",
  "status": "Processing"
}
```
Use the ```/api/tasks/{uuid}``` endpoint to check for the results of your task:
```bash
# curl -H https://Domain.Name/api/tasks/3fa85f64-5717-4562-b3fc-2c963f66afa6
```
If the task is not yet complete you will receive http status 202 and the same TaskResult as before with Content-Type: application/json.  
If the results of the task are ready you will receive http status 200 OK with the Content-Type originally requested, in this case "text/csv".

### I want to retrieve all the skills in the library as JSON-LD.

Make a request to ```/api/skills``` with Accept header "application/json":
```bash
# curl -H "Accept: application/json" https://Domain.Name/api/skills
< Link: <https://Domain.Name/api/skills?page=2>; rel="next";
[
{}, {}, ...
]
```
on http status 200 OK You will receive up to the first 100 results on that page.  
If there are more results, the response will include a "Link" header in [RFC 5988](https://tools.ietf.org/html/rfc5988#section-5) format. 
Request the url specified in the Link header with the ```next``` relation until retrieving the last page.

### I have the "Canonical URL" for a skill, and I want to retrieve the JSON-LD.
Make a request to the url with Accept header set to "application/json":

```bash
# curl -H "Accept: application/json" https://Domain.Name/api/skills/3fa85f64-5717-4562-b3fc-2c963f66afa6
```


### I want to retrieve a human readable description of a skill.
Simply request the "Canonical URL" in a browser.  Browsers will default to requesting "\*/*" or "text/html" in the Accept header. 
The API will respond by returning either a http status 200 OK with content-type "text/html" which contains the human readable description, or an http status 302 redirect.



