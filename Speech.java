package org.debatetool.core;

public class Speech extends SpeechElementContainer{

    public Speech(String name) {
        super(name);
    }

    public Speech(byte[] hash) {
        super(hash, "");
    }

    @Override
    protected String getEnumeration(int i) {
        return i+1+"";
    }
}
