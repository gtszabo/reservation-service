# Reservation service

An underwater volcano formed a new small island in the Pacific Ocean last month. All the conditions on the island seems perfect and it was decided to open it up for the general public to experience the pristine uncharted territory.
The island is big enough to host a single campsite so everybody is very excited to visit. In order to regulate the number of people on the island, it was decided to come up with an online web application to manage the reservations. You are responsible for design and development of a REST API service that will manage the campsite reservations.

To streamline the reservations a few constraints need to be in place -
- The campsite will be free for all.
- The campsite can be reserved for max 3 days.
- The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance. Reservations can be cancelled anytime.
- For sake of simplicity assume the check-in & check-out time is 12:00 AM


## Setup requirements

- JDK 17
- Docker and docker cli

## Local execution

1. Start the Postgres DB: `docker compose up`
2. Start the application: `./gradlew bootRun` from the project root directory
3. Use the provided [postman collection](./postman/Reservation.postman_collection.json)

## Performance testing using Gatling

Performance testing was completed on the availability endpoint. See the results [here](./performance-testing/basicsimulation-20221016215713631/index.html)

To repeat the performance test execution, run `./gradlew gatlingRun` from the project root directory

