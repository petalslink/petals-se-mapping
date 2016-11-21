/**
 * Copyright (c) 2016 Linagora
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
package org.ow2.petals.se.mapping.incomming;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.petals.component.framework.jbidescriptor.generated.Consumes;
import org.ow2.petals.se.mapping.AbstractTest;
import org.ow2.petals.se.mapping.incoming.AnnotatedWsdlParser;
import org.ow2.petals.se.mapping.incoming.exception.InvalidAnnotationException;
import org.ow2.petals.se.mapping.incoming.exception.NoMappingOperationException;
import org.ow2.petals.se.mapping.incoming.exception.NoWsdlBindingException;
import org.ow2.petals.se.mapping.incoming.message.MappingMessage;
import org.ow2.petals.se.mapping.incoming.message.exception.InvalidAnnotationForMessageException;
import org.ow2.petals.se.mapping.incoming.message.xsl.MessageXslMapping;
import org.ow2.petals.se.mapping.incoming.message.xsl.exception.InvalidXslFileMappingException;
import org.ow2.petals.se.mapping.incoming.message.xsl.exception.MultipleMappingXslDefinedException;
import org.ow2.petals.se.mapping.incoming.message.xsl.exception.NoMappingXslDefinedException;
import org.ow2.petals.se.mapping.incoming.message.xsl.exception.NoTransformationDefinedException;
import org.ow2.petals.se.mapping.incoming.message.xsl.exception.UnbuildableXslFileMappingException;
import org.ow2.petals.se.mapping.incoming.message.xsl.exception.UnexistingXslMappingException;
import org.ow2.petals.se.mapping.incoming.operation.MappingOperation;
import org.ow2.petals.se.mapping.incoming.operation.exception.MultipleServiceProviderOperationDefinedException;
import org.ow2.petals.se.mapping.incoming.operation.exception.NoInputOperationException;
import org.ow2.petals.se.mapping.incoming.operation.exception.NoServiceProviderOperationException;
import org.ow2.petals.se.mapping.incoming.operation.exception.SeveralInputOperationException;
import org.ow2.petals.se.mapping.incoming.operation.exception.SeveralOutputOperationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.ebmwebsourcing.easycommons.lang.reflect.ReflectionHelper;
import com.ebmwebsourcing.easycommons.xml.DocumentBuilders;

/**
 * Unit tests of {@link AnnotatedWsdlParser}
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public class AnnotatedWsdlParserTest extends AbstractTest {

    private static final String WSDL_TARGET_NAMESPACE = "http://petals.ow2.org/se/mapping/unit-test/parser/facture";

    private static final String MAPPING_OP_ARCHIVER_NAME = "archiver";

    private static final QName MAPPING_OP_ARCHIVER = new QName(WSDL_TARGET_NAMESPACE, MAPPING_OP_ARCHIVER_NAME);

    private static final String MAPPING_OP_CONSULTER_NAME = "consulter";

    private static final QName MAPPING_OP_CONSULTER = new QName(WSDL_TARGET_NAMESPACE, MAPPING_OP_CONSULTER_NAME);

    private static final String MAPPING_OP_SUPPRIMER_NAME = "supprimer";

    private static final QName MAPPING_OP_SUPPRIMER = new QName(WSDL_TARGET_NAMESPACE, MAPPING_OP_SUPPRIMER_NAME);

    private static String SU_ROOT_PATH;

    private static String SU_NAME = "my-mapping-service-unit";

    private static Consumes SU_SERVICE_PROVIDER = new Consumes();

    private final Logger logger = Logger.getLogger(AnnotatedWsdlParserTest.class.getName());

    private final AnnotatedWsdlParser parser = new AnnotatedWsdlParser(this.logger);

    @BeforeClass
    public static void setSuRootPath() throws URISyntaxException {
        final URL urlValidXsl = AnnotatedWsdlParserTest.class.getResource("/parser/xsl/input-valid.xsl");
        assertNotNull("Valid xsl resource not found", urlValidXsl);
        final File validXsl = new File(urlValidXsl.toURI());
        SU_ROOT_PATH = validXsl.getParentFile().getAbsolutePath();
    }

    /**
     * Read a WSDL as {@link Document} from a resource file
     * 
     * @param resourceName
     *            Name of the resource file
     * @return The WSDL as {@link Document}
     */
    private Document readWsdlDocument(final String resourceName) throws SAXException, IOException {
        final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        assertNotNull("WSDL not found", is);
        final DocumentBuilder docBuilder = DocumentBuilders.takeDocumentBuilder();
        try {
            return docBuilder.parse(is);
        } finally {
            DocumentBuilders.releaseDocumentBuilder(docBuilder);
        }
    }

    /**
     * <p>
     * Check the parser against a valid WSDL
     * </p>
     * <p>
     * Expected results:
     * <ul>
     * <li>No error occurs</li>
     * <li>The expected annotated operation are retrieved</li>
     * </ul>
     * </p>
     */
    @Test
    public void parse_WsdlValid() throws SAXException, IOException {

        final List<MappingOperation> mappingOperations = this.parser.parse(this.readWsdlDocument("parser/valid.wsdl"),
                null, SU_ROOT_PATH, SU_NAME, SU_SERVICE_PROVIDER);
        assertEquals(0, this.parser.getEncounteredErrors().size());
        assertEquals(3, mappingOperations.size());
        boolean op1_found = false;
        boolean op2_found = false;
        boolean op3_found = false;
        for (final MappingOperation mappingOperation : mappingOperations) {

            final MappingMessage inputMessageMapping = (MappingMessage) ReflectionHelper
                    .getFieldValue(MappingOperation.class, mappingOperation, "inputMessageMapping", false);
            final MappingMessage outputMessageMapping = (MappingMessage) ReflectionHelper
                    .getFieldValue(MappingOperation.class, mappingOperation, "outputMessageMapping", false);
            final List<MappingMessage> faultMessageMappings = (List) ReflectionHelper
                    .getFieldValue(MappingOperation.class, mappingOperation, "faultMessageMappings", false);

            if (MAPPING_OP_ARCHIVER.equals(mappingOperation.getWsdlOperation())) {
                op1_found = true;
                assertNotNull(inputMessageMapping);
                assertTrue(inputMessageMapping instanceof MessageXslMapping);
                assertNull(outputMessageMapping);
                assertNotNull(faultMessageMappings);
                assertEquals(0, faultMessageMappings.size());
            } else if (MAPPING_OP_CONSULTER.equals(mappingOperation.getWsdlOperation())) {
                op2_found = true;
                assertNotNull(inputMessageMapping);
                assertTrue(inputMessageMapping instanceof MessageXslMapping);
                assertNotNull(outputMessageMapping);
                assertTrue(outputMessageMapping instanceof MessageXslMapping);
                assertNotNull(faultMessageMappings);
                assertEquals(1, faultMessageMappings.size());
                assertTrue(faultMessageMappings.get(0) instanceof MessageXslMapping);
            } else if (MAPPING_OP_SUPPRIMER.equals(mappingOperation.getWsdlOperation())) {
                op3_found = true;
                assertNotNull(inputMessageMapping);
                assertTrue(inputMessageMapping instanceof MessageXslMapping);
                assertNotNull(outputMessageMapping);
                assertTrue(outputMessageMapping instanceof MessageXslMapping);
                assertNotNull(faultMessageMappings);
                assertEquals(1, faultMessageMappings.size());
                assertTrue(faultMessageMappings.get(0) instanceof MessageXslMapping);
            } else {
                fail("Unexpected annotated operation");
            }
        }
        assertTrue(op1_found);
        assertTrue(op2_found);
        assertTrue(op3_found);
    }

    /**
     * <p>
     * Check the parser against a valid WSDL containing imports
     * </p>
     * <p>
     * Expected results:
     * <ul>
     * <li>No error occurs</li>
     * <li>The expected annotated operation are retrieved</li>
     * </ul>
     * </p>
     */
    @Test
    public void parse_WsdlValidWithImports() throws SAXException, IOException {

        final List<MappingOperation> mappingOperations = this.parser.parse(
                this.readWsdlDocument("parser/valid-with-imports.wsdl"), null, SU_ROOT_PATH, SU_NAME,
                SU_SERVICE_PROVIDER);
        assertEquals(0, this.parser.getEncounteredErrors().size());
        assertEquals(3, mappingOperations.size());
        boolean op1_found = false;
        boolean op2_found = false;
        boolean op3_found = false;
        for (final MappingOperation mappingOperation : mappingOperations) {

            final MappingMessage inputMessageMapping = (MappingMessage) ReflectionHelper
                    .getFieldValue(MappingOperation.class, mappingOperation, "inputMessageMapping", false);
            final MappingMessage outputMessageMapping = (MappingMessage) ReflectionHelper
                    .getFieldValue(MappingOperation.class, mappingOperation, "outputMessageMapping", false);

            if (MAPPING_OP_ARCHIVER.equals(mappingOperation.getWsdlOperation())) {
                op1_found = true;
                assertNotNull(inputMessageMapping);
                assertTrue(inputMessageMapping instanceof MessageXslMapping);
                assertNull(outputMessageMapping);
            } else if (MAPPING_OP_CONSULTER.equals(mappingOperation.getWsdlOperation())) {
                op2_found = true;
                assertNotNull(inputMessageMapping);
                assertTrue(inputMessageMapping instanceof MessageXslMapping);
                assertNotNull(outputMessageMapping);
                assertTrue(outputMessageMapping instanceof MessageXslMapping);
            } else if (MAPPING_OP_SUPPRIMER.equals(mappingOperation.getWsdlOperation())) {
                op3_found = true;
                assertNotNull(inputMessageMapping);
                assertTrue(inputMessageMapping instanceof MessageXslMapping);
                assertNotNull(outputMessageMapping);
                assertTrue(outputMessageMapping instanceof MessageXslMapping);
            } else {
                fail("Unexpected annotated operation");
            }
        }
        assertTrue(op1_found);
        assertTrue(op2_found);
        assertTrue(op3_found);
    }

    /**
     * <p>
     * Check the parser against a WSDL that does not contain binding
     * </p>
     * <p>
     * Expected results: An error occurs about missing binding
     * </p>
     */
    @Test
    public void parse_WsdlWithoutBinding() throws SAXException, IOException {

        assertEquals(0,
                this.parser
                        .parse(this.readWsdlDocument("parser/abstract-import.wsdl"), null, SU_ROOT_PATH, SU_NAME,
                                SU_SERVICE_PROVIDER)
                        .size());
        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(2, encounteredErrors.size());
        boolean noWsdlBindingExceptionCounter = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof NoWsdlBindingException) {
                noWsdlBindingExceptionCounter = true;
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(noWsdlBindingExceptionCounter);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL that does not contain mapping annotation
     * </p>
     * <p>
     * Expected results: An error occurs about missing annotations for each wsdl operations, and an error occurs about
     * no annotated operation found.
     * </p>
     */
    @Test
    public void parse_WsdlWithoutMappingAnnotations() throws SAXException, IOException {

        assertEquals(0,
                this.parser
                        .parse(this.readWsdlDocument("parser/withoutMappingAnnotation.wsdl"), null, SU_ROOT_PATH,
                                SU_NAME, SU_SERVICE_PROVIDER)
                        .size());
        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(3, encounteredErrors.size());
        int multipleNoServiceProviderOperationExceptionCounter = 0;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof NoServiceProviderOperationException) {
                multipleNoServiceProviderOperationExceptionCounter++;
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertEquals(2, multipleNoServiceProviderOperationExceptionCounter);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL that contains an WSDL operation containing several annotations about service
     * provider operation
     * </p>
     * <p>
     * Expected results: An error occurs about multiple annotations
     * </p>
     */
    @Test
    public void parse_WsdlWithSeveralServiceProviderOpInOneWsdlOp() throws SAXException, IOException {

        assertEquals(0,
                this.parser
                        .parse(this.readWsdlDocument("parser/several-service-provider-op-in-one-wsdl-op.wsdl"), null,
                                SU_ROOT_PATH, SU_NAME, SU_SERVICE_PROVIDER)
                        .size());
        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(2, encounteredErrors.size());
        boolean multipleServiceProviderOperationDefinedException = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof MultipleServiceProviderOperationDefinedException) {
                multipleServiceProviderOperationDefinedException = true;
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(multipleServiceProviderOperationDefinedException);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL containing mapping annotations but the service provider operation is as
     * following:
     * <ul>
     * <li>tag missing (ie. no XML tag service provider operation),</li>
     * <li>tag closed (ie. XML tag service provider operation without value),</li>
     * <li>empty (ie. the XML value is empty).</li>
     * </ul>
     * </p>
     * <p>
     * Expected results: An error occurs about the missing or empty service provider operation, and an error occurs
     * about no valid annotated operation found.
     * </p>
     */
    @Test
    public void parse_WsdlWithServiceProviderOpMissing() throws SAXException, IOException {

        assertEquals(0,
                this.parser
                        .parse(this.readWsdlDocument("parser/missing-and-empty-service-provider-op.wsdl"), null,
                                SU_ROOT_PATH, SU_NAME, SU_SERVICE_PROVIDER)
                        .size());

        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(4, encounteredErrors.size());
        boolean missingTagServiceProviderOperationOp1 = false;
        boolean tagNoSetServiceProviderOPerationOp2 = false;
        boolean emptyServiceProviderOperationOp3 = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof NoServiceProviderOperationException) {
                if (MAPPING_OP_ARCHIVER.equals(((NoServiceProviderOperationException) exception).getWsdlOperation())) {
                    missingTagServiceProviderOperationOp1 = true;
                } else if (MAPPING_OP_CONSULTER
                        .equals(((NoServiceProviderOperationException) exception).getWsdlOperation())) {
                    tagNoSetServiceProviderOPerationOp2 = true;
                } else if (MAPPING_OP_SUPPRIMER
                        .equals(((NoServiceProviderOperationException) exception).getWsdlOperation())) {
                    emptyServiceProviderOperationOp3 = true;
                } else {
                    fail("Unexpected operation: "
                            + ((InvalidAnnotationForMessageException) exception).getWsdlOperation());
                }
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(missingTagServiceProviderOperationOp1);
        assertTrue(tagNoSetServiceProviderOPerationOp2);
        assertTrue(emptyServiceProviderOperationOp3);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL containing a binding operation having no input message.
     * </p>
     * <p>
     * Expected results: An error occurs about the missing input message, and an error occurs about no valid annotated
     * operation found.
     * </p>
     */
    @Test
    public void parse_WsdlWithBindingOpWithoutInput() throws SAXException, IOException {

        assertEquals(0, this.parser.parse(this.readWsdlDocument("parser/binding-op-without-input.wsdl"), null,
                SU_ROOT_PATH, SU_NAME, SU_SERVICE_PROVIDER).size());

        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(2, encounteredErrors.size());
        boolean noInputOperationException = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof NoInputOperationException) {
                noInputOperationException = true;
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(noInputOperationException);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL containing a binding operation having several input messages.
     * </p>
     * <p>
     * Expected results: An error occurs about the several input messages, and an error occurs about no valid annotated
     * operation found.
     * </p>
     */
    @Test
    public void parse_WsdlWithBindingOpWithSeveralInputs() throws SAXException, IOException {

        assertEquals(0, this.parser.parse(this.readWsdlDocument("parser/binding-op-witht-several-input.wsdl"), null,
                SU_ROOT_PATH, SU_NAME, SU_SERVICE_PROVIDER).size());

        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(2, encounteredErrors.size());
        boolean severalInputOperationException = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof SeveralInputOperationException) {
                severalInputOperationException = true;
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(severalInputOperationException);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL containing a binding operation having several output messages.
     * </p>
     * <p>
     * Expected results: An error occurs about the several output messages, and an error occurs about no valid annotated
     * operation found.
     * </p>
     */
    @Test
    public void parse_WsdlWithBindingOpWithSeveralOutputs() throws SAXException, IOException {

        assertEquals(0, this.parser.parse(this.readWsdlDocument("parser/binding-op-witht-several-output.wsdl"), null,
                SU_ROOT_PATH, SU_NAME, SU_SERVICE_PROVIDER).size());

        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(2, encounteredErrors.size());
        boolean severalOutputOperationException = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof SeveralOutputOperationException) {
                severalOutputOperationException = true;
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(severalOutputOperationException);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL containing mapping annotations but the xsl style-sheet of input message is as
     * following:
     * <ul>
     * <li>tag missing (ie. no XML tag xsl),</li>
     * <li>tag closed (ie. XML tag xsl without value),</li>
     * <li>empty (ie. the XML value is empty).</li>
     * </ul>
     * </p>
     * <p>
     * Expected results: An error occurs about the missing or empty XSL style-sheet tag, and an error occurs about no
     * valid annotated operation found.
     * </p>
     */
    @Test
    public void parse_WsdlWithInputXslMissing() throws SAXException, IOException {

        assertEquals(0, this.parser.parse(this.readWsdlDocument("parser/input-xsl-missing-and-empty.wsdl"), null,
                SU_ROOT_PATH, SU_NAME, SU_SERVICE_PROVIDER).size());

        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(4, encounteredErrors.size());
        boolean missingTag = false;
        boolean tagNoSet = false;
        boolean emptyTag = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof NoTransformationDefinedException) {
                final InvalidAnnotationForMessageException msgException = (InvalidAnnotationForMessageException) exception;
                assertEquals("input", msgException.getWsdlMessageName());
                if (MAPPING_OP_ARCHIVER.equals(msgException.getWsdlOperation())) {
                    missingTag = true;
                } else {
                    fail("Unexpected operation: " + msgException.getWsdlOperation());
                }
            } else if (exception instanceof NoMappingXslDefinedException) {
                final InvalidAnnotationForMessageException msgException = (InvalidAnnotationForMessageException) exception;
                assertEquals("input", msgException.getWsdlMessageName());
                if (MAPPING_OP_CONSULTER.equals(msgException.getWsdlOperation())) {
                    tagNoSet = true;
                } else if (MAPPING_OP_SUPPRIMER.equals(msgException.getWsdlOperation())) {
                    emptyTag = true;
                } else {
                    fail("Unexpected operation: " + msgException.getWsdlOperation());
                }
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(missingTag);
        assertTrue(tagNoSet);
        assertTrue(emptyTag);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL containing mapping annotations but the xsl style-sheet annotation of input
     * message is duplicated
     * </p>
     * <p>
     * Expected results: An error occurs about the duplicated annotation, and an error occurs about no valid annotated
     * operation found.
     * </p>
     */
    @Test
    public void parse_WsdlWithInputXslDuplicate() throws SAXException, IOException {

        assertEquals(0, this.parser.parse(this.readWsdlDocument("parser/input-xsl-duplicate.wsdl"), null,
                SU_ROOT_PATH, SU_NAME, SU_SERVICE_PROVIDER).size());

        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(2, encounteredErrors.size());
        boolean multipleMappingXslDefinedException = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof MultipleMappingXslDefinedException) {
                multipleMappingXslDefinedException = true;
                assertEquals("input", ((MultipleMappingXslDefinedException) exception).getWsdlMessageName());
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(multipleMappingXslDefinedException);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL containing mapping annotations but the xsl style-sheet of input message does not
     * exist in the service unit.
     * </p>
     * <p>
     * Expected results: An error occurs about the unexisting input XSLT style-sheet, and an error occurs about no valid
     * annotated operation found.
     * </p>
     */
    @Test
    public void parse_WsdlWithInputUnexistingXsl() throws SAXException, IOException {

        assertEquals(0, this.parser.parse(this.readWsdlDocument("parser/input-xsl-not-found.wsdl"), null, SU_ROOT_PATH,
                SU_NAME, SU_SERVICE_PROVIDER).size());

        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(2, encounteredErrors.size());
        boolean unexistingXslMappingException = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof UnexistingXslMappingException) {
                unexistingXslMappingException = true;
                assertEquals("input", ((UnexistingXslMappingException) exception).getWsdlMessageName());
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(unexistingXslMappingException);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL containing a mapping annotation for an input message but the XSLT style-sheet is
     * set to an invalid XSL style-sheet
     * </p>
     * <p>
     * Expected results: An error occurs about the invalid XSLT style-sheet, and an error occurs about no valid
     * annotated operation found.
     * </p>
     */
    @Test
    public void parse_WsdlWithInputXslInvalid() throws SAXException, IOException {

        assertEquals(0, this.parser.parse(this.readWsdlDocument("parser/input-xsl-invalid.wsdl"), null, SU_ROOT_PATH,
                SU_NAME, SU_SERVICE_PROVIDER).size());

        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(2, encounteredErrors.size());
        boolean unbuildableXslFileMappingException = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof UnbuildableXslFileMappingException) {
                unbuildableXslFileMappingException = true;
                assertEquals("input", ((UnbuildableXslFileMappingException) exception).getWsdlMessageName());
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(unbuildableXslFileMappingException);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL containing a mapping annotation for an input message but the XSLT style-sheet is
     * not a file.
     * </p>
     * <p>
     * Expected results: An error occurs about the invalid file format of the XSL style-sheet, and an error occurs about
     * no valid annotated operation found.
     * </p>
     */
    @Test
    public void parse_WsdlWithInputXslNotAFile() throws SAXException, IOException {

        assertEquals(0, this.parser.parse(this.readWsdlDocument("parser/input-xsl-not-a-file.wsdl"), null, SU_ROOT_PATH,
                SU_NAME, SU_SERVICE_PROVIDER).size());

        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(2, encounteredErrors.size());
        boolean invalidXslFileMappingException = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof InvalidXslFileMappingException) {
                invalidXslFileMappingException = true;
                assertEquals("input", ((InvalidXslFileMappingException) exception).getWsdlMessageName());
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(invalidXslFileMappingException);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL containing mapping annotations but the xsl style-sheet of output message is as
     * following:
     * <ul>
     * <li>tag missing (ie. no XML tag xsl),</li>
     * <li>tag closed (ie. XML tag xsl without value),</li>
     * <li>empty (ie. the XML value is empty).</li>
     * </ul>
     * </p>
     * <p>
     * Expected results: An error occurs about the missing or empty XSL style-sheet tag, and an error occurs about no
     * valid annotated operation found.
     * </p>
     */
    @Test
    public void parse_WsdlWithOutputXslMissing() throws SAXException, IOException {

        assertEquals(0, this.parser.parse(this.readWsdlDocument("parser/output-xsl-missing-and-empty.wsdl"), null,
                SU_ROOT_PATH, SU_NAME, SU_SERVICE_PROVIDER).size());

        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(4, encounteredErrors.size());
        boolean missingTag = false;
        boolean tagNoSet = false;
        boolean emptyTag = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof NoTransformationDefinedException) {
                final InvalidAnnotationForMessageException msgException = (InvalidAnnotationForMessageException) exception;
                assertEquals("output", msgException.getWsdlMessageName());
                if (MAPPING_OP_ARCHIVER.equals(msgException.getWsdlOperation())) {
                    missingTag = true;
                } else {
                    fail("Unexpected operation: " + msgException.getWsdlOperation());
                }
            } else if (exception instanceof NoMappingXslDefinedException) {
                final InvalidAnnotationForMessageException msgException = (InvalidAnnotationForMessageException) exception;
                assertEquals("output", msgException.getWsdlMessageName());
                if (MAPPING_OP_CONSULTER.equals(msgException.getWsdlOperation())) {
                    tagNoSet = true;
                } else if (MAPPING_OP_SUPPRIMER.equals(msgException.getWsdlOperation())) {
                    emptyTag = true;
                } else {
                    fail("Unexpected operation: " + msgException.getWsdlOperation());
                }
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(missingTag);
        assertTrue(tagNoSet);
        assertTrue(emptyTag);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL containing mapping annotations but the xsl style-sheet annotation of output
     * message is duplicated
     * </p>
     * <p>
     * Expected results: An error occurs about the duplicated annotation, and an error occurs about no valid annotated
     * operation found.
     * </p>
     */
    @Test
    public void parse_WsdlWithOutputXslDuplicate() throws SAXException, IOException {

        assertEquals(0, this.parser.parse(this.readWsdlDocument("parser/output-xsl-duplicate.wsdl"), null, SU_ROOT_PATH,
                SU_NAME, SU_SERVICE_PROVIDER).size());

        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(2, encounteredErrors.size());
        boolean multipleMappingXslDefinedException = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof MultipleMappingXslDefinedException) {
                multipleMappingXslDefinedException = true;
                assertEquals("output", ((MultipleMappingXslDefinedException) exception).getWsdlMessageName());
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(multipleMappingXslDefinedException);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL containing mapping annotations but the xsl style-sheet of output message does not
     * exist in the service unit.
     * </p>
     * <p>
     * Expected results: An error occurs about the unexisting output XSLT style-sheet, and an error occurs about no
     * valid annotated operation found.
     * </p>
     */
    @Test
    public void parse_WsdlWithOutputUnexistingXsl() throws SAXException, IOException {

        assertEquals(0, this.parser.parse(this.readWsdlDocument("parser/output-xsl-not-found.wsdl"), null, SU_ROOT_PATH,
                SU_NAME, SU_SERVICE_PROVIDER).size());

        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(2, encounteredErrors.size());
        boolean unexistingXslMappingException = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof UnexistingXslMappingException) {
                unexistingXslMappingException = true;
                assertEquals("output", ((UnexistingXslMappingException) exception).getWsdlMessageName());
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(unexistingXslMappingException);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL containing a mapping annotation for an output message but the XSLT style-sheet is
     * set to an invalid XSL style-sheet
     * </p>
     * <p>
     * Expected results: An error occurs about the invalid XSLT style-sheet, and an error occurs about no valid
     * annotated operation found.
     * </p>
     */
    @Test
    public void parse_WsdlWithOutputXslInvalid() throws SAXException, IOException {

        assertEquals(0, this.parser.parse(this.readWsdlDocument("parser/output-xsl-invalid.wsdl"), null, SU_ROOT_PATH,
                SU_NAME, SU_SERVICE_PROVIDER).size());

        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(2, encounteredErrors.size());
        boolean unbuildableXslFileMappingException = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof UnbuildableXslFileMappingException) {
                unbuildableXslFileMappingException = true;
                assertEquals("output", ((UnbuildableXslFileMappingException) exception).getWsdlMessageName());
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(unbuildableXslFileMappingException);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL containing a mapping annotation for an output message but the XSLT style-sheet is
     * not a file.
     * </p>
     * <p>
     * Expected results: An error occurs about the invalid file format of the XSL style-sheet, and an error occurs about
     * no valid annotated operation found.
     * </p>
     */
    @Test
    public void parse_WsdlWithOutputXslNotAFile() throws SAXException, IOException {

        assertEquals(0,
                this.parser.parse(this.readWsdlDocument("parser/output-xsl-not-a-file.wsdl"), null, SU_ROOT_PATH,
                SU_NAME, SU_SERVICE_PROVIDER).size());

        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(2, encounteredErrors.size());
        boolean invalidXslFileMappingException = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof InvalidXslFileMappingException) {
                invalidXslFileMappingException = true;
                assertEquals("output", ((InvalidXslFileMappingException) exception).getWsdlMessageName());
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(invalidXslFileMappingException);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL containing mapping annotations but the xsl style-sheet of fault message is as
     * following:
     * <ul>
     * <li>tag missing (ie. no XML tag xsl),</li>
     * <li>tag closed (ie. XML tag xsl without value),</li>
     * <li>empty (ie. the XML value is empty).</li>
     * </ul>
     * </p>
     * <p>
     * Expected results: An error occurs about the missing or empty XSL style-sheet tag, and an error occurs about no
     * valid annotated operation found.
     * </p>
     */
    @Test
    public void parse_WsdlWithFaultXslMissing() throws SAXException, IOException {

        assertEquals(0, this.parser.parse(this.readWsdlDocument("parser/fault-xsl-missing-and-empty.wsdl"), null,
                SU_ROOT_PATH, SU_NAME, SU_SERVICE_PROVIDER).size());

        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(4, encounteredErrors.size());
        boolean missingTag = false;
        boolean tagNoSet = false;
        boolean emptyTag = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof NoTransformationDefinedException) {
                final InvalidAnnotationForMessageException msgException = (InvalidAnnotationForMessageException) exception;
                if (MAPPING_OP_ARCHIVER.equals(msgException.getWsdlOperation())) {
                    missingTag = true;
                    assertEquals("factureInconnue-tagMissing", msgException.getWsdlMessageName());
                } else {
                    fail("Unexpected operation: " + msgException.getWsdlOperation());
                }
            } else if (exception instanceof NoMappingXslDefinedException) {
                final InvalidAnnotationForMessageException msgException = (InvalidAnnotationForMessageException) exception;
                if (MAPPING_OP_CONSULTER.equals(msgException.getWsdlOperation())) {
                    tagNoSet = true;
                    assertEquals("factureInconnue-tagClosed", msgException.getWsdlMessageName());
                } else if (MAPPING_OP_SUPPRIMER.equals(msgException.getWsdlOperation())) {
                    emptyTag = true;
                    assertEquals("factureInconnue-tagEmpty", msgException.getWsdlMessageName());
                } else {
                    fail("Unexpected operation: " + msgException.getWsdlOperation());
                }
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(missingTag);
        assertTrue(tagNoSet);
        assertTrue(emptyTag);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL containing mapping annotations but the xsl style-sheet annotation of fault
     * message is duplicated
     * </p>
     * <p>
     * Expected results: An error occurs about the duplicated annotation, and an error occurs about no valid annotated
     * operation found.
     * </p>
     */
    @Test
    public void parse_WsdlWithFaultXslDuplicate() throws SAXException, IOException {

        assertEquals(0, this.parser.parse(this.readWsdlDocument("parser/fault-xsl-duplicate.wsdl"), null, SU_ROOT_PATH,
                SU_NAME, SU_SERVICE_PROVIDER).size());

        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(2, encounteredErrors.size());
        boolean multipleMappingXslDefinedException = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof MultipleMappingXslDefinedException) {
                multipleMappingXslDefinedException = true;
                assertEquals("factureInconnue", ((MultipleMappingXslDefinedException) exception).getWsdlMessageName());
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(multipleMappingXslDefinedException);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL containing mapping annotations but the xsl style-sheet of fault message does not
     * exist in the service unit.
     * </p>
     * <p>
     * Expected results: An error occurs about the unexisting input XSLT style-sheet, and an error occurs about no valid
     * annotated operation found.
     * </p>
     */
    @Test
    public void parse_WsdlWithFaultUnexistingXsl() throws SAXException, IOException {

        assertEquals(0, this.parser.parse(this.readWsdlDocument("parser/fault-xsl-not-found.wsdl"), null, SU_ROOT_PATH,
                SU_NAME, SU_SERVICE_PROVIDER).size());

        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(2, encounteredErrors.size());
        boolean unexistingXslMappingException = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof UnexistingXslMappingException) {
                unexistingXslMappingException = true;
                assertEquals("factureInconnue", ((UnexistingXslMappingException) exception).getWsdlMessageName());
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(unexistingXslMappingException);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL containing a mapping annotation for a fault message but the XSLT style-sheet is
     * set to an invalid XSL style-sheet
     * </p>
     * <p>
     * Expected results: An error occurs about the invalid XSLT style-sheet, and an error occurs about no valid
     * annotated operation found.
     * </p>
     */
    @Test
    public void parse_WsdlWithFaultXslInvalid() throws SAXException, IOException {

        assertEquals(0, this.parser.parse(this.readWsdlDocument("parser/fault-xsl-invalid.wsdl"), null, SU_ROOT_PATH,
                SU_NAME, SU_SERVICE_PROVIDER).size());

        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(2, encounteredErrors.size());
        boolean unbuildableXslFileMappingException = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof UnbuildableXslFileMappingException) {
                unbuildableXslFileMappingException = true;
                assertEquals("factureInconnue", ((UnbuildableXslFileMappingException) exception).getWsdlMessageName());
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(unbuildableXslFileMappingException);
        assertTrue(noMappingOperationExceptionFound);
    }

    /**
     * <p>
     * Check the parser against a WSDL containing a mapping annotation for a fault message but the XSLT style-sheet is
     * not a file.
     * </p>
     * <p>
     * Expected results: An error occurs about the invalid file format of the XSL style-sheet, and an error occurs about
     * no valid annotated operation found.
     * </p>
     */
    @Test
    public void parse_WsdlWithFaultXslNotAFile() throws SAXException, IOException {

        assertEquals(0, this.parser.parse(this.readWsdlDocument("parser/fault-xsl-not-a-file.wsdl"), null, SU_ROOT_PATH,
                SU_NAME, SU_SERVICE_PROVIDER).size());

        final List<InvalidAnnotationException> encounteredErrors = this.parser.getEncounteredErrors();
        assertEquals(2, encounteredErrors.size());
        boolean invalidXslFileMappingException = false;
        boolean noMappingOperationExceptionFound = false;
        for (final InvalidAnnotationException exception : encounteredErrors) {
            if (exception instanceof InvalidXslFileMappingException) {
                invalidXslFileMappingException = true;
                assertEquals("factureInconnue", ((InvalidXslFileMappingException) exception).getWsdlMessageName());
            } else if (exception instanceof NoMappingOperationException) {
                noMappingOperationExceptionFound = true;
            } else {
                fail("Unexpected error: " + exception.getClass());
            }
        }
        assertTrue(invalidXslFileMappingException);
        assertTrue(noMappingOperationExceptionFound);
    }
}
