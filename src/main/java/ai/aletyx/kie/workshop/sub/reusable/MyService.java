package ai.aletyx.kie.workshop.sub.reusable;

import java.util.HashMap;
import java.util.Map;

import org.kie.kogito.Model;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@ApplicationScoped
@Path("/custom")
public class MyService {

   @Inject
   ProcessService processService;

   @Inject
   @Named("contentApproval")
   Process<? extends Model> process;

   @GET
   public void startMyProcessCustomWay() {
        for (int i = 0; i < 2; i++) {
            Model m = process.createModel();
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("document", new Document("id" + i, "name" + i, "content"+i));
            m.fromMap(parameters);

            ProcessInstance<?> processInstance = processService.createProcessInstance((Process)process, null, m, null, null);
            System.out.println("Status:" + processInstance.checkError().variables());
        }
    }
}
