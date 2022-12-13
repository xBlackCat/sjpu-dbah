package org.xblackcat.sjpu.storage.ann;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Repeatable(ExpandTypeList.class)
public @interface ExpandType {
    Class<?> type();

    String[] fields();
}
