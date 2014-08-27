package org.xblackcat.sjpu.storage.workflow_auto;

/**
* 22.04.2014 18:10
*
* @author xBlackCat
*/
public class ElementNumber implements IElement<Numbers> {
    public final int id;
    public final Numbers name;

    public ElementNumber(int id, Numbers name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public Numbers getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ElementNumber)) {
            return false;
        }

        ElementNumber that = (ElementNumber) o;

        return name == that.name;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
