package org.videolan.vlc.gui.browser.dummy;

import java.util.ArrayList;
import java.util.List;


public class DummyContent {


    public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    public static class DummyItem {
        public final String id;
        public final String name;

        public DummyItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
