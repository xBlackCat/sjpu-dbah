package org.xblackcat.sjpu.storage.converter.builder;

import java.lang.reflect.Constructor;

record Info(String suffix, Constructor<?>... reference) {
}

