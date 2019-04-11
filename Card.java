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

import org.debatetool.io.IOUtil;
import org.debatetool.io.iocontrollers.IOController;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Card extends HashIdentifiedSpeechComponent implements StateRecoverableComponent {
    /**
     * "tags" here used in the debate sense
     */
    protected List<String> tags = new ArrayList<>();
    protected int tagIndex = 0;
    protected Cite cite;
    protected String text;
    private int preferredHighlightIndex = 0;
    private int preferredUnderlineIndex = 0;
    private CardOverlay loadedOverlay = null;

    private List<CardOverlay> underlining;
    private List<CardOverlay> highlighting;

    public List<CardOverlay> getUnderlining() {
        if (underlining==null){
            loadOverlay();
        }
        return underlining;
    }

    public List<CardOverlay> getHighlighting() {
        if (highlighting==null){
            loadOverlay();
        }
        return highlighting;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * The time the card text was last modified.
     */
    protected long timeStamp;

    public Card(byte[] hash){
        super(hash);
    }

    public Card(Cite cite, String text) {
        setCite(cite);
        setText(text);
    }

    public Cite getCite() {
        return cite;
    }

    private void setCite(Cite cite) {
        this.cite = cite;
        setModified(true);
    }

    private void setCite(String author, String date, String additionalInfo){
        this.cite = new Cite(author, date, additionalInfo);
        setModified(true);
    }

    public String getText() {
        return text;
    }

    private void setText(String text){
        this.text = text;
        formatText();
        timeStamp = System.currentTimeMillis();
        setModified(true);
    }

    public void writeToOutput(DataOutput out) throws IOException {
        out.write(getHash());
        out.writeLong(timeStamp);
        cite.writeToOutput(out);
        IOUtil.writeSerializeString(text,out);
        // write null terminating byte
        out.writeByte(0);
    }

    public static Card loadFromInput(DataInput in, boolean checkHash) throws IOException{
        byte[] hash = new byte[16];
        in.readFully(hash);
        long timeStamp = in.readLong();
        Cite cite = new Cite(in);
        String text = IOUtil.readDeserializeString(in);
        byte nullTerm = in.readByte();
        if (nullTerm!=0){
            throw new IllegalStateException("Card missing null terminator");
        }
        Card card = new Card(cite,text);
        card.timeStamp = timeStamp;
        if (checkHash){
            byte[] validHash = card.getHash();
            if (!Arrays.equals(hash,validHash)){
                throw new IllegalStateException("Hash validation failed for card load");
            }
        }
        return card;
    }

    @Override
    public String getLabel() {
        return getActiveTag()+"\n"+getCite().author+" "+getCite().getDate();
    }

    @Override
    public ArrayList<String>[] toLabelledLists() {
        ArrayList<String>[] labelledLists = new ArrayList[2];
        labelledLists[0] = new ArrayList<>(5);
        labelledLists[1] = new ArrayList<>(5+tags.size());

        labelledLists[0].add("Author");
        labelledLists[0].add("Date");
        labelledLists[0].add("Info");
        labelledLists[0].add("Text");
        labelledLists[0].add("Timestamp");

        labelledLists[1].add(getCite().getAuthor());
        labelledLists[1].add(getCite().getDate());
        labelledLists[1].add(getCite().getAdditionalInfo());
        labelledLists[1].add(getText());
        try {
            labelledLists[1].add(new String(IOUtil.longToBytes(timeStamp),"IBM437"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (String tag:tags){
            labelledLists[1].add(tag);
        }

        return labelledLists;
    }

    @Override
    public void importFromLabelledLists(List<String> labels, List<String> values) {
        String author = values.get(0);
        String date = values.get(1);
        String info = values.get(2);
        String text = values.get(3);
        String timestampString = values.get(4);
        try {
            timeStamp = IOUtil.bytesToLong(timestampString.getBytes("IBM437"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        setCite(author,date,info);
        // update text without changing timestamp
        this.text = text;
        // now the rest should be tags
        for (int i = 5; i < values.size(); i++){
            tags.add(values.get(i));
        }
    }

    @Override
    protected byte[] generateHash() {
        MessageDigest dg = null;
        try {
            dg = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return dg.digest((text+cite.toString()).getBytes(StandardCharsets.UTF_8));
    }

    public int getTagIndex(){
        return tagIndex;
    }

    public void setTagIndex(int i){
        tagIndex = i;
    }

    public String getActiveTag(){
        if (tags.isEmpty()){
            return "<empty>";
        }
        return tags.get(tagIndex);
    }

    public String getTag(int i){
        return tags.get(i);
    }

    public List<String> getTags(){
        return Collections.unmodifiableList(tags);
    }

    public void setTags(List<String> tags){
        this.tags.clear();
        this.tags.addAll(tags);
        setModified(true);
    }

    /**
     * adds a tag but only if it does not already exist
     * @param tag
     */
    public void addTag(String tag){
        if (!tags.contains(tag)){
            tags.add(tag);
        }
        setModified(true);
    }

    @Override
    public String getDisplayContent() {
        if (text == null){
            throw new IllegalStateException("Attempted to display card before loading");
        }
        if (loadedOverlay==null){
            loadOverlay();
        }
        // the overlay sanitizes the text, so don't encode it a second time
        return getCite().getDisplayContent()+"<br>"+loadedOverlay.generateHTML(getText());
    }

    private void loadOverlay(){
        HashMap<String, List<CardOverlay>> overlayMap = IOController.getIoController().getOverlayIOManager().getOverlays(getHash());
        assignOverlaysFromMap(overlayMap);
    }

    public void assignOverlaysFromMap(HashMap<String, List<CardOverlay>> overlayMap){
        underlining = overlayMap.get("Underline");
        highlighting = overlayMap.get("Highlight");
        if (underlining == null){
            underlining = new ArrayList<>();
        }
        if (highlighting == null){
            highlighting = new ArrayList<>();
        }
        CardOverlay underline = null;
        CardOverlay highlight = null;
        if (!underlining.isEmpty()){
            underline = underlining.get(getPreferredUnderlineIndex());
        }
        if (!highlighting.isEmpty()){
            highlight = highlighting.get(getPreferredHighlightIndex());
        }
        loadedOverlay = CardOverlay.combineOverlays(underline, highlight);
    }

    @Override
    public HashIdentifiedSpeechComponent clone() {
        // cite is already read-only, so no need to clone it
        Card clone = new Card(getCite(), getText());
        clone.tags.addAll(tags);
        clone.tagIndex = tagIndex;
        clone.loadedOverlay = loadedOverlay;
        clone.timeStamp = timeStamp;
        clone.preferredHighlightIndex = preferredHighlightIndex;
        clone.preferredUnderlineIndex = preferredUnderlineIndex;
        return clone;
    }

    @Override
    public String getStorageString() {
        return IOUtil.encodeString(getHash());
    }

    @Override
    public String getStateString() {
        return getTagIndex() + ":" + getPreferredUnderlineIndex() + ":" + getPreferredHighlightIndex();
    }

    @Override
    public void restoreState(String stateString){
        String[] states = stateString.split(":");
        tagIndex = Integer.parseInt(states[0]);
        preferredUnderlineIndex = Integer.parseInt(states[1]);
        preferredHighlightIndex = Integer.parseInt(states[2]);
    }

    @Override
    public void load() throws IOException {
        Card self = (Card) IOController.getIoController().getComponentIOManager().retrieveSpeechComponent(getHash());
        // TODO maybe a better way to import this information
        setTo(self);
    }

    public void setTo(Card card){
        this.text = card.text;
        this.cite = card.cite;
        this.timeStamp = card.timeStamp;
        this.tags = card.tags;

    }

    @Override
    public boolean isLoaded() {
        return !(text==null);
    }


    @Override
    public int hashCode() {
        return Arrays.hashCode(getHash());
    }

    protected void formatText(){
        text = cleanForCard(text);
    }

    /**
     * removes illegal characters (new lines and null bytes) from the string, then returns it
     * @param s any string
     * @return s, minus illegal characters for cards
     */
    public static String cleanForCard(String s){
        return s.replaceAll("\n", "").replaceAll("\0", "");
    }

    public int getPreferredUnderlineIndex() {
        return preferredUnderlineIndex;
    }

    public void setPreferredUnderlineIndex(int preferredUnderlineIndex) {
        this.preferredUnderlineIndex = preferredUnderlineIndex;
        loadedOverlay = null;
    }

    public int getPreferredHighlightIndex() {
        return preferredHighlightIndex;
    }

    public void setPreferredHighlightIndex(int preferredHighlightIndex) {
        this.preferredHighlightIndex = preferredHighlightIndex;
        loadedOverlay = null;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof Card)){
            return false;
        }
        Card oc = (Card) o;
        return text.equals(oc.text) && cite.equals(oc.cite);
    }
}
