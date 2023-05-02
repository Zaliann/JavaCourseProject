package ru.croc;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "result")
public class Result {

    @XmlAttribute(name = "word")
    public String word;

    @XmlElement(name = "box")
    public int box;
}
