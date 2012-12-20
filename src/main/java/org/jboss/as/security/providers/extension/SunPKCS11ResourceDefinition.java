/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.security.providers.extension;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.MapAttributeDefinition;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.operations.validation.ModelTypeValidator;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;

/**
 * A ResourceDefinition for SunPKCS11.
 * 
 * @author Josef Cacek
 */
public class SunPKCS11ResourceDefinition extends SimpleResourceDefinition {

    protected static final AttributeDefinition ATTRIBUTES = new AttributesAttributeDefinition();

    // Constructors ----------------------------------------------------------

    SunPKCS11ResourceDefinition() {
        super(SecurityProvidersExtension.SUNPKCS11_PATH, SecurityProvidersExtension
                .getResourceDescriptionResolver(SecurityProvidersExtension.SUNPKCS11), SunPKCS11Add.INSTANCE,
                SunPKCS11Remove.INSTANCE);
    }

    // Public methods --------------------------------------------------------

    /**
     * Registers the "attributes" attribute.
     * 
     * @param resourceRegistration
     * @see org.jboss.as.controller.SimpleResourceDefinition#registerAttributes(org.jboss.as.controller.registry.ManagementResourceRegistration)
     */
    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadOnlyAttribute(ATTRIBUTES, null);
    }

    // Embedded classes ------------------------------------------------------

    /**
     * A AttributesAttributeDefinition.
     */
    private static class AttributesAttributeDefinition extends MapAttributeDefinition {

        /**
         * Create a new AttributesAttributeDefinition.
         * 
         */
        public AttributesAttributeDefinition() {
            super("attributes", "attribute", true, 0, Integer.MAX_VALUE, new ModelTypeValidator(ModelType.STRING));
        }

        @Override
        protected void addValueTypeDescription(ModelNode node, ResourceBundle bundle) {
            node.get(ModelDescriptionConstants.VALUE_TYPE).set(ModelType.STRING);
        }

        @Override
        protected void addAttributeValueTypeDescription(ModelNode node, ResourceDescriptionResolver resolver, Locale locale,
                ResourceBundle bundle) {
            node.get(ModelDescriptionConstants.VALUE_TYPE).set(ModelType.STRING);
        }

        @Override
        protected void addOperationParameterValueTypeDescription(ModelNode node, String operationName,
                ResourceDescriptionResolver resolver, Locale locale, ResourceBundle bundle) {
            node.get(ModelDescriptionConstants.VALUE_TYPE).set(ModelType.STRING);
        }

        @Override
        public void marshallAsElement(ModelNode resourceModel, XMLStreamWriter writer) throws XMLStreamException {
            if (!isMarshallable(resourceModel))
                return;
            resourceModel = resourceModel.get(getName());
            //            writer.writeStartElement(getName());
            for (Property property : resourceModel.asPropertyList()) {
                writer.writeEmptyElement(getXmlName());
                writer.writeAttribute("name", property.getName());
                writer.writeAttribute("value", property.getValue().asString());
            }
            //            writer.writeEndElement();
        }
    }
}