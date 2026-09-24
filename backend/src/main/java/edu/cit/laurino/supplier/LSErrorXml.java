package edu.cit.laurino.supplier;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "LSError")
class LSErrorXml {
    @JacksonXmlProperty(localName = "Code")
    public String code;

    @JacksonXmlProperty(localName = "Message")
    public String message;
}
