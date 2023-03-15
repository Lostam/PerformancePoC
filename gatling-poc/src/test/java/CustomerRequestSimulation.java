import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.core.body.RawFileBody;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Oded Balazada
 */
public class CustomerRequestSimulation extends Simulation {

    File dir = new File("/Users/odedb/Desktop/Projects/Playground/Packages/nuget/publish/packages");
    File[] directoryListing = Arrays.copyOfRange(dir.listFiles(),0,10);

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("https://odedgatlingtest.jfrogdev.org/artifactory")
            .basicAuth("admin", "password")
            .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0");
    ScenarioBuilder scn = scenario("FileUpload")
            .foreach(Arrays.asList(directoryListing), "filePath")
            .on(exec(
                http("upload").put(session -> "/nuget-local/packages/" +  ((File) session.get("filePath")).getName())
                        .header("content-type", "multipart/form-data")
                        .body(RawFileBody(session->  ((File) session.get("filePath")).getAbsolutePath()))
            ));

    ScenarioBuilder scn1 = scenario("FileUpload-1")
            .foreach(Arrays.asList(directoryListing), "filePath", "counter")
            .on(exec(
                    http("upload-1").put(session -> "/nuget-local-1/packages/" +  ((File) session.get("filePath")).getName())
                            .header("content-type", "multipart/form-data")
                            .body(RawFileBody(session->  ((File) session.get("filePath")).getAbsolutePath()))
            ));

    ScenarioBuilder scn2 = scenario("FileUpload-2")
            .foreach(Arrays.asList(directoryListing), "filePath", "counter")
            .on(exec(
                    http("upload-2").put(session -> "/nuget-local-2/packages/" +  ((File) session.get("filePath")).getName())
                            .header("content-type", "multipart/form-data")
                            .body(RawFileBody(session->  ((File) session.get("filePath")).getAbsolutePath()))
            ));

    ScenarioBuilder downloadScenario = scenario("FileDownload")
            .foreach(Arrays.asList(directoryListing), "filePath")
            .on(exec(
                    http("download").get(session -> "/nuget-local-2/packages/" +  ((File) session.get("filePath")).getName())
            ));

    {
        setUp(
                scn.injectOpen(atOnceUsers())
                        .andThen(downloadScenario.injectOpen(atOnceUsers(1))), // -- add download scenario
                scn1.injectOpen(atOnceUsers(1)),
                scn2.injectOpen(atOnceUsers(1))
        ).protocols(httpProtocol);
    }
}