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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * An extension to the JBoss Application Server which registers additional Java Security Providers at runtime.
 * 
 * @author Josef Cacek
 */
public class SecurityProvidersExtension implements Extension {

    /**
     * The name space used for the {@code substystem} element
     */
    public static final String NAMESPACE = "urn:jboss:domain:security-providers:1.0";

    /**
     * The name of our subsystem within the model.
     */
    public static final String SUBSYSTEM_NAME = "security-providers";

    /**
     * Name of the model attribute, which holds SunPKCS11 attributes.
     */
    public static final String ATTRIBUTES = "attributes";

    /**
     * The parser used for parsing our subsystem
     */
    private final SubsystemParser parser = new SubsystemParser();

    protected static final PathElement SUBSYSTEM_PATH = PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME);

    /** Path to the ResourceBundle. */
    private static final String RESOURCE_NAME = SecurityProvidersExtension.class.getPackage().getName() + ".LocalDescriptions";

    /** Model node name with SunPKCS11 configuration */
    public static final String SUNPKCS11 = "sunpkcs11";

    /** Model node name with security provider class name */
    public static final String SIMPLE_PROVIDER = "simple-provider";

    /** The SunPKCS11 registration address in the model. */
    public static final PathElement SUNPKCS11_PATH = PathElement.pathElement(SUNPKCS11);

    public static final PathElement SIMPLE_PROVIDER_PATH = PathElement.pathElement(SIMPLE_PROVIDER);

    // Public methods --------------------------------------------------------

    /**
     * Initialize the XML parsers for this extension.
     * 
     * @param context
     * @see org.jboss.as.controller.Extension#initializeParsers(org.jboss.as.controller.parsing.ExtensionParsingContext)
     */
    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, NAMESPACE, parser);
    }

    /**
     * Initialize this extension - registers the subsystem and its model.
     * 
     * @param context
     * @see org.jboss.as.controller.Extension#initialize(org.jboss.as.controller.ExtensionContext)
     */
    @Override
    public void initialize(ExtensionContext context) {
        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, 1, 0);
        final ManagementResourceRegistration registration = subsystem
                .registerSubsystemModel(SecuritProvidersDefinition.INSTANCE);
        registration.registerOperationHandler(DESCRIBE, GenericSubsystemDescribeHandler.INSTANCE,
                GenericSubsystemDescribeHandler.INSTANCE, false, OperationEntry.EntryType.PRIVATE);
        registration.registerSubModel(new SimpleResourceDefinition(SecurityProvidersExtension.SIMPLE_PROVIDER_PATH,
                SecurityProvidersExtension.getResourceDescriptionResolver(SecurityProvidersExtension.SIMPLE_PROVIDER),
                SimpleProviderAdd.INSTANCE, SimpleProviderRemove.INSTANCE));
        registration.registerSubModel(new SunPKCS11ResourceDefinition());

        subsystem.registerXMLElementWriter(parser);
    }

    // Protected methods -----------------------------------------------------

    /**
     * Returns {@link ResourceDescriptionResolver} instance for the given key prefix.
     * 
     * @param keyPrefix
     * @return
     */
    protected static StandardResourceDescriptionResolver getResourceDescriptionResolver(final String keyPrefix) {
        String prefix = SUBSYSTEM_NAME + (keyPrefix == null ? "" : "." + keyPrefix);
        return new StandardResourceDescriptionResolver(prefix, RESOURCE_NAME,
                SecurityProvidersExtension.class.getClassLoader(), true, false);
    }

    // Private methods -------------------------------------------------------

    /**
     * Creates add operation for this subsystem.
     * 
     * @return ModelNode instance
     */
    private static ModelNode createAddSubsystemOperation() {
        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ADD);
        subsystem.get(OP_ADDR).add(SUBSYSTEM, SUBSYSTEM_NAME);
        return subsystem;
    }

    // Embedded classes ------------------------------------------------------

    /**
     * The subsystem parser, which uses STAX to read and write to and from XML.
     */
    private static class SubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>,
            XMLElementWriter<SubsystemMarshallingContext> {

        private static final String EL_SECURITY_PROVIDERS = "security-providers";
        private static final String EL_PROVIDER_CLASS = "provider-class";
        private static final String EL_SUNPKCS11 = "sunpkcs11";
        private static final String AT_SUNPKCS11_NAME = "name";
        private static final String EL_ATTRIBUTE = "attribute";
        private static final String AT_ATTRIBUTE_NAME = "name";
        private static final String AT_ATTRIBUTE_VALUE = "value";

        /**
         * {@inheritDoc}
         */
        @Override
        public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context) throws XMLStreamException {
            context.startSubsystemElement(SecurityProvidersExtension.NAMESPACE, false);
            writer.writeStartElement(EL_SECURITY_PROVIDERS);
            ModelNode node = context.getModelNode();
            ModelNode simpleProviderNodes = node.get(SIMPLE_PROVIDER);
            if (simpleProviderNodes.isDefined()) {
                for (Property property : simpleProviderNodes.asPropertyList()) {
                    //write each child element to xml
                    writer.writeStartElement(EL_PROVIDER_CLASS);
                    writer.writeCharacters(property.getName());
                    //end EL_PROVIDER_CLASS
                    writer.writeEndElement();
                }
            }

            ModelNode sunpkcs11Nodes = node.get(SUNPKCS11);
            if (sunpkcs11Nodes.isDefined()) {
                for (Property property : sunpkcs11Nodes.asPropertyList()) {
                    //write each child element to xml
                    writer.writeStartElement(EL_SUNPKCS11);
                    writer.writeAttribute(AT_SUNPKCS11_NAME, property.getName());
                    ModelNode sunpkcs11 = property.getValue();
                    ModelNode attributes = sunpkcs11.get(ATTRIBUTES);
                    if (attributes.isDefined()) {
                        final List<ModelNode> attrList = attributes.asList();
                        for (ModelNode option : attrList) {
                            final Property asProperty = option.asProperty();
                            writer.writeStartElement(EL_ATTRIBUTE);
                            writer.writeAttribute(AT_ATTRIBUTE_NAME, asProperty.getName());
                            writer.writeAttribute(AT_ATTRIBUTE_VALUE, asProperty.getValue().asString());
                            //end EL_ATTRIBUTE
                            writer.writeEndElement();
                        }
                    }
                    //end EL_SUNPKCS11
                    writer.writeEndElement();
                }
            }
            //End EL_PROVIDERS
            writer.writeEndElement();
            //end subsystem            
            writer.writeEndElement();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
            // Require no attributes
            ParseUtils.requireNoAttributes(reader);
            list.add(createAddSubsystemOperation());

            //Read the children
            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                if (!reader.getLocalName().equals(EL_SECURITY_PROVIDERS)) {
                    throw ParseUtils.unexpectedElement(reader);
                }
                while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                    if (reader.isStartElement()) {
                        if (reader.getLocalName().equals(EL_PROVIDER_CLASS)) {
                            ModelNode addTypeOperation = new ModelNode();
                            addTypeOperation.get(OP).set(ADD);
                            final String providerClassName = reader.getElementText();
                            PathAddress addr = PathAddress.pathAddress(SUBSYSTEM_PATH,
                                    PathElement.pathElement(SIMPLE_PROVIDER, providerClassName));
                            addTypeOperation.get(OP_ADDR).set(addr.toModelNode());
                            list.add(addTypeOperation);
                        } else {
                            readSunPKCS11(reader, list);
                        }
                    }
                }
            }
        }

        private void readSunPKCS11(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
            if (!reader.getLocalName().equals(EL_SUNPKCS11)) {
                throw ParseUtils.unexpectedElement(reader);
            }
            ModelNode addTypeOperation = new ModelNode();
            addTypeOperation.get(OP).set(ADD);

            String sunPkcs11Name = null;
            for (int i = 0; i < reader.getAttributeCount(); i++) {
                String attr = reader.getAttributeLocalName(i);
                String value = reader.getAttributeValue(i);
                if (attr.equals(AT_SUNPKCS11_NAME)) {
                    sunPkcs11Name = value;
                } else {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
            if (sunPkcs11Name == null) {
                throw ParseUtils.missingRequiredElement(reader, Collections.singleton(AT_SUNPKCS11_NAME));
            }

            Map<String, String> sunPKCS11Attributes = readSunPKCS11Attributes(reader);

            //Add the 'add' operation for each 'sunpkcs11' child
            PathAddress addr = PathAddress.pathAddress(SUBSYSTEM_PATH, PathElement.pathElement(SUNPKCS11, sunPkcs11Name));
            addTypeOperation.get(OP_ADDR).set(addr.toModelNode());
            if (!sunPKCS11Attributes.isEmpty()) {
                final ModelNode attributesNode = addTypeOperation.get(ATTRIBUTES);
                for (final Map.Entry<String, String> entry : sunPKCS11Attributes.entrySet()) {
                    final String optionName = entry.getKey();
                    final String optionValue = entry.getValue();
                    attributesNode.get(optionName).set(optionValue);
                }
            }
            list.add(addTypeOperation);
        }

        /**
         * 
         * @param reader
         * @param list
         * @return
         */
        private Map<String, String> readSunPKCS11Attributes(XMLExtendedStreamReader reader) throws XMLStreamException {
            final Map<String, String> result = new HashMap<String, String>();
            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                if (reader.isStartElement()) {
                    if (!reader.getLocalName().equals(EL_ATTRIBUTE)) {
                        throw ParseUtils.unexpectedElement(reader);
                    }
                    String attrName = null;
                    String attrValue = null;
                    for (int i = 0; i < reader.getAttributeCount(); i++) {
                        String attr = reader.getAttributeLocalName(i);
                        String value = reader.getAttributeValue(i);
                        if (attr.equals(AT_ATTRIBUTE_NAME)) {
                            attrName = value;
                        } else if (attr.equals(AT_ATTRIBUTE_VALUE)) {
                            attrValue = value;
                        } else {
                            throw ParseUtils.unexpectedAttribute(reader, i);
                        }
                    }
                    ParseUtils.requireNoContent(reader);
                    Set<String> missingAttrSet = new HashSet<String>();
                    if (attrName == null) {
                        missingAttrSet.add(AT_ATTRIBUTE_NAME);
                    }
                    if (attrValue == null) {
                        missingAttrSet.add(AT_ATTRIBUTE_VALUE);
                    }
                    if (!missingAttrSet.isEmpty()) {
                        throw ParseUtils.missingRequiredElement(reader, missingAttrSet);
                    }
                    result.put(attrName, attrValue);
                }
            }
            return result;
        }
    }

}
