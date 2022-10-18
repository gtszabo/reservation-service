import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Random;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class BasicSimulation extends Simulation {

    Random random = new Random();
    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080/");

    public LocalDate getRandomEndDate() {
        return LocalDate.now().plusDays(random.nextInt(30));
    }

    ScenarioBuilder scn = scenario("REST Client")
            .exec(http("availability")
                    .get("/availability")
                    .queryParam("locationId", "ISL_VOLC_PO")
                    .queryParam("startDate", LocalDate.now())
                    .queryParam("endDate", getRandomEndDate()))
            .pause(Duration.ofMillis(50));
    {
        setUp(
                scn.injectClosed(
                        rampConcurrentUsers(20).to(50).during(300),
                        constantConcurrentUsers(50).during(300),
                        rampConcurrentUsers(50).to(20).during(300)
                )
        ).protocols(httpProtocol);
    }
}