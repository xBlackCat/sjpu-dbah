package org.xblackcat.sjpu.storage.converter.builder;

import java.lang.reflect.Constructor;

final class Info {
    final String suffix;
    final Constructor<?>[] reference;

    Info(String suffix, Constructor<?>... reference) {
        this.suffix = suffix;
        this.reference = reference;
    }
}

