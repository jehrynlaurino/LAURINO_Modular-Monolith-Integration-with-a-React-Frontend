package edu.cit.laurino.supplier;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "AuthResponse")
class AuthResponseXml {
    @JacksonXmlProperty(localName = "SessionToken")
    public String sessionToken;

    @JacksonXmlProperty(localName = "IssuedAt")
    public String issuedAt;
}
