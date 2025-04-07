/**
 * Copyright (c) 2016-2025 Linagora
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
package org.ow2.petals.se.mapping.incoming.message.exception;

import javax.jbi.messaging.MessagingException;

/**
 * An error occurs during transformation
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public class TransformException extends MessagingException {

    private static final long serialVersionUID = -9099623890267261067L;

    private static final String MESSAGE = "An error occurs during transformation of the message";

    public TransformException(final Throwable cause) {
        super(MESSAGE, cause);
    }
}
