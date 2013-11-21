package org.xblackcat.sjpu.storage;

/**
 * 21.11.13 16:56
 *
 * @author xBlackCat
 */
public abstract class AnObjectMapper<Orig, DBMap> {
    protected final Class<Orig> origClass;
    protected final Class<DBMap> mappedClass;

    protected AnObjectMapper(Class<Orig> origClass, Class<DBMap> mappedClass) {
        this.origClass = origClass;
        this.mappedClass = mappedClass;
    }

    public Class<Orig> getOrigClass() {
        return origClass;
    }

    public Class<DBMap> getMappedClass() {
        return mappedClass;
    }

    public abstract Orig valueOf(DBMap dbValue);

    public abstract DBMap convert(Orig obj);
}
