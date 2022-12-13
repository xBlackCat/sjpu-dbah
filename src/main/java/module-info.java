open module sjpu.dbah {
    exports org.xblackcat.sjpu.storage;
    exports org.xblackcat.sjpu.storage.ann;
    exports org.xblackcat.sjpu.storage.connection;
    exports org.xblackcat.sjpu.storage.consumer;
    exports org.xblackcat.sjpu.storage.converter;
    requires transitive java.sql;
    requires transitive java.management;
    requires transitive sjpu.utils;
    requires transitive sjpu.builder;
    requires commons.logging;
    requires commons.dbcp2;
    requires org.apache.commons.pool2;
    requires org.apache.commons.lang3;
    requires org.javassist;
    requires org.apache.commons.text;
}