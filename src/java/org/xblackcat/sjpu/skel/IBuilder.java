package org.xblackcat.sjpu.skel;

/**
 * 04.04.2014 15:40
 *
 * @author xBlackCat
 */
public interface IBuilder<Base, Helper> {
    <T extends Base> T build(Class<T> target, Helper helper) throws GeneratorException;
}
