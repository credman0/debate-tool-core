package org.debatetool.core;

public class Block extends SpeechElementContainer {
    public Block(byte[] hash) {
        super(hash, "");
    }

    @Override
    protected String getEnumeration(int i) {
        return SpeechElementContainer.toAlphabet(i);
    }

    public Block(String name){
        super(name);
    }
}
