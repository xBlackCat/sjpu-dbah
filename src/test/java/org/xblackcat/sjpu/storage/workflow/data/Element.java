package org.xblackcat.sjpu.storage.workflow.data;

/**
* 22.04.2014 18:10
*
* @author xBlackCat
*/
public class Element implements IElement<String> {
    public final int id;
    public final String name;

    public Element(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Element)) {
            return false;
        }

        Element element = (Element) o;

        return !(name != null ? !name.equals(element.name) : element.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
