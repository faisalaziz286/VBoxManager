//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.08.12 at 07:46:30 PM CDT 
//


package com.kedzie.vbox.api.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BandwidthGroupType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="BandwidthGroupType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Null"/>
 *     &lt;enumeration value="Disk"/>
 *     &lt;enumeration value="Network"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "BandwidthGroupType")
@XmlEnum
public enum BandwidthGroupType {

    @XmlEnumValue("Null")
    NULL("Null"),
    @XmlEnumValue("Disk")
    DISK("Disk"),
    @XmlEnumValue("Network")
    NETWORK("Network");
    private final String value;

    BandwidthGroupType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BandwidthGroupType fromValue(String v) {
        for (BandwidthGroupType c: BandwidthGroupType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}