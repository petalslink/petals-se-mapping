<?xml version="1.0" encoding="UTF-8"?>
<!--
 Copyright (c) 2016 Linagora
 
 This program/library is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 2.1 of the License, or (at your
 option) any later version.
 
 This program/library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this program/library; If not, see http://www.gnu.org/licenses/
 for the GNU Lesser General Public License version 2.1.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:mapping-xsl-properties="http://petals.ow2.org/se/mapping/xsl/param/1.0" xmlns:facture="http://facture.mapping.samples.petals.ow2.org/">

   <xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="no" />

   <xsl:template match="/">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="facture:consulter">
      <xsl:element name="consulter" namespace="http://service.server.ged.mapping.samples.petals.ow2.org/">
         <xsl:element name="reference" namespace="http://service.server.ged.mapping.samples.petals.ow2.org/">
            <xsl:value-of select="identifiant" />
         </xsl:element>
      </xsl:element>
   </xsl:template>

</xsl:stylesheet>
