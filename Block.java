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

    @Override
    public boolean canBeAdded(SpeechComponent component) {
        return (!(component instanceof Speech)) &&(!(component instanceof Block));
    }
}
