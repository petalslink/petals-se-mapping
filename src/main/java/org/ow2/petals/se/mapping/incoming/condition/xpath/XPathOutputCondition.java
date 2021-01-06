/**
 * Copyright (c) 2016-2021 Linagora
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
package org.ow2.petals.se.mapping.incoming.condition.xpath;

import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.ow2.petals.se.mapping.incoming.condition.AbstractMappingCondition;
import org.ow2.petals.se.mapping.incoming.condition.MappingOutputCondition;
import org.ow2.petals.se.mapping.incoming.condition.xpath.exception.XpathExprEvaluationUnexpectedException;
import org.ow2.petals.se.mapping.incoming.condition.xpath.exception.XpathExprInvalidException;
import org.ow2.petals.se.mapping.incoming.condition.xpath.exception.XpathExprReturnUnexpectedException;
import org.ow2.petals.se.mapping.incoming.message.exception.InvalidAnnotationForMessageException;
import org.w3c.dom.Document;

/**
 * A {@link MappingOutputCondition} based on an XPath expression
 * 
 * @author Christophe DENEUX - Linagora
 *
 */
public class XPathOutputCondition extends AbstractMappingCondition implements MappingOutputCondition {

    /**
     * The XPath expression as {@link String}
     */
    private XPathExpression xpathExpr = null;

    /**
     * The XPath expression compiled
     */
    private final String xpathExprStr;
    
    
    /**
     * The XPath expression builder
     */
    private final XPath xpathBuilder;

    /**
     * 
     * @param wsdlOperationName
     *            The WSDL operation containing the current annotations
     * @param xpathExprStr
     *            The XPath expression of the condition. Not {@code null} and not empty.
     * @param xpathBuilder
     *            The XPath expression builder
     * @param logger
     */
    public XPathOutputCondition(final QName wsdlOperationName, final String wsdlMessageName, final String xpathExprStr,
            final XPath xpathBuilder, final Logger logger) {
        super(wsdlOperationName, wsdlMessageName, logger);
        this.xpathBuilder = xpathBuilder;
        this.xpathExprStr = xpathExprStr;
    }

    @Override
    public boolean shouldReturnFault(final Document technicalDoc) throws InvalidAnnotationForMessageException {

        try {
            synchronized (this.xpathExpr) {
                // XPathExpression is not thread-safe
                return (boolean) this.xpathExpr.evaluate(technicalDoc, XPathConstants.BOOLEAN);
            }
        } catch (final IllegalArgumentException e) {
            // XPath expression does not return a boolean
            throw new XpathExprReturnUnexpectedException(this.wsdlOperationName, this.wsdlMessageName, e);
        } catch (final XPathExpressionException e) {
            // An error occurs during XPath evaluation
            throw new XpathExprEvaluationUnexpectedException(this.wsdlOperationName, this.wsdlMessageName, e);
        }
    }

    @Override
    public void verifyAnnotationCoherence() throws InvalidAnnotationForMessageException {
        try {
            this.xpathExpr = this.xpathBuilder.compile(this.xpathExprStr);
        } catch (final XPathExpressionException e) {
            throw new XpathExprInvalidException(this.wsdlOperationName, this.wsdlMessageName, e);
        }
    }

    @Override
    public void log() {
        super.log();
        this.logger.config("      - xpath expression = " + this.xpathExprStr);
    }

}
