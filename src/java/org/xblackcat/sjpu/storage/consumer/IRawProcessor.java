package org.xblackcat.sjpu.storage.consumer;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Consume a results row raw data.
 * <p>
 * The interface is used in cases when it is necessary to process sql types without converting into objects.
 *
 * @author xBlackCat
 */
public interface IRawProcessor {
    void process(ResultSet rs) throws SQLException;
}
