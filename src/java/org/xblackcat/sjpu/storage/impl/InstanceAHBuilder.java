package org.xblackcat.sjpu.storage.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.IAH;
import org.xblackcat.sjpu.storage.StorageSetupException;

import java.lang.reflect.InvocationTargetException;

/**
 * 21.02.13 11:54
 *
 * @author xBlackCat
 */
class InstanceAHBuilder implements IAHBuilder {
    protected final Log log = LogFactory.getLog(getClass());

    @Override
    public <T extends IAH> T build(Class<T> target, IQueryHelper helper) throws StorageSetupException {
        try {
            return target.getConstructor(IQueryHelper.class).newInstance(helper);
        } catch (InstantiationException e) {
            throw new StorageSetupException("Class is not implemented", e);
        } catch (IllegalAccessException e) {
            throw new StorageSetupException("Access helper constructor should be public", e);
        } catch (InvocationTargetException e) {
            throw new StorageSetupException("Exception occurs in access helper constructor", e);
        } catch (NoSuchMethodException e) {
            throw new StorageSetupException(
                    "Access helper class constructor should have the same signature as " + AnAH.class.getName(),
                    e
            );
        }
    }
}
