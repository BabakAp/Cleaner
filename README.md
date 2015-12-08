# Cleaner
This aims to clean lane measurements provided by NIST for NIST Data Science Pre-Pilot program. The project was done as part of Data Science course [(Dr. Daisy Zhe Wang)](http://dsr.cise.ufl.edu/daisyw/) at University of Florida.

## Steps
* Group traffic data by laneid
* Create nine member segments of traffic events

  #### Three comparisons
    * Flow vs group mean: | Flow - Group mean | < 4 * Global standard deviation
      * Replace with last valid value
    * Flow is non-negative: Flow > 0
      * Replace with group median, or 0 if group median is invalid
    * Flow vs group median: | Flow - Group median | < 3 * Group standard deviation
        * Replace with group median


## Run
You can import the project into NetBeans 8.1 and build and run it.
The ```main``` function takes a directory (that contains the files to be cleaned) as input.
