# Monitoring interface for Redis with PCF

# Foreword
This document details a strategy for ensuring a Redis instance in _cloud foundry_ is running properly.
  - To use the RESTful interface for monitoring a PCF Redis installation, you must download and install the PCF Redis Admin API Proxy.
  - One instance of the Admin API will monitor one Redis Service.


# Overview
This recipe covers two steps for monitoring and discovering the state of your PCF Redis installation. The first step is a simple "is it running" API which simply PINGs the Redis Cluster to verify it is responding. The second section details more complex monitoring which can be used for performance analysis, scaling and statistical analysis.

# Deploy the admin API
Download the application jar file and the manifest.

  * Using the cf CLI, perform the following:
    * cf push redis-api -f manifest.yml -p target/redis-monitor-proxy-0.0.2-SNAPSHOT.jar
    * cf bind-service redis-api myRedisServiceName

Your API will be reachable at https://redis-api.your.cfinstallation.com

# Is my Redis Cluster Running

The simplest approach to verifying a PCF Redis cluster is _happy_ is to execute the ping API

| HTTP Method | API Path | Description |
|----|----|----|
| GET | /admin/ping |Intended for use by monitoring tools. If everything is working correctly, will return HTTP status 200 with a JSON response similar to:  **{"resultCode": "200","resultMessage": "Connection Successful","resultData": {"Ping": "PONG","used_memory": "836048"}}**|

# Detailed Performance Monitoring

The Redis Admin APIs expose many useful features for performance tuning and application monitoring. The sections available to monitor are:

  * Server -- Information about the Server including version, os and uptime.
  * Clients -- The client connections. Useful for finding (and killing) zombie or harmful client connections.
  * Memory -- Memory information including currently used and peak utilization
  * Persistence -- Save status, timeouts and size Information
  * Stats -- Historical data including total connections, operations per second and keyspace info
  * Replication -- Roles, master / slave information
  * CPU -- CPU info for server and user
  * Cluster -- Shows whether Redis clustering is enabled
  * Keyspace -- Information about the current Redis keyspace



| HTTP Method | API Path | Description |
|----|----|----|
| GET | /admin/ping | Sends a PING request to Redis. {"resultCode":"200","resultMessage":"Connection Successful","resultData":{"Ping":"PONG","used_memory":"836048"}} |
| GET | /admin/info | A complete list of all sections above |
| GET | /admin/info/{section}| The details about the section of data. The section name is case sensitive, use the details above. |


# API Output
## API Result from /admin/ping

```javascript
{
  "resultCode": "200",
  "resultMessage": "Connection Successful",
  "resultData": {
    "Ping": "PONG",
    "used_memory": "836048"
  }
}
```

## API Result from /admin/info/Server

```javascript
{
  "resultCode": "200",
  "resultMessage": "Connection Successful",
  "resultData": {
    "Server": {
      "redis_version": "3.0.7",
      "redis_git_sha1": "00000000",
      "redis_git_dirty": "0",
      "redis_build_id": "cacb135580d61140",
      "redis_mode": "standalone",
      "os": "Linux 3.19.0-51-generic x86_64",
      "arch_bits": "64",
      "multiplexing_api": "epoll",
      "gcc_version": "4.8.4",
      "process_id": "31611",
      "run_id": "067790c2dbe55cec9c345c6cf51a48cb64bdaec5",
      "tcp_port": "38593",
      "uptime_in_seconds": "822827",
      "uptime_in_days": "9",
      "hz": "10",
      "lru_clock": "383069",
      "config_file": "/var/vcap/store/cf-redis-broker/redis-data/7900c2c5-4d81-491a-ab5d-a94e70e37dc1/redis.conf"
    }
  }
}
```

## Result from /admin/info

```javascript
{
  "resultCode": "200",
  "resultMessage": "Connection Successful",
  "resultData": {
    "Server": {
      "redis_version": "3.0.7",
      "redis_git_sha1": "00000000",
      "redis_git_dirty": "0",
      "redis_build_id": "cacb135580d61140",
      "redis_mode": "standalone",
      "os": "Linux 3.19.0-51-generic x86_64",
      "arch_bits": "64",
      "multiplexing_api": "epoll",
      "gcc_version": "4.8.4",
      "process_id": "31611",
      "run_id": "067790c2dbe55cec9c345c6cf51a48cb64bdaec5",
      "tcp_port": "38593",
      "uptime_in_seconds": "822319",
      "uptime_in_days": "9",
      "hz": "10",
      "lru_clock": "382561",
      "config_file": "/var/vcap/store/cf-redis-broker/redis-data/7900c2c5-4d81-491a-ab5d-a94e70e37dc1/redis.conf"
    },
    "Clients": {
      "connected_clients": "1",
      "client_longest_output_list": "0",
      "client_biggest_input_buf": "0",
      "blocked_clients": "0"
    },
    "Memory": {
      "used_memory": "815112",
      "used_memory_human": "796.01K",
      "used_memory_rss": "9261056",
      "used_memory_peak": "877856",
      "used_memory_peak_human": "857.28K",
      "used_memory_lua": "36864",
      "mem_fragmentation_ratio": "11.36",
      "mem_allocator": "jemalloc-3.6.0"
    },
    "Persistence": {
      "loading": "0",
      "rdb_changes_since_last_save": "0",
      "rdb_bgsave_in_progress": "0",
      "rdb_last_save_time": "1459987202",
      "rdb_last_bgsave_status": "ok",
      "rdb_last_bgsave_time_sec": "0",
      "rdb_current_bgsave_time_sec": "-1",
      "aof_enabled": "1",
      "aof_rewrite_in_progress": "0",
      "aof_rewrite_scheduled": "0",
      "aof_last_rewrite_time_sec": "-1",
      "aof_current_rewrite_time_sec": "-1",
      "aof_last_bgrewrite_status": "ok",
      "aof_last_write_status": "ok",
      "aof_current_size": "0",
      "aof_base_size": "0",
      "aof_pending_rewrite": "0",
      "aof_buffer_length": "0",
      "aof_rewrite_buffer_length": "0",
      "aof_pending_bio_fsync": "0",
      "aof_delayed_fsync": "0"
    },
    "Stats": {
      "total_connections_received": "65",
      "total_commands_processed": "212",
      "instantaneous_ops_per_sec": "0",
      "total_net_input_bytes": "7177",
      "total_net_output_bytes": "72608070",
      "instantaneous_input_kbps": "0.00",
      "instantaneous_output_kbps": "0.00",
      "rejected_connections": "0",
      "sync_full": "0",
      "sync_partial_ok": "0",
      "sync_partial_err": "0",
      "expired_keys": "0",
      "evicted_keys": "0",
      "keyspace_hits": "0",
      "keyspace_misses": "0",
      "pubsub_channels": "0",
      "pubsub_patterns": "0",
      "latest_fork_usec": "890",
      "migrate_cached_sockets": "0"
    },
    "Replication": {
      "role": "master",
      "connected_slaves": "0",
      "master_repl_offset": "0",
      "repl_backlog_active": "0",
      "repl_backlog_size": "1048576",
      "repl_backlog_first_byte_offset": "0",
      "repl_backlog_histlen": "0"
    },
    "CPU": {
      "used_cpu_sys": "406.81",
      "used_cpu_user": "45.90",
      "used_cpu_sys_children": "0.00",
      "used_cpu_user_children": "0.00"
    },
    "Cluster": {
      "cluster_enabled": "0"
    },
    "Keyspace": {}
  }
}
```
