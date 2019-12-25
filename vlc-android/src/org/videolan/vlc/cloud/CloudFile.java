package org.videolan.vlc.cloud;

public class CloudFile {

    private String name;
    private String type;
    private String home;

    public CloudFile(String name, String type, String home) {
        this.name = name;
        this.type = type;
        this.home = home;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }
}
