Quick utility for ripping CPU counts out of exported JSON.

Pre-reqs - Maven 3.3.9+, JAVA 1.8.0+

Clone the repo, then:

mvn clean install

To run the utility pre-parse the original file using:

java -classpath target/jsonparse-1.0-jar-with-dependencies.jar org.uth.jsonparse.utils.Preparse data/container_cpu_usage_seconds_total.json data/output true

(where data/output is the target directory for the smaller files, parsed for performance)

Then use:

java -classpath target/jsonparse-1.0-jar-with-dependencies.jar org.uth.jsonparse.utils.GenerateReportFromFiles data/output/ output.txt "2018-01-04 05:28:55" "2019-01-30 04:24:55"

(where data/output is the target directory where the interim files were generated, output.txt is a future enhancement for generating a report and the start and end dates in normal format for refining output)
