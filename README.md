Quick utility for ripping CPU counts out of exported JSON.

Pre-reqs - Maven 3.3.9+, JAVA 1.8.0+

Clone the repo, then:

mvn clean install

To run the utility use:

java -classpath target/jsonparse-1.0-jar-with-dependencies.jar org.uth.jsonparse.utils.Generator data/container_cpu_usage_seconds_total.json "2018-12-26 00:00:00" "2019-01-10 23:59:59"

(replace the data/xxxx.json with the target JSON file, and the start and finish times with yyyy-mm-dd hh:mm:ss format)
Time brackets are optional, if you provide no times the genertaor defaults to providing the CPU counts for all records.
