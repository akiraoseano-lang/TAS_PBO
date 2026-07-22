package com.project.tas_pbo.model;

public abstract class BaseModel {
    public abstract int getId();

    public String getDisplayInfo() {
        return getClass().getSimpleName() + " #" + getId();
    }
}
