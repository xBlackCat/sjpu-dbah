package org.xblackcat.sjpu.storage.consumer;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 17.06.2014 11:13
 *
 * @author xBlackCat
 */
public interface IRawProcessor {
    void process(ResultSet rs) throws SQLException;
}
