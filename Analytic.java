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

import org.debatetool.core.html.HtmlEncoder;

import java.io.IOException;

public class Analytic extends SpeechComponent {
    private String content;

    public Analytic(String content) {
        this.content = content;
    }

    @Override
    public String getDisplayContent() {
        return "<t>"+ HtmlEncoder.encode(content)+"</t>";
    }

    @Override
    public SpeechComponent clone() {
        return new Analytic(this.content);
    }

    @Override
    public String getStorageString() {
        return content;
    }

    @Override
    public String getStateString() {
        return null;
    }

    @Override
    public void load() throws IOException {
        // nothing to do
    }

    @Override
    public String getLabel() {
        return content;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public boolean equals(Object o){
        if (!(o instanceof Analytic)){
            return false;
        }
        return content.equals(((Analytic) o).content);
    }
}
