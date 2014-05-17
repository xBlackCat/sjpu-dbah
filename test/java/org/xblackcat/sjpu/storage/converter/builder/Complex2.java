package org.xblackcat.sjpu.storage.converter.builder;

import org.xblackcat.sjpu.storage.ann.DefaultRowMap;

/**
 * 25.04.2014 13:27
 *
 * @author xBlackCat
 */
public class Complex2 {
    public Complex2(int i, SimpleObject o, String s) {
    }

    public Complex2(int i, SimpleObject o, long s) {
    }

    public Complex2(int i, SimpleObject o, SimpleObject s) {
    }

    @DefaultRowMap
    public Complex2(String i, SimpleObject o, long s) {
    }
}
