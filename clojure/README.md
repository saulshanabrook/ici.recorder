# `ici.recorder` CLojure code

## Usage

Set the `CLOJUSH-PARQUET-URI` to a local path (`clojush/`) or a HDFS path
(`alluxio://192.55.555.55:19998/clojush/`)

```clojure
(require 'ici-recorder)
(ici-recorder/record-run uuid config)
(ici-recorder/record-generation uuid generation-number config)
```


## Data types

We support writing a subset of Parquet data types. We are targeting
[Apache Drill](https://drill.apache.org/docs/parquet-format/), so we
choose to support those types so that we maximize the variety of types in
Clojure/Java, without writing more types than we need to.



| SQL Type    | Parquet Type                     | Parquet Logical Type | Clojure/Java Type             | Description                                                                                                                                |
|:------------|:---------------------------------|:---------------------|:------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------|
| `INTEGER`    | `INT32`                          |                      | `Integer`                        | 8-byte signed integer                                                                                                                      |
| `BIGINT`    | `INT64`                          |                      | `Long`                        | 8-byte signed integer                                                                                                                      |
| `BOOLEAN`   | `BOOLEAN`                        |                      | `Boolean`                     | TRUE (1) or FALSE (0)                                                                                                                      |
| `DOUBLE`    | `DOUBLE`                         |                      | `Double`                      | 8-byte double precision floating point number                                                                                              |
| `VARCHAR`   | `BINARY`                         | `UTF8`               | `String`, `Symbol`, functions | Annotates the binary primitive type. The byte array is interpreted as a UTF-8 encoded character string.                                    |
| `DATE`      | `INT32`                          | `DATE`               | `java.time.LocalDate`         | Date, not including time of day. Uses the int32 annotation. Stores the number of days from the Unix epoch, 1 January 1970.                 |
| `TIME`      | `INT32`                          | `TIME_MILLIS`        | `java.time.LocalTime`         | Logical time, not including the date. Annotates int32. Number of milliseconds after midnight.                                              |
| `TIMESTAMP` | `INT64`                          | `TIMESTAMP_MILLIS`   | `java.time.Instant`           | Logical date and time. Annotates an int64 that stores the number of milliseconds from the Unix epoch, 00:00:00.000 on 1 January 1970, UTC. |
| `INTERVAL`  | `FIXED_LEN_BYTE_ARRAY` length 12 | `INTERVAL`           | `java.time.Duration`          | An interval of time. Annotates a fixed_len_byte_array of length 12. Months, days, and ms in unsigned little-endian encoding.               |

## Development
### installing alluxio jar

```bash
cd alluxio
mvn install -Dhadoop.version=2.2.0 -DskipTests
```