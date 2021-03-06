/*
 *                               This program is free software: you can redistribute it and/or modify
 *                                it under the terms of the GNU General Public License as published by
 *                                the Free Software Foundation, version 3 of the License.
 *
 *                                This program is distributed in the hope that it will be useful,
 *                                but WITHOUT ANY WARRANTY; without even the implied warranty of
 *                                MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *                                GNU General Public License for more details.
 *
 *                                You should have received a copy of the GNU General Public License
 *                                along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *                                Copyright (c) 2019 Colin Redman
 */

package org.debatetool.core;

import org.debatetool.io.iocontrollers.IOController;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SpeechElementContainer extends HashIdentifiedSpeechComponent {
    protected String name;
    private List<SpeechComponent> contents;
    private boolean loaded = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setModified(true);
    }
    public SpeechElementContainer(byte[] hash, String name){
        super(hash);
        this.name = name;
        contents = new ArrayList<>();
    }

    public SpeechElementContainer(String name){
        this.name = name;
        contents = new ArrayList<>();
    }

    public SpeechElementContainer(){
        this("");
    }

    public String getDisplayContent(){
        StringBuilder contentsBuilder = new StringBuilder();
        for (int i = 0; i < contents.size(); i++) {
            contentsBuilder.append("<p><n>"+getEnumeration(i) + ") </n>");
            SpeechComponent component = contents.get(i);
            if (component instanceof Card){
                contentsBuilder.append("<n>"+((Card) component).getActiveTag() + "</n><br>");
            }
            contentsBuilder.append(component.getDisplayContent() + "</p>");
        }
        return contentsBuilder.toString();
    }

    public String getExportDisplayContent(boolean includeAnalytics){
        StringBuilder contentsBuilder = new StringBuilder();
        for (int i = 0; i < contents.size(); i++) {
            SpeechComponent component = contents.get(i);
            if (!includeAnalytics && component instanceof Analytic){
                continue;
            }
            contentsBuilder.append("<p><n>"+getEnumeration(i) + ") </n>");
            if (component instanceof Card){
                contentsBuilder.append("<n>"+((Card) component).getActiveTag() + "</n><br>");
            }
            if (component instanceof SpeechElementContainer){
                contentsBuilder.append(((SpeechElementContainer) component).getExportDisplayContent(includeAnalytics));
            }else{
                contentsBuilder.append(component.getDisplayContent());
            }
            contentsBuilder.append("</p>");
        }
        return contentsBuilder.toString();
    }

    @Override
    public String getStateString() {
        return null;
    }

    public abstract boolean canBeAdded(SpeechComponent component);

    public void addComponent(SpeechComponent component){
        if (!canBeAdded(component)){
            throw new IllegalArgumentException("Attempted to add component of illegal type: " + component.getClass());
        }
        contents.add(component);
        setModified(true);
    }

    public void removeComponent(SpeechComponent component){
        contents.remove(component);
        setModified(true);
    }

    public void removeComponent(int index){
        contents.remove(index);
        setModified(true);
    }

    public void insertComponentAbove(SpeechComponent component1, SpeechComponent toInsert){
        int index = contents.indexOf(component1);
        if (index>=0){
            contents.add(index, toInsert);
        }
    }


    public SpeechComponent getComponent(int i){
        return contents.get(i);
    }

    public void clearContents(){
        contents.clear();
        setModified(true);
    }

    public int size(){
        return contents.size();
    }

    @Override
    public String getLabel() {
        return name;
    }

    @Override
    public ArrayList<String>[] toLabelledLists() {
        ArrayList<String>[] labelledLists = new ArrayList[2];
        labelledLists[0] = new ArrayList<>(contents.size());
        labelledLists[1] = new ArrayList<>(contents.size());
        labelledLists[1].add(name);
        for (SpeechComponent component:contents){
            labelledLists[0].add(component.getClass().getName());
            labelledLists[1].add(component.getStorageString());
            String state = component.getStateString();
            if (state!=null){
                labelledLists[0].add("STATE");
                labelledLists[1].add(state);
            }
        }
        return labelledLists;
    }

    @Override
    public void importFromLabelledLists(List<String> labels, List<String> values) {
        this.name = values.get(0);
        for (int i = 0; i < labels.size(); i++){
            try {
                contents.add(SpeechComponent.importFromData(labels.get(i),values.get(i+1)));
                // check for optional state string
                if (i < labels.size()-1 && labels.get(i+1).equals("STATE")){
                    contents.get(contents.size()-1).restoreState(values.get(i+2));
                    i++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void load() throws IOException {
        IOController.getIoController().getComponentIOManager().loadAll(this);
        loaded = true;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public long getTimeStamp() {
        return 0;
    }

    @Override
    protected byte[] generateHash() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[16];
        random.nextBytes(bytes);
        return bytes;
    }


    @Override
    public HashIdentifiedSpeechComponent clone() {
        SpeechElementContainer clone = null;
        try {
            clone = getClass().getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        // copy the list
        assert clone != null;
        clone.contents.addAll(contents);
        clone.loaded = loaded;
        return clone;
    }


    @Override
    public String getStorageString() {
        return null;
    }

    public void reload() throws IOException {
        // TODO more elegant fix than querying the database?
        setModified(true);
        SpeechElementContainer container = (SpeechElementContainer) IOController.getIoController().getComponentIOManager().retrieveSpeechComponent(getHash());
        loaded = false;
        this.contents = container.contents;
        load();
    }

    @Override
    public boolean equals(Object o){
        if (!(o instanceof SpeechElementContainer)){
            return false;
        }
        return contents.equals(((SpeechElementContainer) o).contents) && bytesEqual(getHash(),((SpeechElementContainer) o).getHash()) && getName().equals(((SpeechElementContainer) o).getName());
    }

    public static boolean bytesEqual(byte[] b0, byte[] b1){
        if (b0.length!=b1.length){
            return false;
        }
        for (int i = 0; i < b0.length; i++){
            if (b0[i]!=b1[i]){
                return false;
            }
        }
        return true;
    }

    protected abstract String getEnumeration(int i);

    /**
     * convert an integer to an alphabetic index (a,b,...aa,ab,etc)
     * @param i
     * @return
     */
    public static String toAlphabet(int i){
        if (i<0){
            return "";
        }else {
            return toAlphabet((i / 26) - 1) + (char)(65 + i % 26);
        }
    }
}
