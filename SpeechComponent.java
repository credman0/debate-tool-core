/*
 *                               This program is free software: you can redistribute it and/or modify
 *                               it under the terms of the GNU General Public License as published by
 *                                the Free Software Foundation, either version 3 of the License, or
 *                                (at your option) any later version.
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

import java.io.IOException;
import java.io.Serializable;

public abstract class SpeechComponent implements Serializable {
    public abstract void load() throws IOException;
    public abstract boolean isLoaded();
    public abstract String getDisplayContent();
    public abstract String getStorageString();

    /**
     * A string containing additional information about the special state of an object, IE current tag/overlays
     * @return
     */
    public abstract String getStateString();
    public void restoreState(String s){
        throw new UnsupportedOperationException();
    }
    public abstract SpeechComponent clone();
    public abstract String getLabel();

    public static SpeechComponent importFromData(String type, String storageString) throws IOException {
        if (type.equals(Block.class.getName())){
            // empty card with hash used to dynamically load it later
            return IOController.getIoController().getComponentIOManager().retrieveSpeechComponent(IOUtil.decodeString(storageString));
        }else if (type.equals(Card.class.getName())){
            // empty card with hash used to dynamically load it later
            return new Card(IOUtil.decodeString(storageString));
        }else if (type.equals(Analytic.class.getName())){
            return new Analytic(storageString);
        }else{
            throw new IllegalArgumentException("Unrecognized type: " + type);
        }
    }
}
