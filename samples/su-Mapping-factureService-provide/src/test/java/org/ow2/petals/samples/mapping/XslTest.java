/**
 * Copyright (c) 2016-2024 Linagora
 * 
 * This program/library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This program/library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program/library; If not, see http://www.gnu.org/licenses/
 * for the GNU Lesser General Public License version 2.1.
 */
package org.ow2.petals.samples.mapping;

import static org.ow2.petals.se.mapping.junit.Assert.assertXslTransformation;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

/**
 * Unit tests of XSLs used by the SU
 * 
 * @author Christophe DENEUX - Linagora
 *
 */
public class XslTest {

    private static final String XML_DIR = "xml-to-transform/";

    private static final String XML_RESULT_DIR = "xml-result/";

    private static final String INPUT_CONSULTER_DIR = "input-consulter/";

    private static final String OUTPUT_CONSULTER_DIR = "output-consulter/";

    private static final String INPUT_STOCKER_DIR = "input-stocker/";

    private static final String OUTPUT_STOCKER_DIR = "output-stocker/";

    private static final String INPUT_CONSULTER_XML_DIR = XML_DIR + INPUT_CONSULTER_DIR;

    private static final String INPUT_CONSULTER_RESULT_DIR = XML_RESULT_DIR + INPUT_CONSULTER_DIR;

    private static final String OUTPUT_CONSULTER_XML_DIR = XML_DIR + OUTPUT_CONSULTER_DIR;

    private static final String OUTPUT_CONSULTER_RESULT_DIR = XML_RESULT_DIR + OUTPUT_CONSULTER_DIR;

    private static final String INPUT_STOCKER_XML_DIR = XML_DIR + INPUT_STOCKER_DIR;

    private static final String INPUT_STOCKER_RESULT_DIR = XML_RESULT_DIR + INPUT_STOCKER_DIR;

    private static final String OUTPUT_STOCKER_XML_DIR = XML_DIR + OUTPUT_STOCKER_DIR;

    private static final String OUTPUT_STOCKER_RESULT_DIR = XML_RESULT_DIR + OUTPUT_STOCKER_DIR;

    private static final String XSL_INPUT_CONSULTER = "input-consulter.xsl";

    private static final String XSL_OUTPUT_CONSULTER = "output-consulter.xsl";

    private static final String XSL_INPUT_STOCKER = "input-stocker.xsl";

    private static final String XSL_OUTPUT_STOCKER = "output-stocker.xsl";

    /**
     * Check the XSL 'input-consulter.xsl' against a nominal XML
     */
    @Test
    public void inputConsulter_Nominal() throws IOException, TransformerException, SAXException {
        assertXslTransformation(INPUT_CONSULTER_RESULT_DIR + "nominal.xml", INPUT_CONSULTER_XML_DIR + "nominal.xml",
                XSL_INPUT_CONSULTER);
    }

    /**
     * Check the XSL 'output-consulter.xsl' against a nominal XML for an existing document
     */
    @Test
    public void outputConsulter_NominalExisting() throws IOException, TransformerException, SAXException {
        assertXslTransformation(OUTPUT_CONSULTER_RESULT_DIR + "nominal.xml", OUTPUT_CONSULTER_XML_DIR + "nominal.xml",
                XSL_OUTPUT_CONSULTER);
    }

    /**
     * Check the XSL 'output-consulter.xsl' against an un-existing document
     */
    @Test
    public void outputConsulter_Fault() throws IOException, TransformerException, SAXException {
        assertXslTransformation(OUTPUT_CONSULTER_RESULT_DIR + "fault.xml", OUTPUT_CONSULTER_XML_DIR + "fault.xml",
                XSL_OUTPUT_CONSULTER);
    }

    /**
     * Check the XSL 'input-stocker.xsl' against a nominal XML
     */
    @Test
    public void inputStocker_Nominal() throws IOException, TransformerException, SAXException {
        assertXslTransformation(INPUT_STOCKER_RESULT_DIR + "nominal.xml", INPUT_STOCKER_XML_DIR + "nominal.xml",
                XSL_INPUT_STOCKER);
    }

    /**
     * Check the XSL 'output-stocker.xsl' against a nominal XML
     */
    @Test
    public void outputStocker_Nominal() throws IOException, TransformerException, SAXException {
        assertXslTransformation(OUTPUT_STOCKER_RESULT_DIR + "nominal.xml", OUTPUT_STOCKER_XML_DIR + "nominal.xml",
                XSL_OUTPUT_STOCKER);
    }

}
