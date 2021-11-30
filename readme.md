Prometheus queries:
* Requests: *rate(http_server_requests_seconds_count{uri="/service/ping"}[5s])*
* Custom counter: *my_custom_counter_total*