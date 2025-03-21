package ai.aletyx.kie.workshop.sub.reusable;

import java.io.Serializable;

public class Document implements Serializable {
    
    String id;
    String name;
    String content;

    public Document(){

    }

    public Document(String id, String name, String content) {
        this.id = id;
        this.name = name;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
