package com.thed.utils;

import java.util.*;

/**
 * Created by prashant on 3/1/20.
 */
public class XMLLink {

    enum Type {
        OBJECT,
        ARRAY
    }

    private String xmlTagName;

    private Type type;//object, array

    private Map<String, XMLLink> childXMLLinks;

    private XMLLink parentXMLLink;

    public XMLLink(String xmlTagName, Type type) {
        setXmlTagName(xmlTagName);
        setType(type);
        init();
    }

    public XMLLink() {
        init();
    }

    private void init() {
        childXMLLinks = new HashMap<String, XMLLink>();
    }

    public List<XMLLink> getChildWithType(Type type) {
        List<XMLLink> linkList = new ArrayList<XMLLink>();
        for (XMLLink link : getChildXMLLinks().values()) {
            if(link.getType() == type) {
                linkList.add(link);
            }
        }
        return linkList;
    }

    public void attachLink(String[] linkArray, Type type) {

        if(linkArray.length > 0 && getXmlTagName() == null) {
            setXmlTagName(linkArray[0]);
            setType(type);
        }

        if(linkArray.length > 1 && getXmlTagName().equals(linkArray[0])) {
            XMLLink xmlLink = getChildXMLLinks().get(linkArray[1]);
            if(xmlLink != null) {
                if(xmlLink.getType() == Type.ARRAY) {
                    xmlLink.setType(type);
                }
                xmlLink.attachLink(Arrays.copyOfRange(linkArray, 1, linkArray.length), type);
            } else {
                xmlLink = new XMLLink(linkArray[1], type);
                addChildXMLLink(xmlLink);
                xmlLink.setParentXMLLink(this);
                xmlLink.attachLink(Arrays.copyOfRange(linkArray, 1, linkArray.length), Type.OBJECT);
            }
        }
    }

    public String getXMLPath() {
        if(getParentXMLLink() == null) {
            return getXmlTagName();
        } else {
            return getParentXMLLink().getXMLPath() + "." + getXmlTagName();
        }
    }

    public String getXmlTagName() {
        return xmlTagName;
    }

    public void setXmlTagName(String xmlTagName) {
        this.xmlTagName = xmlTagName;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Map<String, XMLLink> getChildXMLLinks() {
        return childXMLLinks;
    }

    public void setChildXMLLinks(Map<String, XMLLink> childXMLLinks) {
        this.childXMLLinks = childXMLLinks;
    }

    public void addChildXMLLink(XMLLink childLink) {
        childXMLLinks.put(childLink.getXmlTagName(), childLink);
    }

    public XMLLink getParentXMLLink() {
        return parentXMLLink;
    }

    public void setParentXMLLink(XMLLink parentXMLLink) {
        this.parentXMLLink = parentXMLLink;
    }
}
