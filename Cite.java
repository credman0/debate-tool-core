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

import org.debatetool.core.html.HtmlEncoder;
import org.debatetool.io.IOUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class Cite implements Serializable {
    protected String author;
    protected String date;
    protected String additionalInfo;

    public Cite(String author, String date, String additionalInfo) {
        this.author = author;
        this.date = date;
        this.additionalInfo = additionalInfo;
    }

    /**
     * Loads the cite from the given DataInput, given that the DataInput is pointed at the beginning of a serialized
     * cite.
     * @param in
     */
    public Cite (DataInput in) throws IOException {
        loadFromInput(in);
    }

    public String getAuthor() {
        return author;
    }
    public String getDate() {
        return date;
    }
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void writeToOutput(DataOutput out) throws IOException {
        IOUtil.writeSerializeString(getAuthor(),out);
        IOUtil.writeSerializeString(getDate(),out);
        IOUtil.writeSerializeString(getAdditionalInfo(),out);
    }

    public void loadFromInput(DataInput in) throws IOException{
        author = IOUtil.readDeserializeString(in);
        date = IOUtil.readDeserializeString(in);
        additionalInfo = IOUtil.readDeserializeString(in);
    }

    public String toString(){
        return getAuthor()+getDate()+getAdditionalInfo();
    }

    public String getDisplayContent(){
        return "<c>"+ HtmlEncoder.encode(getAuthor() + " " + getDate()) + "</c> ("+HtmlEncoder.encode(getAdditionalInfo())+")";
    }

    @Override
    public boolean equals(Object o){
        if (!(o instanceof Cite)){
            return false;
        }
        Cite oc = (Cite) o;
        return (oc.author.equals(author)) && (oc.additionalInfo.equals(additionalInfo)) && (oc.date.equals(date));
    }
}
