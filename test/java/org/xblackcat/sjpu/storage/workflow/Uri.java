package org.xblackcat.sjpu.storage.workflow;

import java.net.URI;

/**
* 22.04.2014 18:10
*
* @author xBlackCat
*/
public class Uri implements IElement<URI> {
    public final int id;
    public final URI name;

    public Uri(int id, URI name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public URI getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Uri)) {
            return false;
        }

        Uri uri = (Uri) o;

        if (id != uri.id) {
            return false;
        }
        if (name != null ? !name.equals(uri.name) : uri.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
