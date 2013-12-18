package org.xblackcat.sjpu.storage.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sjpu.storage.converter.IToObjectConverter;

/**
* 17.12.13 16:45
*
* @author xBlackCat
*/
class ConverterInfo {
    private static final Log log = LogFactory.getLog(ConverterInfo.class);

    private final Class<?> realReturnType;
    private final Class<? extends IToObjectConverter<?>> converter;
    private final boolean useFieldList;

    ConverterInfo(
            Class<?> realReturnType,
            Class<? extends IToObjectConverter<?>> converter,
            boolean useFieldList
    ) {
        this.realReturnType = realReturnType;
        this.converter = converter;
        this.useFieldList = useFieldList;
    }

    public Class<?> getRealReturnType() {
        return realReturnType;
    }

    public Class<? extends IToObjectConverter<?>> getConverter() {
        return converter;
    }

    public boolean isUseFieldList() {
        return useFieldList;
    }

}
