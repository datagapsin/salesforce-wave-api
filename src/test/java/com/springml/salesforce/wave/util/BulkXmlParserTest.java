package com.springml.salesforce.wave.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.springml.salesforce.wave.model.BatchInfo;
import com.springml.salesforce.wave.model.BatchInfoList;

public class BulkXmlParserTest {
    private static final String NS = "xmlns=\"http://www.force.com/2009/06/asyncapi/dataload\"";

    private static final String BATCH_INFO = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><batchInfo " + NS + ">"
            + "<id>751B0000000scSHIAY</id><jobId>750B0000000WlhtIAC</jobId><state>Completed</state>"
            + "<createdDate>2016-03-15T06:25:25.000Z</createdDate><systemModstamp>2016-03-15T06:25:26.000Z</systemModstamp>"
            + "<numberRecordsProcessed>2</numberRecordsProcessed><numberRecordsFailed>1</numberRecordsFailed>"
            + "<totalProcessingTime>93</totalProcessingTime><apiActiveProcessingTime>29</apiActiveProcessingTime>"
            + "<apexProcessingTime>7</apexProcessingTime></batchInfo>";

    @Test
    public void parsesBatchInfo() throws Exception {
        BatchInfo batchInfo = BulkXmlParser.parseBatchInfo(BATCH_INFO);

        assertEquals("751B0000000scSHIAY", batchInfo.getId());
        assertEquals("750B0000000WlhtIAC", batchInfo.getJobId());
        assertEquals("Completed", batchInfo.getState());
        assertEquals("2016-03-15T06:25:25.000Z", batchInfo.getCreatedDate());
        assertEquals("2016-03-15T06:25:26.000Z", batchInfo.getSystemModstamp());
        assertEquals(2L, batchInfo.getNumberRecordsProcessed());
        assertEquals(1L, batchInfo.getNumberRecordsFailed());
        assertEquals(93L, batchInfo.getTotalProcessingTime());
        assertEquals(29L, batchInfo.getApiActiveProcessingTime());
        assertEquals(7L, batchInfo.getApexProcessingTime());
    }

    @Test
    public void parsesBatchInfoWithoutNamespaceAndIgnoresUnknownElements() throws Exception {
        BatchInfo batchInfo = BulkXmlParser.parseBatchInfo(
                "<batchInfo><id>X1</id><state>Queued</state><unknownTag>zzz</unknownTag></batchInfo>");

        assertEquals("X1", batchInfo.getId());
        assertEquals("Queued", batchInfo.getState());
        assertEquals(0L, batchInfo.getNumberRecordsProcessed());
    }

    @Test
    public void parsesBatchInfoList() throws Exception {
        String xml = "<batchInfoList " + NS + ">"
                + "<batchInfo><id>B1</id><jobId>J1</jobId><state>Completed</state>"
                + "<numberRecordsProcessed>2</numberRecordsProcessed></batchInfo>"
                + "<batchInfo><id>B2</id><jobId>J1</jobId><state>InProgress</state></batchInfo>"
                + "</batchInfoList>";

        BatchInfoList list = BulkXmlParser.parseBatchInfoList(xml);

        assertEquals(2, list.getBatchInfo().size());
        assertEquals("B1", list.getBatchInfo().get(0).getId());
        assertEquals(2L, list.getBatchInfo().get(0).getNumberRecordsProcessed());
        assertEquals("InProgress", list.getBatchInfo().get(1).getState());
    }

    @Test
    public void parsesBatchResultIds() throws Exception {
        List<String> ids = BulkXmlParser.parseBatchResultIds("<result-list " + NS
                + "><result>752x00000004CJE</result><result>752x00000004CJF</result></result-list>");

        assertEquals(2, ids.size());
        assertEquals("752x00000004CJE", ids.get(0));
        assertEquals("752x00000004CJF", ids.get(1));
    }

    @Test
    public void emptyResultListGivesEmptyList() throws Exception {
        assertTrue(BulkXmlParser.parseBatchResultIds("<result-list/>").isEmpty());
    }

    @Test
    public void rejectsDoctypeToBlockXxe() throws Exception {
        String xml = "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
                + "<batchInfo><id>&xxe;</id></batchInfo>";
        try {
            BulkXmlParser.parseBatchInfo(xml);
            fail("DOCTYPE must be rejected");
        } catch (Exception expected) {
            // expected
        }
    }
}
