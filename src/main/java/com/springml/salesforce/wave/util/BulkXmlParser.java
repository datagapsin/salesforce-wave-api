package com.springml.salesforce.wave.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.springml.salesforce.wave.model.BatchInfo;
import com.springml.salesforce.wave.model.BatchInfoList;

/**
 * Parses the XML responses of the Salesforce Bulk API (batchInfo, batchInfoList and result-list)
 * with the JDK's built-in XML parser.
 *
 * This replaces the Jackson XmlMapper (jackson-dataformat-xml) that was used before. That module has
 * to match the jackson-core on the classpath exactly, which breaks on runtimes that ship their own
 * (older) Jackson, for example Databricks.
 *
 * Unknown elements are ignored and the XML namespace is not taken into account, as before.
 */
public final class BulkXmlParser {

    private BulkXmlParser() {
    }

    public static BatchInfo parseBatchInfo(String xml) throws ParserConfigurationException, SAXException, IOException {
        return toBatchInfo(parse(xml).getDocumentElement());
    }

    public static BatchInfoList parseBatchInfoList(String xml)
            throws ParserConfigurationException, SAXException, IOException {
        Element root = parse(xml).getDocumentElement();
        List<BatchInfo> batches = new ArrayList<BatchInfo>();
        for (Element child : childElements(root)) {
            if ("batchInfo".equals(localName(child))) {
                batches.add(toBatchInfo(child));
            }
        }

        BatchInfoList list = new BatchInfoList();
        list.setBatchInfo(batches);
        return list;
    }

    public static List<String> parseBatchResultIds(String xml)
            throws ParserConfigurationException, SAXException, IOException {
        Element root = parse(xml).getDocumentElement();
        List<String> resultIds = new ArrayList<String>();
        for (Element child : childElements(root)) {
            if ("result".equals(localName(child))) {
                resultIds.add(child.getTextContent());
            }
        }

        return resultIds;
    }

    private static BatchInfo toBatchInfo(Element element) {
        BatchInfo batchInfo = new BatchInfo();
        for (Element child : childElements(element)) {
            String value = child.getTextContent().trim();
            switch (localName(child)) {
            case "id":
                batchInfo.setId(value);
                break;
            case "jobId":
                batchInfo.setJobId(value);
                break;
            case "state":
                batchInfo.setState(value);
                break;
            case "stateMessage":
                batchInfo.setStateMessage(value);
                break;
            case "createdDate":
                batchInfo.setCreatedDate(value);
                break;
            case "systemModstamp":
                batchInfo.setSystemModstamp(value);
                break;
            case "numberRecordsProcessed":
                batchInfo.setNumberRecordsProcessed(toLong(value));
                break;
            case "numberRecordsFailed":
                batchInfo.setNumberRecordsFailed(toLong(value));
                break;
            case "totalProcessingTime":
                batchInfo.setTotalProcessingTime(toLong(value));
                break;
            case "apiActiveProcessingTime":
                batchInfo.setApiActiveProcessingTime(toLong(value));
                break;
            case "apexProcessingTime":
                batchInfo.setApexProcessingTime(toLong(value));
                break;
            default:
                // unknown element, ignored
                break;
            }
        }

        return batchInfo;
    }

    private static long toLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0L;
        }

        return Long.parseLong(value.trim());
    }

    private static String localName(Element element) {
        String name = element.getLocalName();
        return name != null ? name : element.getNodeName();
    }

    private static List<Element> childElements(Element parent) {
        List<Element> elements = new ArrayList<Element>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element) node);
            }
        }

        return elements;
    }

    private static Document parse(String xml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = newSecureDocumentBuilderFactory().newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Responses come from Salesforce, but the parser is still hardened against DTDs and external
     * entities (XXE).
     */
    private static DocumentBuilderFactory newSecureDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }
}
