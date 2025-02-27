package ai.aletyx.kie.workshop.sub.reusable;

import java.util.Map;

import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.kogito.internal.process.event.DefaultKogitoProcessEventListener;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MyListener extends DefaultKogitoProcessEventListener {
    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        Map<String, Object> metadata = event.getProcessInstance().getProcess().getMetaData();
        String riskLevel = (String) metadata.get("riskLevel");
        if (riskLevel != null && riskLevel == "low") {
            System.out.println("This is low risk process");
        }
    }
}
